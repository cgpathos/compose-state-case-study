package today.pathos.myapplication.study.agent05.result005

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import today.pathos.myapplication.study.common.BaseViewModel
import today.pathos.myapplication.study.common.Item
import today.pathos.myapplication.study.common.ScreenUiState

data class Agent05UiState(
    val screenState: ScreenUiState = ScreenUiState.Initializing,
    val items: List<Item> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val operationStatus: String? = null
)

class Agent05ViewModel(
    private val itemService: ItemService
) : BaseViewModel() {
    
    private val _uiState = MutableStateFlow(Agent05UiState())
    val uiState: StateFlow<Agent05UiState> = _uiState.asStateFlow()
    
    init {
        loadInitialItems()
    }
    
    private fun loadInitialItems() {
        viewModelScope.launch {
            updateLoadingState(true)
            
            try {
                val result = itemService.loadItems()
                result.fold(
                    onSuccess = { items ->
                        _uiState.value = _uiState.value.copy(
                            screenState = ScreenUiState.Succeed,
                            items = items,
                            isLoading = false,
                            error = null,
                            operationStatus = "Items loaded successfully"
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            screenState = ScreenUiState.Failed(exception.message ?: "Unknown error"),
                            isLoading = false,
                            error = exception.message,
                            operationStatus = "Failed to load items"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    screenState = ScreenUiState.Failed(e.message ?: "Unknown error"),
                    isLoading = false,
                    error = e.message,
                    operationStatus = "Error during initialization"
                )
            }
        }
    }
    
    fun addItem(title: String, description: String) {
        viewModelScope.launch {
            updateLoadingState(true, "Adding item...")
            
            try {
                val result = itemService.addItem(title, description)
                result.fold(
                    onSuccess = { items ->
                        _uiState.value = _uiState.value.copy(
                            items = items,
                            isLoading = false,
                            error = null,
                            operationStatus = "Item '$title' added successfully"
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = exception.message,
                            operationStatus = "Failed to add item: ${exception.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message,
                    operationStatus = "Error adding item: ${e.message}"
                )
            }
        }
    }
    
    fun removeItem(itemId: String) {
        viewModelScope.launch {
            val itemToRemove = _uiState.value.items.find { it.id == itemId }
            updateLoadingState(true, "Removing item...")
            
            try {
                val result = itemService.removeItem(itemId)
                result.fold(
                    onSuccess = { items ->
                        _uiState.value = _uiState.value.copy(
                            items = items,
                            isLoading = false,
                            error = null,
                            operationStatus = "Item '${itemToRemove?.title ?: itemId}' removed successfully"
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = exception.message,
                            operationStatus = "Failed to remove item: ${exception.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message,
                    operationStatus = "Error removing item: ${e.message}"
                )
            }
        }
    }
    
    fun updateItem(item: Item) {
        viewModelScope.launch {
            updateLoadingState(true, "Updating item...")
            
            try {
                val result = itemService.updateItem(item)
                result.fold(
                    onSuccess = { items ->
                        _uiState.value = _uiState.value.copy(
                            items = items,
                            isLoading = false,
                            error = null,
                            operationStatus = "Item '${item.title}' updated successfully"
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = exception.message,
                            operationStatus = "Failed to update item: ${exception.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message,
                    operationStatus = "Error updating item: ${e.message}"
                )
            }
        }
    }
    
    fun refreshItems() {
        viewModelScope.launch {
            updateLoadingState(true, "Refreshing items...")
            
            try {
                val result = itemService.refreshItems()
                result.fold(
                    onSuccess = { items ->
                        _uiState.value = _uiState.value.copy(
                            items = items,
                            isLoading = false,
                            error = null,
                            operationStatus = "Items refreshed successfully (${items.size} items)"
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = exception.message,
                            operationStatus = "Failed to refresh items: ${exception.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message,
                    operationStatus = "Error refreshing items: ${e.message}"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun clearOperationStatus() {
        _uiState.value = _uiState.value.copy(operationStatus = null)
    }
    
    private fun updateLoadingState(isLoading: Boolean, status: String? = null) {
        _uiState.value = _uiState.value.copy(
            isLoading = isLoading,
            error = null,
            operationStatus = status
        )
    }
    
    companion object {
        fun create(): Agent05ViewModel {
            val container = DependencyProvider.getContainer()
            return container.provideViewModel()
        }
    }
}