package today.pathos.myapplication.study.agent03.result003

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import today.pathos.myapplication.study.common.BaseViewModel
import today.pathos.myapplication.study.common.Item
import today.pathos.myapplication.study.common.ScreenUiState

class ActorModelViewModel : BaseViewModel() {
    
    // Initial screen state (only used for initialization)
    var screenUiState by mutableStateOf<ScreenUiState>(ScreenUiState.Initializing)
        private set
    
    // State flows for reactive UI
    private val _itemsFlow = MutableStateFlow<List<Item>>(emptyList())
    val itemsFlow: StateFlow<List<Item>> = _itemsFlow.asStateFlow()
    
    private val _isLoadingFlow = MutableStateFlow(false)
    val isLoadingFlow: StateFlow<Boolean> = _isLoadingFlow.asStateFlow()
    
    private val _errorFlow = MutableStateFlow<String?>(null)
    val errorFlow: StateFlow<String?> = _errorFlow.asStateFlow()
    
    // Actor messages
    private sealed class ActorMessage {
        object InitialLoad : ActorMessage()
        object Refresh : ActorMessage()
        data class AddItem(val item: Item) : ActorMessage()
        data class RemoveItem(val itemId: String) : ActorMessage()
        data class UpdateItem(val item: Item) : ActorMessage()
        data class SetLoading(val isLoading: Boolean) : ActorMessage()
        data class SetError(val error: String?) : ActorMessage()
        data class SetItems(val items: List<Item>) : ActorMessage()
    }
    
    // State management actor
    private val stateActor = viewModelScope.actor<ActorMessage>(capacity = Channel.UNLIMITED) {
        var currentItems = emptyList<Item>()
        var isLoading = false
        var currentError: String? = null
        
        for (message in channel) {
            when (message) {
                is ActorMessage.SetItems -> {
                    currentItems = message.items
                    _itemsFlow.value = currentItems
                }
                is ActorMessage.SetLoading -> {
                    isLoading = message.isLoading
                    _isLoadingFlow.value = isLoading
                }
                is ActorMessage.SetError -> {
                    currentError = message.error
                    _errorFlow.value = currentError
                }
                is ActorMessage.InitialLoad -> {
                    // Delegate to operation actor
                    operationActor.send(ActorMessage.InitialLoad)
                }
                is ActorMessage.Refresh -> {
                    operationActor.send(ActorMessage.Refresh)
                }
                is ActorMessage.AddItem -> {
                    operationActor.send(ActorMessage.AddItem(message.item))
                }
                is ActorMessage.RemoveItem -> {
                    operationActor.send(ActorMessage.RemoveItem(message.itemId))
                }
                is ActorMessage.UpdateItem -> {
                    operationActor.send(ActorMessage.UpdateItem(message.item))
                }
            }
        }
    }
    
    // Operation processing actor
    private val operationActor = viewModelScope.actor<ActorMessage>(capacity = Channel.UNLIMITED) {
        for (message in channel) {
            try {
                // Set loading state
                stateActor.send(ActorMessage.SetLoading(true))
                stateActor.send(ActorMessage.SetError(null))
                
                when (message) {
                    is ActorMessage.InitialLoad -> {
                        delay(2000) // Simulate network delay
                        if (shouldSimulateError()) {
                            throw Exception("Initial load failed")
                        }
                        val items = generateInitialItems()
                        stateActor.send(ActorMessage.SetItems(items))
                        updateScreenStateAfterInitialLoad(null)
                    }
                    is ActorMessage.Refresh -> {
                        delay(1000) // Simulate refresh delay
                        if (shouldSimulateError()) {
                            throw Exception("Refresh failed")
                        }
                        val items = generateInitialItems()
                        stateActor.send(ActorMessage.SetItems(items))
                    }
                    is ActorMessage.AddItem -> {
                        delay(200) // Simulate operation delay
                        if (shouldSimulateError()) {
                            throw Exception("Add operation failed")
                        }
                        val currentItems = _itemsFlow.value.toMutableList()
                        currentItems.add(message.item)
                        stateActor.send(ActorMessage.SetItems(currentItems))
                    }
                    is ActorMessage.RemoveItem -> {
                        delay(200) // Simulate operation delay
                        if (shouldSimulateError()) {
                            throw Exception("Remove operation failed")
                        }
                        val currentItems = _itemsFlow.value.toMutableList()
                        currentItems.removeAll { it.id == message.itemId }
                        stateActor.send(ActorMessage.SetItems(currentItems))
                    }
                    is ActorMessage.UpdateItem -> {
                        delay(200) // Simulate operation delay
                        if (shouldSimulateError()) {
                            throw Exception("Update operation failed")
                        }
                        val currentItems = _itemsFlow.value.toMutableList()
                        val index = currentItems.indexOfFirst { it.id == message.item.id }
                        if (index >= 0) {
                            currentItems[index] = message.item
                        }
                        stateActor.send(ActorMessage.SetItems(currentItems))
                    }
                    else -> { /* Other messages handled by state actor */ }
                }
                
                // Clear loading state
                stateActor.send(ActorMessage.SetLoading(false))
                
            } catch (e: Exception) {
                stateActor.send(ActorMessage.SetError(e.message))
                stateActor.send(ActorMessage.SetLoading(false))
                
                // Handle initial load failure
                if (message is ActorMessage.InitialLoad) {
                    updateScreenStateAfterInitialLoad(e.message)
                }
            }
        }
    }
    
    // Notification channels for inter-actor communication
    private val notificationChannel = Channel<String>(capacity = Channel.UNLIMITED)
    val notifications = notificationChannel.receiveAsFlow()
    
    init {
        setupNotificationHandler()
        triggerInitialLoad()
    }
    
    private fun setupNotificationHandler() {
        viewModelScope.launch {
            notifications.collect { notification ->
                // Handle notifications from actors if needed
                println("Actor notification: $notification")
            }
        }
    }
    
    private fun triggerInitialLoad() {
        viewModelScope.launch {
            stateActor.send(ActorMessage.InitialLoad)
        }
    }
    
    private fun updateScreenStateAfterInitialLoad(errorMessage: String?) {
        viewModelScope.launch {
            screenUiState = if (errorMessage != null) {
                ScreenUiState.Failed(errorMessage)
            } else {
                ScreenUiState.Succeed
            }
        }
    }
    
    fun refresh() {
        viewModelScope.launch {
            stateActor.send(ActorMessage.Refresh)
            notificationChannel.send("Refresh requested")
        }
    }
    
    fun addItem() {
        viewModelScope.launch {
            val newItem = generateNewItem(_itemsFlow.value.size)
            stateActor.send(ActorMessage.AddItem(newItem))
            notificationChannel.send("Add item: ${newItem.id}")
        }
    }
    
    fun removeItem(itemId: String) {
        viewModelScope.launch {
            stateActor.send(ActorMessage.RemoveItem(itemId))
            notificationChannel.send("Remove item: $itemId")
        }
    }
    
    fun updateItem(item: Item) {
        viewModelScope.launch {
            val updatedItem = item.copy(
                title = "${item.title} (Updated)",
                timestamp = System.currentTimeMillis()
            )
            stateActor.send(ActorMessage.UpdateItem(updatedItem))
            notificationChannel.send("Update item: ${item.id}")
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        stateActor.close()
        operationActor.close()
        notificationChannel.close()
    }
}