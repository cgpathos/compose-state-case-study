package today.pathos.myapplication.study.agent02.result005

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import today.pathos.myapplication.study.common.BaseViewModel
import today.pathos.myapplication.study.common.Item
import today.pathos.myapplication.study.common.ScreenUiState

class UdfViewModel : BaseViewModel() {
    
    private val _state = MutableStateFlow(UdfState())
    val state: StateFlow<UdfState> = _state.asStateFlow()
    
    private val _screenUiState = MutableStateFlow<ScreenUiState>(ScreenUiState.Initializing)
    val screenUiState: StateFlow<ScreenUiState> = _screenUiState.asStateFlow()
    
    private val _effects = Channel<UdfEffect>(Channel.BUFFERED)
    val effects: Flow<UdfEffect> = _effects.receiveAsFlow()
    
    init {
        handleEvent(UdfEvent.LoadItems)
    }
    
    fun handleEvent(event: UdfEvent) {
        when (event) {
            is UdfEvent.LoadItems -> loadItems()
            is UdfEvent.RefreshItems -> refreshItems()
            is UdfEvent.AddItem -> addItem(event.item)
            is UdfEvent.RemoveItem -> removeItem(event.itemId)
            is UdfEvent.UpdateItem -> updateItem(event.item)
            is UdfEvent.RetryLoad -> retryLoad()
            is UdfEvent.ClearError -> clearError()
        }
    }
    
    private fun loadItems() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                error = null,
                loadingStage = LoadingStage.Loading
            )
            
            try {
                delay(2000) // Simulate network delay
                
                if (shouldSimulateError()) {
                    throw Exception("UDF loading failed")
                }
                
                val items = generateInitialItems()
                
                _state.value = _state.value.copy(
                    items = items,
                    isLoading = false,
                    loadingStage = LoadingStage.Loaded
                )
                
                _screenUiState.value = ScreenUiState.Succeed
                
                // Side effect: Log successful load
                _effects.trySend(UdfEffect.LogError("UDF: Items loaded successfully"))
                
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message,
                    loadingStage = LoadingStage.Failed
                )
                
                _screenUiState.value = ScreenUiState.Failed(e.message ?: "Unknown error")
                
                // Side effect: Show error snackbar
                _effects.trySend(UdfEffect.ShowSnackbar("Failed to load items: ${e.message}"))
            }
        }
    }
    
    private fun refreshItems() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isRefreshing = true, error = null)
            
            try {
                delay(1000) // Simulate refresh delay
                
                if (shouldSimulateError()) {
                    throw Exception("UDF refresh failed")
                }
                
                val items = generateInitialItems()
                
                _state.value = _state.value.copy(
                    items = items,
                    isRefreshing = false
                )
                
                // Side effect: Scroll to top after refresh
                _effects.trySend(UdfEffect.ScrollToTop)
                _effects.trySend(UdfEffect.ShowSnackbar("Items refreshed successfully"))
                
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isRefreshing = false,
                    error = e.message
                )
                
                _effects.trySend(UdfEffect.ShowSnackbar("Refresh failed: ${e.message}"))
            }
        }
    }
    
    private fun addItem(item: Item) {
        val currentItems = _state.value.items
        val updatedItems = currentItems + item
        
        _state.value = _state.value.copy(items = updatedItems)
        
        // Side effect: Show success message
        _effects.trySend(UdfEffect.ShowSnackbar("Item added successfully"))
    }
    
    private fun removeItem(itemId: String) {
        val currentItems = _state.value.items
        val itemToRemove = currentItems.find { it.id == itemId }
        val updatedItems = currentItems.filterNot { it.id == itemId }
        
        _state.value = _state.value.copy(items = updatedItems)
        
        // Side effect: Show removal message
        if (itemToRemove != null) {
            _effects.trySend(UdfEffect.ShowSnackbar("${itemToRemove.title} removed"))
        }
    }
    
    private fun updateItem(item: Item) {
        val currentItems = _state.value.items
        val updatedItems = currentItems.map { if (it.id == item.id) item else it }
        
        _state.value = _state.value.copy(items = updatedItems)
        
        // Side effect: Show update message
        _effects.trySend(UdfEffect.ShowSnackbar("${item.title} updated"))
    }
    
    private fun retryLoad() {
        handleEvent(UdfEvent.LoadItems)
    }
    
    private fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}