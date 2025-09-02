package today.pathos.myapplication.study.agent05.result004

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import today.pathos.myapplication.study.common.BaseViewModel
import today.pathos.myapplication.study.common.Item
import today.pathos.myapplication.study.common.ScreenUiState

data class Agent05UiState(
    val screenState: ScreenUiState = ScreenUiState.Initializing,
    val items: List<Item> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val lastStateChange: String? = null
)

class Agent05ViewModel(
    private val eventBus: EventBus = EventBusImpl(),
    private val stateStore: ItemStateStore = ItemStateStoreImpl()
) : BaseViewModel() {
    
    private val _screenState = MutableStateFlow<ScreenUiState>(ScreenUiState.Initializing)
    private val _lastStateChange = MutableStateFlow<String?>(null)
    
    // Combine state flows from store and local state
    val uiState: StateFlow<Agent05UiState> = combine(
        _screenState,
        stateStore.items,
        stateStore.isLoading,
        stateStore.error,
        _lastStateChange
    ) { screenState, items, isLoading, error, lastStateChange ->
        Agent05UiState(
            screenState = screenState,
            items = items,
            isLoading = isLoading,
            error = error,
            lastStateChange = lastStateChange
        )
    }
    
    init {
        setupEventBus()
        loadInitialItems()
    }
    
    private fun setupEventBus() {
        // Start the state store listening to events
        stateStore.startListening(eventBus)
        
        // Listen to state changes for UI feedback
        viewModelScope.launch {
            eventBus.stateChanges.collect { stateChange ->
                handleStateChange(stateChange)
            }
        }
    }
    
    private fun handleStateChange(stateChange: ItemStateChange) {
        when (stateChange) {
            is ItemStateChange.LoadingStarted -> {
                _lastStateChange.value = "Loading started..."
            }
            is ItemStateChange.LoadingFinished -> {
                _lastStateChange.value = "Loading finished"
            }
            is ItemStateChange.ItemsLoaded -> {
                _screenState.value = ScreenUiState.Succeed
                _lastStateChange.value = "Items loaded: ${stateChange.items.size} items"
            }
            is ItemStateChange.ItemAdded -> {
                _lastStateChange.value = "Item added: ${stateChange.item.title}"
            }
            is ItemStateChange.ItemRemoved -> {
                _lastStateChange.value = "Item removed: ${stateChange.itemId}"
            }
            is ItemStateChange.ItemUpdated -> {
                _lastStateChange.value = "Item updated: ${stateChange.item.title}"
            }
            is ItemStateChange.ErrorOccurred -> {
                if (_screenState.value == ScreenUiState.Initializing) {
                    _screenState.value = ScreenUiState.Failed(stateChange.error)
                }
                _lastStateChange.value = "Error: ${stateChange.error}"
            }
            is ItemStateChange.ErrorCleared -> {
                _lastStateChange.value = "Error cleared"
            }
            is ItemStateChange.OperationCompleted -> {
                _lastStateChange.value = stateChange.message
            }
        }
    }
    
    private fun loadInitialItems() {
        viewModelScope.launch {
            eventBus.publishEvent(ItemEvent.LoadItems)
        }
    }
    
    fun addItem(title: String, description: String) {
        viewModelScope.launch {
            eventBus.publishEvent(ItemEvent.AddItem(title, description))
        }
    }
    
    fun removeItem(itemId: String) {
        viewModelScope.launch {
            eventBus.publishEvent(ItemEvent.RemoveItem(itemId))
        }
    }
    
    fun updateItem(item: Item) {
        viewModelScope.launch {
            eventBus.publishEvent(ItemEvent.UpdateItem(item))
        }
    }
    
    fun refreshItems() {
        viewModelScope.launch {
            eventBus.publishEvent(ItemEvent.RefreshItems)
        }
    }
    
    fun clearError() {
        viewModelScope.launch {
            eventBus.publishEvent(ItemEvent.ClearError)
        }
    }
    
    fun clearLastStateChange() {
        _lastStateChange.value = null
    }
    
    override fun onCleared() {
        super.onCleared()
        stateStore.stopListening()
    }
}