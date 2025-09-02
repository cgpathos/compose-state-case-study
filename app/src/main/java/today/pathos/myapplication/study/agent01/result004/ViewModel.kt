package today.pathos.myapplication.study.agent01.result004

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import today.pathos.myapplication.study.common.BaseViewModel
import today.pathos.myapplication.study.common.Item
import today.pathos.myapplication.study.common.ScreenUiState

class ViewModel : BaseViewModel() {
    
    private val _screenUiState = MutableStateFlow<ScreenUiState>(ScreenUiState.Initializing)
    val screenUiState: StateFlow<ScreenUiState> = _screenUiState.asStateFlow()
    
    private val _events = MutableSharedFlow<UiEvent>(extraBufferCapacity = 10)
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()
    
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    init {
        collectEvents()
        initializeScreen()
    }
    
    private fun collectEvents() {
        viewModelScope.launch {
            _events.collect { event ->
                when (event) {
                    is UiEvent.ItemsLoaded -> {
                        // This would typically be handled with data from repository
                        // Here we just handle the loading state
                    }
                    
                    is UiEvent.ItemAdded -> {
                        _uiState.value = _uiState.value.copy(
                            items = _uiState.value.items + event.item,
                            error = null
                        )
                    }
                    
                    is UiEvent.ItemRemoved -> {
                        _uiState.value = _uiState.value.copy(
                            items = _uiState.value.items.filter { it.id != event.itemId },
                            error = null
                        )
                    }
                    
                    is UiEvent.ItemUpdated -> {
                        _uiState.value = _uiState.value.copy(
                            items = _uiState.value.items.map { item ->
                                if (item.id == event.item.id) event.item else item
                            },
                            error = null
                        )
                    }
                    
                    is UiEvent.ItemsRefreshed -> {
                        // Refresh completed, state already updated in refresh method
                    }
                    
                    is UiEvent.Error -> {
                        _uiState.value = _uiState.value.copy(
                            error = event.message,
                            isLoading = false,
                            isRefreshing = false
                        )
                    }
                    
                    is UiEvent.LoadingStarted -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = true,
                            error = null
                        )
                    }
                    
                    is UiEvent.LoadingFinished -> {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                    
                    is UiEvent.RefreshStarted -> {
                        _uiState.value = _uiState.value.copy(
                            isRefreshing = true,
                            error = null
                        )
                    }
                    
                    is UiEvent.RefreshFinished -> {
                        _uiState.value = _uiState.value.copy(isRefreshing = false)
                    }
                }
            }
        }
    }
    
    private fun initializeScreen() {
        viewModelScope.launch {
            try {
                delay(2000) // 2 second delay for initialization
                
                if (shouldSimulateError()) {
                    _screenUiState.value = ScreenUiState.Failed("Failed to initialize screen")
                    return@launch
                }
                
                val initialItems = generateInitialItems()
                _screenUiState.value = ScreenUiState.Succeed
                _uiState.value = _uiState.value.copy(items = initialItems)
                _events.emit(UiEvent.ItemsLoaded)
            } catch (e: Exception) {
                _screenUiState.value = ScreenUiState.Failed(e.message ?: "Unknown error")
            }
        }
    }
    
    fun addItem() {
        viewModelScope.launch {
            _events.emit(UiEvent.LoadingStarted)
            delay(1000) // Simulate network delay
            
            try {
                if (shouldSimulateError()) {
                    _events.emit(UiEvent.Error("Failed to add item"))
                    return@launch
                }
                
                val newItem = generateNewItem(_uiState.value.items.size)
                _events.emit(UiEvent.ItemAdded(newItem))
                _events.emit(UiEvent.LoadingFinished)
            } catch (e: Exception) {
                _events.emit(UiEvent.Error(e.message ?: "Unknown error"))
            }
        }
    }
    
    fun removeItem(itemId: String) {
        viewModelScope.launch {
            _events.emit(UiEvent.LoadingStarted)
            delay(500) // Simulate network delay
            
            try {
                if (shouldSimulateError()) {
                    _events.emit(UiEvent.Error("Failed to remove item"))
                    return@launch
                }
                
                _events.emit(UiEvent.ItemRemoved(itemId))
                _events.emit(UiEvent.LoadingFinished)
            } catch (e: Exception) {
                _events.emit(UiEvent.Error(e.message ?: "Unknown error"))
            }
        }
    }
    
    fun updateItem(updatedItem: Item) {
        viewModelScope.launch {
            _events.emit(UiEvent.LoadingStarted)
            delay(500) // Simulate network delay
            
            try {
                if (shouldSimulateError()) {
                    _events.emit(UiEvent.Error("Failed to update item"))
                    return@launch
                }
                
                _events.emit(UiEvent.ItemUpdated(updatedItem))
                _events.emit(UiEvent.LoadingFinished)
            } catch (e: Exception) {
                _events.emit(UiEvent.Error(e.message ?: "Unknown error"))
            }
        }
    }
    
    fun refresh() {
        viewModelScope.launch {
            _events.emit(UiEvent.RefreshStarted)
            delay(1500) // Simulate network delay
            
            try {
                if (shouldSimulateError()) {
                    _events.emit(UiEvent.Error("Failed to refresh"))
                    return@launch
                }
                
                val refreshedItems = generateInitialItems()
                _uiState.value = _uiState.value.copy(
                    items = refreshedItems,
                    error = null
                )
                _events.emit(UiEvent.ItemsRefreshed)
                _events.emit(UiEvent.RefreshFinished)
            } catch (e: Exception) {
                _events.emit(UiEvent.Error(e.message ?: "Unknown error"))
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}