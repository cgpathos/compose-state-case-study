package today.pathos.myapplication.study.agent03.result003

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
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
    
    // State management using channels
    private val stateChannel = Channel<ActorMessage>(Channel.UNLIMITED)
    private val operationChannel = Channel<ActorMessage>(Channel.UNLIMITED)
    
    init {
        initializeActors()
        initializeOperationProcessor()
        setupNotificationHandler()
        triggerInitialLoad()
    }
    
    private fun initializeActors() {
        // State management coroutine
        viewModelScope.launch {
            var currentItems = emptyList<Item>()
            var isLoading = false
            var currentError: String? = null
            
            for (message in stateChannel) {
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
                        operationChannel.send(ActorMessage.InitialLoad)
                    }
                    is ActorMessage.Refresh -> {
                        operationChannel.send(ActorMessage.Refresh)
                    }
                    is ActorMessage.AddItem -> {
                        operationChannel.send(ActorMessage.AddItem(message.item))
                    }
                    is ActorMessage.RemoveItem -> {
                        operationChannel.send(ActorMessage.RemoveItem(message.itemId))
                    }
                    is ActorMessage.UpdateItem -> {
                        operationChannel.send(ActorMessage.UpdateItem(message.item))
                    }
                }
            }
        }
    }
    
    private fun initializeOperationProcessor() {
        // Operation processing coroutine
        viewModelScope.launch {
            for (message in operationChannel) {
                try {
                    // Set loading state
                    stateChannel.send(ActorMessage.SetLoading(true))
                    stateChannel.send(ActorMessage.SetError(null))
                
                when (message) {
                    is ActorMessage.InitialLoad -> {
                        delay(2000) // Simulate network delay
                        if (shouldSimulateError()) {
                            throw Exception("Initial load failed")
                        }
                        val items = generateInitialItems()
                        stateChannel.send(ActorMessage.SetItems(items))
                        updateScreenStateAfterInitialLoad(null)
                    }
                    is ActorMessage.Refresh -> {
                        delay(1000) // Simulate refresh delay
                        if (shouldSimulateError()) {
                            throw Exception("Refresh failed")
                        }
                        val items = generateInitialItems()
                        stateChannel.send(ActorMessage.SetItems(items))
                    }
                    is ActorMessage.AddItem -> {
                        delay(200) // Simulate operation delay
                        if (shouldSimulateError()) {
                            throw Exception("Add operation failed")
                        }
                        val currentItems = _itemsFlow.value.toMutableList()
                        currentItems.add(message.item)
                        stateChannel.send(ActorMessage.SetItems(currentItems))
                    }
                    is ActorMessage.RemoveItem -> {
                        delay(200) // Simulate operation delay
                        if (shouldSimulateError()) {
                            throw Exception("Remove operation failed")
                        }
                        val currentItems = _itemsFlow.value.toMutableList()
                        currentItems.removeAll { it.id == message.itemId }
                        stateChannel.send(ActorMessage.SetItems(currentItems))
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
                        stateChannel.send(ActorMessage.SetItems(currentItems))
                    }
                    else -> { /* Other messages handled by state actor */ }
                }
                
                // Clear loading state
                stateChannel.send(ActorMessage.SetLoading(false))
                
            } catch (e: Exception) {
                stateChannel.send(ActorMessage.SetError(e.message))
                stateChannel.send(ActorMessage.SetLoading(false))
                
                // Handle initial load failure
                if (message is ActorMessage.InitialLoad) {
                    updateScreenStateAfterInitialLoad(e.message)
                }
            }
        }
    }
    }
    
    // Notification channels for inter-actor communication
    private val notificationChannel = Channel<String>(capacity = Channel.UNLIMITED)
    val notifications = notificationChannel.receiveAsFlow()
    
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
            stateChannel.send(ActorMessage.InitialLoad)
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
            stateChannel.send(ActorMessage.Refresh)
            notificationChannel.send("Refresh requested")
        }
    }
    
    fun addItem() {
        viewModelScope.launch {
            val newItem = generateNewItem(_itemsFlow.value.size)
            stateChannel.send(ActorMessage.AddItem(newItem))
            notificationChannel.send("Add item: ${newItem.id}")
        }
    }
    
    fun removeItem(itemId: String) {
        viewModelScope.launch {
            stateChannel.send(ActorMessage.RemoveItem(itemId))
            notificationChannel.send("Remove item: $itemId")
        }
    }
    
    fun updateItem(item: Item) {
        viewModelScope.launch {
            val updatedItem = item.copy(
                title = "${item.title} (Updated)",
                timestamp = System.currentTimeMillis()
            )
            stateChannel.send(ActorMessage.UpdateItem(updatedItem))
            notificationChannel.send("Update item: ${item.id}")
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        stateChannel.close()
        operationChannel.close()
        notificationChannel.close()
    }
}