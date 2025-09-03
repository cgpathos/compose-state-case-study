package today.pathos.myapplication.study.agent05.result002

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import today.pathos.myapplication.study.common.BaseViewModel
import today.pathos.myapplication.study.common.Item
import today.pathos.myapplication.study.common.ScreenUiState

// Combined UI state using delegate pattern
data class Agent05UiState(
    val screenState: ScreenUiState = ScreenUiState.Initializing,
    val items: List<Item> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class Agent05ViewModel(
    private val stateHolder: ItemStateHolder = ItemStateHolderImpl(),
    private val operationDelegate: ItemOperationDelegate = ItemOperationDelegateImpl()
) : BaseViewModel() {
    
    private val _screenState = MutableStateFlow<ScreenUiState>(ScreenUiState.Initializing)
    
    // Combine screen state with delegate states
    val uiState: StateFlow<Agent05UiState> = combine(
        _screenState,
        stateHolder.items,
        stateHolder.isLoading,
        stateHolder.error
    ) { screenState: ScreenUiState, items: List<Item>, isLoading: Boolean, error: String? ->
        Agent05UiState(
            screenState = screenState,
            items = items,
            isLoading = isLoading,
            error = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = Agent05UiState(
            screenState = ScreenUiState.Initializing,
            items = emptyList(),
            isLoading = false,
            error = null
        )
    )
    
    init {
        loadInitialItems()
    }
    
    private fun loadInitialItems() {
        viewModelScope.launch {
            try {
                val result = operationDelegate.loadInitialItems()
                result.fold(
                    onSuccess = { items ->
                        stateHolder.updateItems(items)
                        _screenState.value = ScreenUiState.Succeed
                    },
                    onFailure = { exception ->
                        stateHolder.setError(exception.message)
                        _screenState.value = ScreenUiState.Failed(exception.message ?: "Unknown error")
                    }
                )
            } catch (e: Exception) {
                stateHolder.setError(e.message)
                _screenState.value = ScreenUiState.Failed(e.message ?: "Unknown error")
            }
        }
    }
    
    fun addItem(title: String, description: String) {
        viewModelScope.launch {
            stateHolder.setLoading(true)
            stateHolder.clearError()
            
            try {
                val result = operationDelegate.addNewItem(title, description, stateHolder.items.value)
                result.fold(
                    onSuccess = { newItem ->
                        stateHolder.addItem(newItem)
                        stateHolder.setLoading(false)
                    },
                    onFailure = { exception ->
                        stateHolder.setError(exception.message)
                        stateHolder.setLoading(false)
                    }
                )
            } catch (e: Exception) {
                stateHolder.setError(e.message)
                stateHolder.setLoading(false)
            }
        }
    }
    
    fun removeItem(itemId: String) {
        viewModelScope.launch {
            stateHolder.setLoading(true)
            stateHolder.clearError()
            
            try {
                val result = operationDelegate.performOperation {
                    stateHolder.removeItem(itemId)
                }
                result.fold(
                    onSuccess = {
                        stateHolder.setLoading(false)
                    },
                    onFailure = { exception ->
                        stateHolder.setError(exception.message)
                        stateHolder.setLoading(false)
                    }
                )
            } catch (e: Exception) {
                stateHolder.setError(e.message)
                stateHolder.setLoading(false)
            }
        }
    }
    
    fun updateItem(item: Item) {
        viewModelScope.launch {
            stateHolder.setLoading(true)
            stateHolder.clearError()
            
            try {
                val result = operationDelegate.performOperation {
                    stateHolder.updateItem(item)
                }
                result.fold(
                    onSuccess = {
                        stateHolder.setLoading(false)
                    },
                    onFailure = { exception ->
                        stateHolder.setError(exception.message)
                        stateHolder.setLoading(false)
                    }
                )
            } catch (e: Exception) {
                stateHolder.setError(e.message)
                stateHolder.setLoading(false)
            }
        }
    }
    
    fun refreshItems() {
        viewModelScope.launch {
            stateHolder.setLoading(true)
            stateHolder.clearError()
            
            try {
                val result = operationDelegate.refreshItems()
                result.fold(
                    onSuccess = { items ->
                        stateHolder.updateItems(items)
                        stateHolder.setLoading(false)
                    },
                    onFailure = { exception ->
                        stateHolder.setError(exception.message)
                        stateHolder.setLoading(false)
                    }
                )
            } catch (e: Exception) {
                stateHolder.setError(e.message)
                stateHolder.setLoading(false)
            }
        }
    }
    
    fun clearError() {
        stateHolder.clearError()
    }
}