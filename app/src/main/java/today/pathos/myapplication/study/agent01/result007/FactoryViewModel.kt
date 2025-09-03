package today.pathos.myapplication.study.agent01.result007

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import today.pathos.myapplication.study.common.BaseViewModel
import today.pathos.myapplication.study.common.Item
import today.pathos.myapplication.study.common.ScreenUiState

class FactoryViewModel : BaseViewModel() {
    
    private val _screenUiState = MutableStateFlow<ScreenUiState>(ScreenUiState.Initializing)
    val screenUiState: StateFlow<ScreenUiState> = _screenUiState.asStateFlow()
    
    // Factory Method Pattern - init{} 없이 팩토리 메서드로 상태 생성
    private val _uiState = MutableStateFlow(FactoryUiState.createInitialState())
    val uiState: StateFlow<FactoryUiState> = _uiState.asStateFlow()
    
    // Factory method for creating states based on conditions
    private fun createStateBasedOnCondition(
        items: List<Item>? = null,
        error: String? = null,
        isLoading: Boolean = false,
        isRefreshing: Boolean = false
    ): FactoryUiState {
        return when {
            error != null -> {
                if (error.contains("fatal")) {
                    FactoryUiState.createFatalErrorState(error)
                } else {
                    FactoryUiState.createErrorState(error)
                }
            }
            isLoading -> FactoryUiState.createLoadingState()
            items != null -> {
                if (isRefreshing) {
                    FactoryUiState.createSuccessStateWithRefresh(items)
                } else {
                    FactoryUiState.createSuccessState(items)
                }
            }
            else -> FactoryUiState.createInitialState()
        }
    }
    
    // 수동 초기화 - init{} 대신 외부에서 호출
    fun initialize() {
        if (_uiState.value !is FactoryUiState.Uninitialized) return
        
        viewModelScope.launch {
            _uiState.value = FactoryUiState.createLoadingState()
            delay(2000)
            
            try {
                if (shouldSimulateError()) {
                    val errorMessage = "Factory initialization failed"
                    _uiState.value = FactoryUiState.createErrorState(errorMessage)
                    _screenUiState.value = ScreenUiState.Failed(errorMessage)
                } else {
                    val items = generateInitialItems()
                    _uiState.value = FactoryUiState.createSuccessState(items)
                    _screenUiState.value = ScreenUiState.Succeed
                }
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Unknown factory error"
                _uiState.value = FactoryUiState.createFatalErrorState(errorMessage)
                _screenUiState.value = ScreenUiState.Failed(errorMessage)
            }
        }
    }
    
    fun addItem(title: String, description: String) {
        val currentState = _uiState.value
        if (currentState is FactoryUiState.Success) {
            val newItem = generateNewItem(currentState.items.size)
                .copy(title = title, description = description)
            val updatedItems = currentState.items + newItem
            _uiState.value = FactoryUiState.createSuccessState(updatedItems)
        }
    }
    
    fun removeItem(itemId: String) {
        val currentState = _uiState.value
        if (currentState is FactoryUiState.Success) {
            val updatedItems = currentState.items.filterNot { it.id == itemId }
            _uiState.value = FactoryUiState.createSuccessState(updatedItems)
        }
    }
    
    fun updateItem(updatedItem: Item) {
        val currentState = _uiState.value
        if (currentState is FactoryUiState.Success) {
            val updatedItems = currentState.items.map { item ->
                if (item.id == updatedItem.id) updatedItem else item
            }
            _uiState.value = FactoryUiState.createSuccessState(updatedItems)
        }
    }
    
    fun refresh() {
        val currentState = _uiState.value
        if (currentState is FactoryUiState.Success) {
            viewModelScope.launch {
                // 새로고침 상태로 변경
                _uiState.value = FactoryUiState.createSuccessStateWithRefresh(currentState.items)
                delay(1000)
                
                try {
                    if (shouldSimulateError()) {
                        _uiState.value = FactoryUiState.createErrorState("Refresh failed")
                    } else {
                        val refreshedItems = generateInitialItems()
                        _uiState.value = FactoryUiState.createSuccessState(refreshedItems)
                    }
                } catch (e: Exception) {
                    _uiState.value = FactoryUiState.createErrorState(
                        e.message ?: "Refresh error"
                    )
                }
            }
        }
    }
    
    fun retry() {
        when (val currentState = _uiState.value) {
            is FactoryUiState.Error -> {
                if (currentState.canRetry) {
                    initialize()
                }
            }
            else -> initialize()
        }
    }
    
    fun clearError() {
        val currentState = _uiState.value
        if (currentState is FactoryUiState.Error) {
            _uiState.value = FactoryUiState.createInitialState()
        }
    }
    
    // Factory method for creating items with different strategies
    fun createItemWithStrategy(
        title: String, 
        description: String, 
        strategy: ItemCreationStrategy = ItemCreationStrategy.DEFAULT
    ) {
        val currentState = _uiState.value
        if (currentState is FactoryUiState.Success) {
            val newItem = when (strategy) {
                ItemCreationStrategy.DEFAULT -> generateNewItem(currentState.items.size)
                ItemCreationStrategy.TIMESTAMPED -> generateNewItem(currentState.items.size)
                    .copy(description = "$description (Created at ${System.currentTimeMillis()})")
                ItemCreationStrategy.NUMBERED -> generateNewItem(currentState.items.size)
                    .copy(title = "${currentState.items.size + 1}. $title")
            }.copy(title = title, description = description)
            
            val updatedItems = currentState.items + newItem
            _uiState.value = FactoryUiState.createSuccessState(updatedItems)
        }
    }
    
    enum class ItemCreationStrategy {
        DEFAULT,
        TIMESTAMPED,
        NUMBERED
    }
}