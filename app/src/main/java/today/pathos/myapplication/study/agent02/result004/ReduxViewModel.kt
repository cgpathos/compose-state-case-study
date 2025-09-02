package today.pathos.myapplication.study.agent02.result004

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import today.pathos.myapplication.study.common.BaseViewModel
import today.pathos.myapplication.study.common.Item
import today.pathos.myapplication.study.common.ScreenUiState

class ReduxViewModel : BaseViewModel() {
    
    private val store = ReduxStore(viewModelScope)
    
    private val _screenUiState = MutableStateFlow<ScreenUiState>(ScreenUiState.Initializing)
    val screenUiState: StateFlow<ScreenUiState> = _screenUiState.asStateFlow()
    
    val reduxState: StateFlow<ReduxState> = store.state
    
    init {
        // Monitor Redux state changes for screen state updates
        viewModelScope.launch {
            store.state.collect { state ->
                when {
                    !state.isInitialized && !state.isLoading && state.error == null -> {
                        // Initial state - start loading
                        _screenUiState.value = ScreenUiState.Initializing
                    }
                    state.isInitialized && state.error == null -> {
                        _screenUiState.value = ScreenUiState.Succeed
                    }
                    state.error != null && !state.isInitialized -> {
                        _screenUiState.value = ScreenUiState.Failed(state.error)
                    }
                }
            }
        }
        
        // Start initial load
        dispatch(ReduxAction.LoadItems)
    }
    
    fun dispatch(action: ReduxAction) {
        store.dispatch(action)
    }
    
    fun loadItems() {
        dispatch(ReduxAction.LoadItems)
    }
    
    fun refreshItems() {
        dispatch(ReduxAction.RefreshItems)
    }
    
    fun addItem(item: Item) {
        dispatch(ReduxAction.AddItem(item))
    }
    
    fun removeItem(itemId: String) {
        dispatch(ReduxAction.RemoveItem(itemId))
    }
    
    fun updateItem(item: Item) {
        dispatch(ReduxAction.UpdateItem(item))
    }
    
    fun clearError() {
        dispatch(ReduxAction.ClearError)
    }
}