package today.pathos.myapplication.study.agent01.result005

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
    
    private val _actions = MutableSharedFlow<Action>(extraBufferCapacity = 10)
    private val _signals = MutableSharedFlow<Signal>(extraBufferCapacity = 10)
    
    val signals: SharedFlow<Signal> = _signals.asSharedFlow()
    
    // Molecule-like reactive state derivation
    val state: StateFlow<MoleculeState> = _signals
        .scan(MoleculeState()) { currentState, signal ->
            when (signal) {
                is Signal.ItemsLoaded -> currentState.copy(
                    items = signal.items,
                    isLoading = false,
                    error = null
                )
                
                is Signal.ItemAdded -> currentState.copy(
                    items = currentState.items + signal.item,
                    isLoading = false,
                    error = null
                )
                
                is Signal.ItemRemoved -> currentState.copy(
                    items = currentState.items.filter { it.id != signal.itemId },
                    isLoading = false,
                    error = null
                )
                
                is Signal.ItemUpdated -> currentState.copy(
                    items = currentState.items.map { item ->
                        if (item.id == signal.item.id) signal.item else item
                    },
                    isLoading = false,
                    error = null
                )
                
                is Signal.ItemsRefreshed -> currentState.copy(
                    items = signal.items,
                    isRefreshing = false,
                    error = null
                )
                
                is Signal.Error -> currentState.copy(
                    error = signal.message,
                    isLoading = false,
                    isRefreshing = false
                )
                
                is Signal.LoadingStateChanged -> currentState.copy(
                    isLoading = signal.isLoading,
                    error = if (signal.isLoading) null else currentState.error
                )
                
                is Signal.RefreshingStateChanged -> currentState.copy(
                    isRefreshing = signal.isRefreshing,
                    error = if (signal.isRefreshing) null else currentState.error
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = MoleculeState()
        )
    
    init {
        collectActions()
        initializeScreen()
    }
    
    private fun collectActions() {
        viewModelScope.launch {
            _actions.collect { action ->
                when (action) {
                    is Action.LoadItems -> handleLoadItems()
                    is Action.AddItem -> handleAddItem()
                    is Action.RemoveItem -> handleRemoveItem(action.itemId)
                    is Action.UpdateItem -> handleUpdateItem(action.item)
                    is Action.Refresh -> handleRefresh()
                    is Action.ClearError -> handleClearError()
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
                _signals.emit(Signal.ItemsLoaded(initialItems))
            } catch (e: Exception) {
                _screenUiState.value = ScreenUiState.Failed(e.message ?: "Unknown error")
            }
        }
    }
    
    private suspend fun handleLoadItems() {
        _signals.emit(Signal.LoadingStateChanged(true))
        delay(1000)
        
        try {
            if (shouldSimulateError()) {
                _signals.emit(Signal.Error("Failed to load items"))
                return
            }
            
            val items = generateInitialItems()
            _signals.emit(Signal.ItemsLoaded(items))
        } catch (e: Exception) {
            _signals.emit(Signal.Error(e.message ?: "Unknown error"))
        }
    }
    
    private suspend fun handleAddItem() {
        _signals.emit(Signal.LoadingStateChanged(true))
        delay(1000)
        
        try {
            if (shouldSimulateError()) {
                _signals.emit(Signal.Error("Failed to add item"))
                return
            }
            
            val currentItems = state.value.items
            val newItem = generateNewItem(currentItems.size)
            _signals.emit(Signal.ItemAdded(newItem))
        } catch (e: Exception) {
            _signals.emit(Signal.Error(e.message ?: "Unknown error"))
        }
    }
    
    private suspend fun handleRemoveItem(itemId: String) {
        _signals.emit(Signal.LoadingStateChanged(true))
        delay(500)
        
        try {
            if (shouldSimulateError()) {
                _signals.emit(Signal.Error("Failed to remove item"))
                return
            }
            
            _signals.emit(Signal.ItemRemoved(itemId))
        } catch (e: Exception) {
            _signals.emit(Signal.Error(e.message ?: "Unknown error"))
        }
    }
    
    private suspend fun handleUpdateItem(updatedItem: Item) {
        _signals.emit(Signal.LoadingStateChanged(true))
        delay(500)
        
        try {
            if (shouldSimulateError()) {
                _signals.emit(Signal.Error("Failed to update item"))
                return
            }
            
            _signals.emit(Signal.ItemUpdated(updatedItem))
        } catch (e: Exception) {
            _signals.emit(Signal.Error(e.message ?: "Unknown error"))
        }
    }
    
    private suspend fun handleRefresh() {
        _signals.emit(Signal.RefreshingStateChanged(true))
        delay(1500)
        
        try {
            if (shouldSimulateError()) {
                _signals.emit(Signal.Error("Failed to refresh"))
                return
            }
            
            val refreshedItems = generateInitialItems()
            _signals.emit(Signal.ItemsRefreshed(refreshedItems))
        } catch (e: Exception) {
            _signals.emit(Signal.Error(e.message ?: "Unknown error"))
        }
    }
    
    private suspend fun handleClearError() {
        // For clear error, we could emit a specific signal or just update loading states
        _signals.emit(Signal.LoadingStateChanged(false))
        _signals.emit(Signal.RefreshingStateChanged(false))
    }
    
    // Action triggers
    fun addItem() {
        viewModelScope.launch {
            _actions.emit(Action.AddItem)
        }
    }
    
    fun removeItem(itemId: String) {
        viewModelScope.launch {
            _actions.emit(Action.RemoveItem(itemId))
        }
    }
    
    fun updateItem(updatedItem: Item) {
        viewModelScope.launch {
            _actions.emit(Action.UpdateItem(updatedItem))
        }
    }
    
    fun refresh() {
        viewModelScope.launch {
            _actions.emit(Action.Refresh)
        }
    }
    
    fun clearError() {
        viewModelScope.launch {
            _actions.emit(Action.ClearError)
        }
    }
}