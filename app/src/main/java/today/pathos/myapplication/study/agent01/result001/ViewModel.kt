package today.pathos.myapplication.study.agent01.result001

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
    
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
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
                _uiState.value = UiState.Success(initialItems)
            } catch (e: Exception) {
                _screenUiState.value = ScreenUiState.Failed(e.message ?: "Unknown error")
            }
        }
    }
    
    fun addItem() {
        viewModelScope.launch {
            if (_uiState.value !is UiState.Success) return@launch
            
            _uiState.value = UiState.Loading
            delay(1000) // Simulate network delay
            
            try {
                if (shouldSimulateError()) {
                    _uiState.value = UiState.Error("Failed to add item")
                    return@launch
                }
                
                val currentItems = (_uiState.value as? UiState.Success)?.items ?: return@launch
                val newItem = generateNewItem(currentItems.size)
                _uiState.value = UiState.Success(currentItems + newItem)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    fun removeItem(itemId: String) {
        viewModelScope.launch {
            if (_uiState.value !is UiState.Success) return@launch
            
            _uiState.value = UiState.Loading
            delay(500) // Simulate network delay
            
            try {
                if (shouldSimulateError()) {
                    _uiState.value = UiState.Error("Failed to remove item")
                    return@launch
                }
                
                val currentItems = (_uiState.value as? UiState.Success)?.items ?: return@launch
                val updatedItems = currentItems.filter { it.id != itemId }
                _uiState.value = UiState.Success(updatedItems)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    fun updateItem(updatedItem: Item) {
        viewModelScope.launch {
            if (_uiState.value !is UiState.Success) return@launch
            
            _uiState.value = UiState.Loading
            delay(500) // Simulate network delay
            
            try {
                if (shouldSimulateError()) {
                    _uiState.value = UiState.Error("Failed to update item")
                    return@launch
                }
                
                val currentItems = (_uiState.value as? UiState.Success)?.items ?: return@launch
                val updatedItems = currentItems.map { item ->
                    if (item.id == updatedItem.id) updatedItem else item
                }
                _uiState.value = UiState.Success(updatedItems)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    fun refresh() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            delay(1500) // Simulate network delay
            
            try {
                if (shouldSimulateError()) {
                    _uiState.value = UiState.Error("Failed to refresh")
                    return@launch
                }
                
                val refreshedItems = generateInitialItems()
                _uiState.value = UiState.Success(refreshedItems)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    fun clearError() {
        if (_uiState.value is UiState.Error) {
            _uiState.value = UiState.Success(emptyList())
        }
    }
}