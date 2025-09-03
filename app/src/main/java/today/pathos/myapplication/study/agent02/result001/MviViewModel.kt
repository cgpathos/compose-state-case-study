package today.pathos.myapplication.study.agent02.result001

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import today.pathos.myapplication.study.common.BaseViewModel
import today.pathos.myapplication.study.common.Item
import today.pathos.myapplication.study.common.ScreenUiState

class MviViewModel : BaseViewModel() {
    
    private val _screenUiState = MutableStateFlow<ScreenUiState>(ScreenUiState.Initializing)
    val screenUiState: StateFlow<ScreenUiState> = _screenUiState.asStateFlow()
    
    private val _viewState = MutableStateFlow(ViewState())
    val viewState: StateFlow<ViewState> = _viewState.asStateFlow()
    
    init {
        handleIntent(ViewIntent.LoadItems)
    }
    
    fun handleIntent(intent: ViewIntent) {
        when (intent) {
            is ViewIntent.LoadItems -> loadItems()
            is ViewIntent.RefreshItems -> refreshItems()
            is ViewIntent.AddItem -> addItem(intent.item)
            is ViewIntent.RemoveItem -> removeItem(intent.itemId)
            is ViewIntent.UpdateItem -> updateItem(intent.item)
        }
    }
    
    private fun loadItems() {
        viewModelScope.launch {
            _viewState.value = _viewState.value.copy(isLoading = true, error = null)
            
            try {
                delay(2000) // Simulate network delay
                
                if (shouldSimulateError()) {
                    throw Exception("Network error occurred")
                }
                
                val items = generateInitialItems()
                _viewState.value = _viewState.value.copy(
                    items = items,
                    isLoading = false
                )
                _screenUiState.value = ScreenUiState.Succeed
                
            } catch (e: Exception) {
                _viewState.value = _viewState.value.copy(
                    isLoading = false,
                    error = e.message
                )
                _screenUiState.value = ScreenUiState.Failed(e.message ?: "Unknown error")
            }
        }
    }
    
    private fun refreshItems() {
        viewModelScope.launch {
            _viewState.value = _viewState.value.copy(isRefreshing = true, error = null)
            
            try {
                delay(1000) // Simulate refresh delay
                
                if (shouldSimulateError()) {
                    throw Exception("Refresh failed")
                }
                
                val items = generateInitialItems()
                _viewState.value = _viewState.value.copy(
                    items = items,
                    isRefreshing = false
                )
                
            } catch (e: Exception) {
                _viewState.value = _viewState.value.copy(
                    isRefreshing = false,
                    error = e.message
                )
            }
        }
    }
    
    private fun addItem(item: Item) {
        val currentItems = _viewState.value.items
        val updatedItems = currentItems + item
        _viewState.value = _viewState.value.copy(items = updatedItems)
    }
    
    private fun removeItem(itemId: String) {
        val currentItems = _viewState.value.items
        val updatedItems = currentItems.filterNot { it.id == itemId }
        _viewState.value = _viewState.value.copy(items = updatedItems)
    }
    
    private fun updateItem(item: Item) {
        val currentItems = _viewState.value.items
        val updatedItems = currentItems.map { if (it.id == item.id) item else it }
        _viewState.value = _viewState.value.copy(items = updatedItems)
    }
}