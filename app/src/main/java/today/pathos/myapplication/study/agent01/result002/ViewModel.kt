package today.pathos.myapplication.study.agent01.result002

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import today.pathos.myapplication.study.common.BaseViewModel
import today.pathos.myapplication.study.common.Item
import today.pathos.myapplication.study.common.ScreenUiState

class ViewModel : BaseViewModel() {
    
    private val _screenUiState = MutableStateFlow<ScreenUiState>(ScreenUiState.Initializing)
    val screenUiState: StateFlow<ScreenUiState> = _screenUiState.asStateFlow()
    
    var uiState by mutableStateOf(UiState(isLoading = false))
        private set
    
    init {
        initializeScreen()
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
                uiState = uiState.copy(items = initialItems, isLoading = false)
            } catch (e: Exception) {
                _screenUiState.value = ScreenUiState.Failed(e.message ?: "Unknown error")
            }
        }
    }
    
    fun addItem() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            delay(1000) // Simulate network delay
            
            try {
                if (shouldSimulateError()) {
                    uiState = uiState.copy(isLoading = false, error = "Failed to add item")
                    return@launch
                }
                
                val newItem = generateNewItem(uiState.items.size)
                uiState = uiState.copy(
                    isLoading = false,
                    items = uiState.items + newItem,
                    error = null
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }
    
    fun removeItem(itemId: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            delay(500) // Simulate network delay
            
            try {
                if (shouldSimulateError()) {
                    uiState = uiState.copy(isLoading = false, error = "Failed to remove item")
                    return@launch
                }
                
                val updatedItems = uiState.items.filter { it.id != itemId }
                uiState = uiState.copy(
                    isLoading = false,
                    items = updatedItems,
                    error = null
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }
    
    fun updateItem(updatedItem: Item) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            delay(500) // Simulate network delay
            
            try {
                if (shouldSimulateError()) {
                    uiState = uiState.copy(isLoading = false, error = "Failed to update item")
                    return@launch
                }
                
                val updatedItems = uiState.items.map { item ->
                    if (item.id == updatedItem.id) updatedItem else item
                }
                uiState = uiState.copy(
                    isLoading = false,
                    items = updatedItems,
                    error = null
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }
    
    fun refresh() {
        viewModelScope.launch {
            uiState = uiState.copy(isRefreshing = true, error = null)
            delay(1500) // Simulate network delay
            
            try {
                if (shouldSimulateError()) {
                    uiState = uiState.copy(isRefreshing = false, error = "Failed to refresh")
                    return@launch
                }
                
                val refreshedItems = generateInitialItems()
                uiState = uiState.copy(
                    isRefreshing = false,
                    items = refreshedItems,
                    error = null
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isRefreshing = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }
    
    fun clearError() {
        uiState = uiState.copy(error = null)
    }
}