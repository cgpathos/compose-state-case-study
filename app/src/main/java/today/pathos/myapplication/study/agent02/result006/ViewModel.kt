package today.pathos.myapplication.study.agent02.result006

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import today.pathos.myapplication.study.common.BaseViewModel
import today.pathos.myapplication.study.common.Item
import today.pathos.myapplication.study.common.ScreenUiState

class CommandViewModel : BaseViewModel() {
    
    private val _screenUiState = MutableStateFlow<ScreenUiState>(ScreenUiState.Initializing)
    val screenUiState: StateFlow<ScreenUiState> = _screenUiState.asStateFlow()
    
    private val _items = MutableStateFlow<List<Item>>(emptyList())
    val items: StateFlow<List<Item>> = _items.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val commandInvoker = CommandInvoker(viewModelScope)
    
    // Manual initialization method - NO init{} block
    fun initialize() {
        val loadCommand = LoadItemsCommand(this)
        commandInvoker.enqueueCommand(loadCommand)
    }
    
    // Command execution methods
    fun executeCommand(command: Command) {
        commandInvoker.enqueueCommand(command)
    }
    
    fun undoLastCommand() {
        commandInvoker.undoLastCommand()
    }
    
    fun hasUndoableCommands(): Boolean = commandInvoker.hasUndoableCommands()
    
    fun getPendingCommandsCount(): Int = commandInvoker.getPendingCommandsCount()
    
    // Internal execution methods called by commands
    suspend fun executeLoadItems() {
        _isLoading.value = true
        _error.value = null
        
        try {
            delay(2000) // Simulate network delay
            
            if (shouldSimulateError()) {
                throw Exception("Network error occurred")
            }
            
            val newItems = generateInitialItems()
            _items.value = newItems
            _screenUiState.value = ScreenUiState.Succeed
            
        } catch (e: Exception) {
            _error.value = e.message
            _screenUiState.value = ScreenUiState.Failed(e.message ?: "Unknown error")
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun executeRefreshItems() {
        _isLoading.value = true
        _error.value = null
        
        try {
            delay(1000) // Simulate refresh delay
            
            if (shouldSimulateError()) {
                throw Exception("Refresh failed")
            }
            
            val refreshedItems = generateInitialItems()
            _items.value = refreshedItems
            
        } catch (e: Exception) {
            _error.value = e.message
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun executeAddItem(item: Item) {
        val currentItems = _items.value
        _items.value = currentItems + item
    }
    
    suspend fun executeRemoveItem(itemId: String) {
        val currentItems = _items.value
        _items.value = currentItems.filterNot { it.id == itemId }
    }
    
    suspend fun executeUpdateItem(item: Item) {
        val currentItems = _items.value
        _items.value = currentItems.map { if (it.id == item.id) item else it }
    }
    
    fun findItemById(itemId: String): Item? {
        return _items.value.find { it.id == itemId }
    }
    
    // Public methods that create and enqueue commands
    fun loadItems() {
        val command = LoadItemsCommand(this)
        executeCommand(command)
    }
    
    fun refreshItems() {
        val command = RefreshItemsCommand(this)
        executeCommand(command)
    }
    
    fun addItem(item: Item) {
        val command = AddItemCommand(this, item)
        executeCommand(command)
    }
    
    fun removeItem(itemId: String) {
        val command = RemoveItemCommand(this, itemId)
        executeCommand(command)
    }
    
    fun updateItem(item: Item) {
        val command = UpdateItemCommand(this, item)
        executeCommand(command)
    }
}