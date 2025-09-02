package today.pathos.myapplication.study.agent03.result004

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

class CombineFlowsViewModel : BaseViewModel() {
    
    // Initial screen state (only used for initialization)
    var screenUiState by mutableStateOf<ScreenUiState>(ScreenUiState.Initializing)
        private set
    
    // Individual state flows that will be combined
    private val _itemsFlow = MutableStateFlow<List<Item>>(emptyList())
    private val _loadingFlow = MutableStateFlow(false)
    private val _errorFlow = MutableStateFlow<String?>(null)
    private val _lastOperationFlow = MutableStateFlow<String?>(null)
    private val _operationCountFlow = MutableStateFlow(0)
    private val _timestampFlow = MutableStateFlow(System.currentTimeMillis())
    
    // Operation triggers
    private val _refreshTrigger = MutableSharedFlow<Unit>()
    private val _addTrigger = MutableSharedFlow<Unit>()
    private val _removeTrigger = MutableSharedFlow<String>()
    private val _updateTrigger = MutableSharedFlow<Item>()
    
    // Combined state for UI
    data class CombinedUiState(
        val items: List<Item>,
        val isLoading: Boolean,
        val error: String?,
        val lastOperation: String?,
        val operationCount: Int,
        val lastUpdateTime: Long,
        val isDataStale: Boolean
    )
    
    // Combine multiple flows into a single state
    val combinedUiState: StateFlow<CombinedUiState> = combine(
        _itemsFlow,
        _loadingFlow,
        _errorFlow,
        _lastOperationFlow,
        _operationCountFlow,
        _timestampFlow
    ) { items, isLoading, error, lastOperation, operationCount, timestamp ->
        val currentTime = System.currentTimeMillis()
        val isDataStale = (currentTime - timestamp) > 30000 // 30 seconds
        
        CombinedUiState(
            items = items,
            isLoading = isLoading,
            error = error,
            lastOperation = lastOperation,
            operationCount = operationCount,
            lastUpdateTime = timestamp,
            isDataStale = isDataStale
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CombinedUiState(
            items = emptyList(),
            isLoading = false,
            error = null,
            lastOperation = null,
            operationCount = 0,
            lastUpdateTime = System.currentTimeMillis(),
            isDataStale = false
        )
    )
    
    // Separate flows for specific aspects (demonstrating flow splitting)
    val itemsFlow: StateFlow<List<Item>> = _itemsFlow.asStateFlow()
    val isLoadingFlow: StateFlow<Boolean> = _loadingFlow.asStateFlow()
    val errorFlow: StateFlow<String?> = _errorFlow.asStateFlow()
    
    // Derived flows
    val itemCountFlow: StateFlow<Int> = _itemsFlow.map { it.size }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)
    
    val hasErrorFlow: StateFlow<Boolean> = _errorFlow.map { it != null }
        .stateIn(viewModelScope, SharingStarted.Lazily, false)
    
