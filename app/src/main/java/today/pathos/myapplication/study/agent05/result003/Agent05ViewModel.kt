package today.pathos.myapplication.study.agent05.result003

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
    val lastEvent: String? = null
)

class Agent05ViewModel(
    private val stateManager: ItemStateManager = ItemStateManager.getInstance()
) : BaseViewModel() {
    
    private val _screenState = MutableStateFlow<ScreenUiState>(ScreenUiState.Initializing)
    private val _lastEvent = MutableStateFlow<String?>(null)
    
    // Combine all state flows from the singleton state manager
    val uiState: StateFlow<Agent05UiState> = combine(
        _screenState,
        stateManager.items,
        stateManager.isLoading,
        stateManager.error,
        _lastEvent
    ) { screenState, items, isLoading, error, lastEvent ->
        Agent05UiState(
            screenState = screenState,
            items = items,
            isLoading = isLoading,
            error = error,
            lastEvent = lastEvent
        )
    }
    
    init {
        observeStateManagerEvents()
        initializeItems()
    }
    
    private fun observeStateManagerEvents() {
        viewModelScope.launch {
            stateManager.events.collect { event ->
                when (event) {
                    is ItemStateManager.StateEvent.InitializationStarted -> {
                        _lastEvent.value = "Initialization started..."
                    }
                    is ItemStateManager.StateEvent.InitializationCompleted -> {
                        _screenState.value = ScreenUiState.Succeed
                        _lastEvent.value = "Initialization completed"
                    }
                    is ItemStateManager.StateEvent.InitializationFailed -> {
                        _screenState.value = ScreenUiState.Failed(event.error)
                        _lastEvent.value = "Initialization failed: ${event.error}"
                    }
                    is ItemStateManager.StateEvent.OperationCompleted -> {
                        _lastEvent.value = event.message
                    }
                    is ItemStateManager.StateEvent.OperationFailed -> {
                        _lastEvent.value = "Operation failed: ${event.error}"
                    }
                }
            }
        }
    }
    
    private fun initializeItems() {
        stateManager.initializeItems()
    }
    
    fun addItem(title: String, description: String) {
        stateManager.addItem(title, description)
    }
    
    fun removeItem(itemId: String) {
        stateManager.removeItem(itemId)
    }
    
    fun updateItem(item: Item) {
        stateManager.updateItem(item)
    }
    
    fun refreshItems() {
        stateManager.refreshItems()
    }
    
    fun clearError() {
        stateManager.clearError()
    }
    
    fun clearLastEvent() {
        _lastEvent.value = null
    }
    
    override fun onCleared() {
        super.onCleared()
        // Note: We don't clear the singleton state manager here as it might be used by other ViewModels
        // In a real app, you might want to implement reference counting or similar mechanism
    }
}