package today.pathos.myapplication.study.agent01.result006

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import today.pathos.myapplication.study.common.BaseViewModel
import today.pathos.myapplication.study.common.Item
import today.pathos.myapplication.study.common.ScreenUiState

class LazyInitViewModel : BaseViewModel() {
    
    private val _screenUiState = MutableStateFlow<ScreenUiState>(ScreenUiState.Initializing)
    val screenUiState: StateFlow<ScreenUiState> = _screenUiState.asStateFlow()
    
    private val _uiState = MutableStateFlow(LazyInitUiState())
    
    // Lazy initialization - init{} 블록 없이 onStart로 첫 구독 시 초기화
    val uiState: StateFlow<LazyInitUiState> = _uiState.asStateFlow()
        .onStart { 
            if (!_uiState.value.isInitialized) {
                initializeIfNeeded()
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily, // Lazy 시작으로 필요할 때만 초기화
            initialValue = LazyInitUiState()
        )
    
    private suspend fun initializeIfNeeded() {
        if (_uiState.value.isInitialized) return
        
        _uiState.update { it.copy(isLoading = true) }
        delay(2000)
        
        try {
            if (shouldSimulateError()) {
                _screenUiState.value = ScreenUiState.Failed("Lazy initialization failed")
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = "Failed to initialize",
                        isInitialized = true
                    )
                }
            } else {
                val items = generateInitialItems()
                _screenUiState.value = ScreenUiState.Succeed
                _uiState.update { 
                    it.copy(
                        items = items,
                        isLoading = false,
                        isInitialized = true
                    )
                }
            }
        } catch (e: Exception) {
            _screenUiState.value = ScreenUiState.Failed(e.message ?: "Unknown error")
            _uiState.update { 
                it.copy(
                    isLoading = false, 
                    error = e.message,
                    isInitialized = true
                )
            }
        }
    }
    
    fun addItem(title: String, description: String) {
        val newItem = generateNewItem(_uiState.value.items.size)
        _uiState.update { currentState ->
            currentState.copy(
                items = currentState.items + newItem.copy(title = title, description = description)
            )
        }
    }
    
    fun removeItem(itemId: String) {
        _uiState.update { currentState ->
            currentState.copy(items = currentState.items.filterNot { it.id == itemId })
        }
    }
    
    fun updateItem(updatedItem: Item) {
        _uiState.update { currentState ->
            currentState.copy(
                items = currentState.items.map { item ->
                    if (item.id == updatedItem.id) updatedItem else item
                }
            )
        }
    }
    
    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }
            delay(1000)
            
            if (shouldSimulateError()) {
                _uiState.update { 
                    it.copy(
                        isRefreshing = false, 
                        error = "Refresh failed"
                    )
                }
            } else {
                val refreshedItems = generateInitialItems()
                _uiState.update { 
                    it.copy(
                        items = refreshedItems,
                        isRefreshing = false
                    )
                }
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    // 수동 초기화 메서드 - 필요 시 외부에서 호출
    fun manualInitialize() {
        viewModelScope.launch {
            initializeIfNeeded()
        }
    }
}