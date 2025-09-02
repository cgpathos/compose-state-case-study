package today.pathos.myapplication.study.agent03.result001

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import today.pathos.myapplication.study.common.BaseViewModel
import today.pathos.myapplication.study.common.Item
import today.pathos.myapplication.study.common.ScreenUiState

class FlowChainViewModel : BaseViewModel() {
    
    // Initial screen state (only used for initialization)
    var screenUiState by mutableStateOf<ScreenUiState>(ScreenUiState.Initializing)
        private set
    
    // Reactive list state using Flow
    private val _itemsFlow = MutableStateFlow<List<Item>>(emptyList())
    val itemsFlow: StateFlow<List<Item>> = _itemsFlow.asStateFlow()
    
    private val _isLoadingFlow = MutableStateFlow(false)
    val isLoadingFlow: StateFlow<Boolean> = _isLoadingFlow.asStateFlow()
    
    private val _errorFlow = MutableStateFlow<String?>(null)
    val errorFlow: StateFlow<String?> = _errorFlow.asStateFlow()
    
    // Flow chain for operations
    private val _operationTrigger = MutableSharedFlow<Operation>()
    
    private sealed class Operation {
        object InitialLoad : Operation()
        object Refresh : Operation()
        data class AddItem(val item: Item) : Operation()
        data class RemoveItem(val itemId: String) : Operation()
        data class UpdateItem(val item: Item) : Operation()
    }
    
    init {
        setupFlowChain()
        triggerInitialLoad()
    }
    
    private fun setupFlowChain() {
        _operationTrigger
            .onEach { _isLoadingFlow.value = true }
            .onEach { _errorFlow.value = null }
            .flatMapLatest { operation ->
                flow {
                    try {
                        when (operation) {
                            is Operation.InitialLoad -> {
                                delay(2000) // Simulate network delay
                                if (shouldSimulateError()) {
                                    throw Exception("Initial load failed")
                                }
                                emit(generateInitialItems())
                            }
                            is Operation.Refresh -> {
                                delay(1000) // Simulate refresh delay
                                if (shouldSimulateError()) {
                                    throw Exception("Refresh failed")
                                }
                                emit(generateInitialItems())
                            }
                            is Operation.AddItem -> {
                                if (shouldSimulateError()) {
                                    throw Exception("Add operation failed")
                                }
                                val currentList = _itemsFlow.value.toMutableList()
                                currentList.add(operation.item)
                                emit(currentList)
                            }
                            is Operation.RemoveItem -> {
                                if (shouldSimulateError()) {
                                    throw Exception("Remove operation failed")
                                }
                                val currentList = _itemsFlow.value.toMutableList()
                                currentList.removeAll { it.id == operation.itemId }
                                emit(currentList)
                            }
                            is Operation.UpdateItem -> {
                                if (shouldSimulateError()) {
                                    throw Exception("Update operation failed")
                                }
                                val currentList = _itemsFlow.value.toMutableList()
                                val index = currentList.indexOfFirst { it.id == operation.item.id }
                                if (index >= 0) {
                                    currentList[index] = operation.item
                                }
                                emit(currentList)
                            }
                        }
                    } catch (e: Exception) {
                        throw e
                    }
                }
            }
            .catch { error ->
                _errorFlow.value = error.message
            }
            .onEach { items ->
                _itemsFlow.value = items
            }
            .onCompletion {
                _isLoadingFlow.value = false
            }
            .launchIn(viewModelScope)
    }
    
    private fun triggerInitialLoad() {
        viewModelScope.launch {
            _operationTrigger.emit(Operation.InitialLoad)
            // Update screen state after initial load attempt
            delay(2100) // Wait for operation to complete
            screenUiState = if (_errorFlow.value != null) {
                ScreenUiState.Failed(_errorFlow.value!!)
            } else {
                ScreenUiState.Succeed
            }
        }
    }
    
    fun refresh() {
        viewModelScope.launch {
            _operationTrigger.emit(Operation.Refresh)
        }
    }
    
    fun addItem() {
        viewModelScope.launch {
            val newItem = generateNewItem(_itemsFlow.value.size)
            _operationTrigger.emit(Operation.AddItem(newItem))
        }
    }
    
    fun removeItem(itemId: String) {
        viewModelScope.launch {
            _operationTrigger.emit(Operation.RemoveItem(itemId))
        }
    }
    
    fun updateItem(item: Item) {
        viewModelScope.launch {
            val updatedItem = item.copy(
                title = "${item.title} (Updated)",
                timestamp = System.currentTimeMillis()
            )
            _operationTrigger.emit(Operation.UpdateItem(updatedItem))
        }
    }
}