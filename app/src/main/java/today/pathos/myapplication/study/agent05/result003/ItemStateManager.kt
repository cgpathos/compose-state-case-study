package today.pathos.myapplication.study.agent05.result003

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import today.pathos.myapplication.study.common.Item
import kotlin.random.Random

// Singleton state manager for centralized state management
class ItemStateManager private constructor() {
    
    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    // State flows
    private val _items = MutableStateFlow<List<Item>>(emptyList())
    val items: StateFlow<List<Item>> = _items.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // Event flow for one-time events
    private val _events = MutableSharedFlow<StateEvent>()
    val events = _events.asSharedFlow()
    
    sealed class StateEvent {
        object InitializationStarted : StateEvent()
        object InitializationCompleted : StateEvent()
        data class InitializationFailed(val error: String) : StateEvent()
        data class OperationCompleted(val message: String) : StateEvent()
        data class OperationFailed(val error: String) : StateEvent()
    }
    
    companion object {
        @Volatile
        private var INSTANCE: ItemStateManager? = null
        
        fun getInstance(): ItemStateManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ItemStateManager().also { INSTANCE = it }
            }
        }
        
        // For testing purposes - allows resetting the singleton
        fun resetInstance() {
            INSTANCE = null
        }
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
    
    fun initializeItems() {
        managerScope.launch {
            _isLoading.value = true
            _error.value = null
            _events.emit(StateEvent.InitializationStarted)
            
            try {
                delay(2000) // 2s delay for initial load
                
                if (shouldSimulateError()) {
                    val errorMsg = "Failed to initialize items"
                    _error.value = errorMsg
                    _events.emit(StateEvent.InitializationFailed(errorMsg))
                } else {
                    val items = generateInitialItems()
                    _items.value = items
                    _events.emit(StateEvent.InitializationCompleted)
                }
            } catch (e: Exception) {
                _error.value = e.message
                _events.emit(StateEvent.InitializationFailed(e.message ?: "Unknown error"))
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun addItem(title: String, description: String) {
        managerScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                if (shouldSimulateError()) {
                    val errorMsg = "Failed to add item"
                    _error.value = errorMsg
                    _events.emit(StateEvent.OperationFailed(errorMsg))
                } else {
                    val currentItems = _items.value.toMutableList()
                    val newId = (currentItems.size + 1).toString()
                    val newItem = Item(
                        id = "item_$newId",
                        title = title,
                        description = description
                    )
                    currentItems.add(newItem)
                    _items.value = currentItems
                    _events.emit(StateEvent.OperationCompleted("Item added successfully"))
                }
            } catch (e: Exception) {
                _error.value = e.message
                _events.emit(StateEvent.OperationFailed(e.message ?: "Unknown error"))
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun removeItem(itemId: String) {
        managerScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                if (shouldSimulateError()) {
                    val errorMsg = "Failed to remove item"
                    _error.value = errorMsg
                    _events.emit(StateEvent.OperationFailed(errorMsg))
                } else {
                    val currentItems = _items.value.toMutableList()
                    currentItems.removeAll { it.id == itemId }
                    _items.value = currentItems
                    _events.emit(StateEvent.OperationCompleted("Item removed successfully"))
                }
            } catch (e: Exception) {
                _error.value = e.message
                _events.emit(StateEvent.OperationFailed(e.message ?: "Unknown error"))
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateItem(item: Item) {
        managerScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                if (shouldSimulateError()) {
                    val errorMsg = "Failed to update item"
                    _error.value = errorMsg
                    _events.emit(StateEvent.OperationFailed(errorMsg))
                } else {
                    val currentItems = _items.value.toMutableList()
                    val index = currentItems.indexOfFirst { it.id == item.id }
                    if (index != -1) {
                        currentItems[index] = item
                        _items.value = currentItems
                        _events.emit(StateEvent.OperationCompleted("Item updated successfully"))
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message
                _events.emit(StateEvent.OperationFailed(e.message ?: "Unknown error"))
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun refreshItems() {
        managerScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                delay(1000) // 1s delay for refresh
                
                if (shouldSimulateError()) {
                    val errorMsg = "Failed to refresh items"
                    _error.value = errorMsg
                    _events.emit(StateEvent.OperationFailed(errorMsg))
                } else {
                    val items = generateInitialItems()
                    _items.value = items
                    _events.emit(StateEvent.OperationCompleted("Items refreshed successfully"))
                }
            } catch (e: Exception) {
                _error.value = e.message
                _events.emit(StateEvent.OperationFailed(e.message ?: "Unknown error"))
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}