    val recentItemsFlow: StateFlow<List<Item>> = _itemsFlow.map { items ->
        val recentThreshold = System.currentTimeMillis() - 10000 // 10 seconds
        items.filter { it.timestamp > recentThreshold }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    init {
        setupFlowCombinations()
        triggerInitialLoad()
    }
    
    private fun setupFlowCombinations() {
        // Initial load flow
        viewModelScope.launch {
            delay(2000)
            try {
                updateLoadingAndError(true, null)
                updateOperationInfo("Initial Load", 1)
                
                if (shouldSimulateError()) {
                    throw Exception("Initial load failed")
                }
                
                val items = generateInitialItems()
                updateItemsAndTimestamp(items)
                updateScreenStateAfterInitialLoad(null)
            } catch (e: Exception) {
                updateLoadingAndError(false, e.message)
                updateScreenStateAfterInitialLoad(e.message)
            } finally {
                updateLoadingAndError(false, null)
            }
        }
        
        // Refresh flow
        _refreshTrigger
            .onEach { updateOperationInfo("Refresh", _operationCountFlow.value + 1) }
            .onEach { updateLoadingAndError(true, null) }
            .flatMapLatest {
                flow {
                    delay(1000)
                    if (shouldSimulateError()) {
                        throw Exception("Refresh failed")
                    }
                    emit(generateInitialItems())
                }
            }
            .catch { error ->
                updateLoadingAndError(false, error.message)
            }
            .onEach { items ->
                updateItemsAndTimestamp(items)
                updateLoadingAndError(false, null)
            }
            .launchIn(viewModelScope)
        
        // Add item flow
        _addTrigger
            .onEach { updateOperationInfo("Add Item", _operationCountFlow.value + 1) }
            .onEach { updateLoadingAndError(true, null) }
            .flatMapLatest {
                flow {
                    delay(300)
                    if (shouldSimulateError()) {
                        throw Exception("Add operation failed")
                    }
                    val newItem = generateNewItem(_itemsFlow.value.size)
                    emit(_itemsFlow.value + newItem)
                }
            }
            .catch { error ->
                updateLoadingAndError(false, error.message)
            }
            .onEach { items ->
                updateItemsAndTimestamp(items)
                updateLoadingAndError(false, null)
            }
            .launchIn(viewModelScope)
        
        // Remove item flow
        _removeTrigger
            .onEach { updateOperationInfo("Remove Item", _operationCountFlow.value + 1) }
            .onEach { updateLoadingAndError(true, null) }
            .flatMapLatest { itemId ->
                flow {
                    delay(300)
                    if (shouldSimulateError()) {
                        throw Exception("Remove operation failed")
                    }
                    emit(_itemsFlow.value.filter { it.id != itemId })
                }
            }
            .catch { error ->
                updateLoadingAndError(false, error.message)
            }
            .onEach { items ->
                updateItemsAndTimestamp(items)
                updateLoadingAndError(false, null)
            }
            .launchIn(viewModelScope)
        
        // Update item flow
        _updateTrigger
            .onEach { updateOperationInfo("Update Item", _operationCountFlow.value + 1) }
            .onEach { updateLoadingAndError(true, null) }
            .flatMapLatest { itemToUpdate ->
                flow {
                    delay(300)
                    if (shouldSimulateError()) {
                        throw Exception("Update operation failed")
                    }
                    val updatedItem = itemToUpdate.copy(
                        title = "${itemToUpdate.title} (Updated)",
                        timestamp = System.currentTimeMillis()
                    )
                    val updatedItems = _itemsFlow.value.map { item ->
                        if (item.id == updatedItem.id) updatedItem else item
                    }
                    emit(updatedItems)
                }
            }
            .catch { error ->
                updateLoadingAndError(false, error.message)
            }
            .onEach { items ->
                updateItemsAndTimestamp(items)
                updateLoadingAndError(false, null)
            }
            .launchIn(viewModelScope)
    }
    
    private fun updateLoadingAndError(isLoading: Boolean, error: String?) {
        _loadingFlow.value = isLoading
        if (error != null) {
            _errorFlow.value = error
        } else if (!isLoading) {
            _errorFlow.value = null
        }
    }
    
    private fun updateItemsAndTimestamp(items: List<Item>) {
        _itemsFlow.value = items
        _timestampFlow.value = System.currentTimeMillis()
    }
    
    private fun updateOperationInfo(operation: String, count: Int) {
        _lastOperationFlow.value = operation
        _operationCountFlow.value = count
    }
    
    private fun updateScreenStateAfterInitialLoad(errorMessage: String?) {
        viewModelScope.launch {
            screenUiState = if (errorMessage != null) {
                ScreenUiState.Failed(errorMessage)
            } else {
                ScreenUiState.Succeed
            }
        }
    }
    
    fun refresh() {
        viewModelScope.launch {
            _refreshTrigger.emit(Unit)
        }
    }
    
    fun addItem() {
        viewModelScope.launch {
            _addTrigger.emit(Unit)
        }
    }
    
    fun removeItem(itemId: String) {
        viewModelScope.launch {
            _removeTrigger.emit(itemId)
        }
    }
    
    fun updateItem(item: Item) {
        viewModelScope.launch {
            _updateTrigger.emit(item)
        }
    }
}