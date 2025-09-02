package today.pathos.myapplication.study.agent05.result004

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import today.pathos.myapplication.study.common.Item
import kotlin.random.Random

// State store that manages the actual state and listens to events
interface ItemStateStore {
    val items: StateFlow<List<Item>>
    val isLoading: StateFlow<Boolean>
    val error: StateFlow<String?>
    
    fun startListening(eventBus: EventBus)
    fun stopListening()
}

class ItemStateStoreImpl : ItemStateStore {
    
    private val storeScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    // State flows
    private val _items = MutableStateFlow<List<Item>>(emptyList())
    override val items: StateFlow<List<Item>> = _items.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    override val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    override val error: StateFlow<String?> = _error.asStateFlow()
    
    override fun startListening(eventBus: EventBus) {
        // Listen to events and handle them
        storeScope.launch {
            eventBus.events.collect { event ->
                handleEvent(event, eventBus)
            }
        }
    }
    
    override fun stopListening() {
        // In a real implementation, you would cancel the listening job
    }
    
    private suspend fun handleEvent(event: ItemEvent, eventBus: EventBus) {
        when (event) {
            is ItemEvent.LoadItems -> handleLoadItems(eventBus)
            is ItemEvent.AddItem -> handleAddItem(event, eventBus)
            is ItemEvent.RemoveItem -> handleRemoveItem(event, eventBus)
            is ItemEvent.UpdateItem -> handleUpdateItem(event, eventBus)
            is ItemEvent.RefreshItems -> handleRefreshItems(eventBus)
            is ItemEvent.ClearError -> handleClearError(eventBus)
        }
    }
    
    private suspend fun handleLoadItems(eventBus: EventBus) {
        _isLoading.value = true
        eventBus.publishStateChange(ItemStateChange.LoadingStarted)
        
        try {
            delay(2000) // 2s delay for initial load
            
            if (shouldSimulateError()) {
                val errorMsg = "Failed to load items"
                _error.value = errorMsg
                eventBus.publishStateChange(ItemStateChange.ErrorOccurred(errorMsg))
            } else {
                val items = generateInitialItems()
                _items.value = items
                eventBus.publishStateChange(ItemStateChange.ItemsLoaded(items))
            }
        } catch (e: Exception) {
            val errorMsg = e.message ?: "Unknown error"
            _error.value = errorMsg
            eventBus.publishStateChange(ItemStateChange.ErrorOccurred(errorMsg))
        } finally {
            _isLoading.value = false
            eventBus.publishStateChange(ItemStateChange.LoadingFinished)
        }
    }
    
    private suspend fun handleAddItem(event: ItemEvent.AddItem, eventBus: EventBus) {
        _isLoading.value = true
        eventBus.publishStateChange(ItemStateChange.LoadingStarted)
        
        try {
            if (shouldSimulateError()) {
                val errorMsg = "Failed to add item"
                _error.value = errorMsg
                eventBus.publishStateChange(ItemStateChange.ErrorOccurred(errorMsg))
            } else {
                val currentItems = _items.value.toMutableList()
                val newId = (currentItems.size + 1).toString()
                val newItem = Item(
                    id = "item_$newId",
                    title = event.title,
                    description = event.description
                )
                currentItems.add(newItem)
                _items.value = currentItems
                eventBus.publishStateChange(ItemStateChange.ItemAdded(newItem))
                eventBus.publishStateChange(ItemStateChange.OperationCompleted("Item added successfully"))
            }
        } catch (e: Exception) {
            val errorMsg = e.message ?: "Unknown error"
            _error.value = errorMsg
            eventBus.publishStateChange(ItemStateChange.ErrorOccurred(errorMsg))
        } finally {
            _isLoading.value = false
            eventBus.publishStateChange(ItemStateChange.LoadingFinished)
        }
    }
    
    private suspend fun handleRemoveItem(event: ItemEvent.RemoveItem, eventBus: EventBus) {
        _isLoading.value = true
        eventBus.publishStateChange(ItemStateChange.LoadingStarted)
        
        try {
            if (shouldSimulateError()) {
                val errorMsg = "Failed to remove item"
                _error.value = errorMsg
                eventBus.publishStateChange(ItemStateChange.ErrorOccurred(errorMsg))
            } else {
                val currentItems = _items.value.toMutableList()
                currentItems.removeAll { it.id == event.itemId }
                _items.value = currentItems
                eventBus.publishStateChange(ItemStateChange.ItemRemoved(event.itemId))
                eventBus.publishStateChange(ItemStateChange.OperationCompleted("Item removed successfully"))
            }
        } catch (e: Exception) {
            val errorMsg = e.message ?: "Unknown error"
            _error.value = errorMsg
            eventBus.publishStateChange(ItemStateChange.ErrorOccurred(errorMsg))
        } finally {
            _isLoading.value = false
            eventBus.publishStateChange(ItemStateChange.LoadingFinished)
        }
    }
    
    private suspend fun handleUpdateItem(event: ItemEvent.UpdateItem, eventBus: EventBus) {
        _isLoading.value = true
        eventBus.publishStateChange(ItemStateChange.LoadingStarted)
        
        try {
            if (shouldSimulateError()) {
                val errorMsg = "Failed to update item"
                _error.value = errorMsg
                eventBus.publishStateChange(ItemStateChange.ErrorOccurred(errorMsg))
            } else {
                val currentItems = _items.value.toMutableList()
                val index = currentItems.indexOfFirst { it.id == event.item.id }
                if (index != -1) {
                    currentItems[index] = event.item
                    _items.value = currentItems
                    eventBus.publishStateChange(ItemStateChange.ItemUpdated(event.item))
                    eventBus.publishStateChange(ItemStateChange.OperationCompleted("Item updated successfully"))
                }
            }
        } catch (e: Exception) {
            val errorMsg = e.message ?: "Unknown error"
            _error.value = errorMsg
            eventBus.publishStateChange(ItemStateChange.ErrorOccurred(errorMsg))
        } finally {
            _isLoading.value = false
            eventBus.publishStateChange(ItemStateChange.LoadingFinished)
        }
    }
    
    private suspend fun handleRefreshItems(eventBus: EventBus) {
        _isLoading.value = true
        eventBus.publishStateChange(ItemStateChange.LoadingStarted)
        
        try {
            delay(1000) // 1s delay for refresh
            
            if (shouldSimulateError()) {
                val errorMsg = "Failed to refresh items"
                _error.value = errorMsg
                eventBus.publishStateChange(ItemStateChange.ErrorOccurred(errorMsg))
            } else {
                val items = generateInitialItems()
                _items.value = items
                eventBus.publishStateChange(ItemStateChange.ItemsLoaded(items))
                eventBus.publishStateChange(ItemStateChange.OperationCompleted("Items refreshed successfully"))
            }
        } catch (e: Exception) {
            val errorMsg = e.message ?: "Unknown error"
            _error.value = errorMsg
            eventBus.publishStateChange(ItemStateChange.ErrorOccurred(errorMsg))
        } finally {
            _isLoading.value = false
            eventBus.publishStateChange(ItemStateChange.LoadingFinished)
        }
    }
    
    private suspend fun handleClearError(eventBus: EventBus) {
        _error.value = null
        eventBus.publishStateChange(ItemStateChange.ErrorCleared)
    }
    
    private fun shouldSimulateError(): Boolean {
        return Random.nextDouble() < 0.2 // 20% chance of error
    }
    
    private fun generateInitialItems(): List<Item> {
        return (1..5).map { index ->
            Item(
                id = "item_$index",
                title = "Item $index",
                description = "Description for item $index"
            )
        }
    }
}