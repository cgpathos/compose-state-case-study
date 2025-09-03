package today.pathos.myapplication.study.agent04.result006

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import today.pathos.myapplication.study.common.BaseViewModel
import today.pathos.myapplication.study.common.Item
import today.pathos.myapplication.study.common.ScreenUiState

class LaunchedEffectViewModel : BaseViewModel() {
    
    private val _screenUiState = MutableStateFlow<ScreenUiState>(ScreenUiState.Initializing)
    val screenUiState: StateFlow<ScreenUiState> = _screenUiState.asStateFlow()
    
    private val _items = MutableStateFlow<List<Item>>(emptyList())
    val items: StateFlow<List<Item>> = _items.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // LaunchedEffect initialization tracking
    private val _launchedEffectExecutions = MutableStateFlow(0)
    val launchedEffectExecutions: StateFlow<Int> = _launchedEffectExecutions.asStateFlow()
    
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()
    
    // Method called by LaunchedEffect in Composable - NO init{} block
    fun initializeFromLaunchedEffect(key: String) {
        if (_isInitialized.value) return // Prevent duplicate initialization
        
        _launchedEffectExecutions.value = _launchedEffectExecutions.value + 1
        _isInitialized.value = true
        
        loadItems()
    }
    
    // Method for reinitialization with different keys
    fun reinitializeFromLaunchedEffect(key: String) {
        _launchedEffectExecutions.value = _launchedEffectExecutions.value + 1
        loadItems()
    }
    
    private fun loadItems() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                delay(1500) // Simulate loading
                
                if (shouldSimulateError()) {
                    throw Exception("LaunchedEffect initialization failed")
                }
                
                val initialItems = generateInitialItems()
                _items.value = initialItems
                _screenUiState.value = ScreenUiState.Succeed
                
            } catch (e: Exception) {
                _error.value = e.message
                _screenUiState.value = ScreenUiState.Failed(e.message ?: "Unknown error")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun refreshItems() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                delay(1000)
                
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
    }
    
    fun addItem(item: Item) {
        val currentItems = _items.value
        _items.value = currentItems + item
    }
    
    fun removeItem(itemId: String) {
        val currentItems = _items.value
        _items.value = currentItems.filterNot { it.id == itemId }
    }
    
    fun updateItem(item: Item) {
        val currentItems = _items.value
        _items.value = currentItems.map { if (it.id == item.id) item else it }
    }
    
    fun resetInitialization() {
        _isInitialized.value = false
        _launchedEffectExecutions.value = 0
        _screenUiState.value = ScreenUiState.Initializing
        _items.value = emptyList()
        _error.value = null
    }
    
    // Method for conditional LaunchedEffect reinitialization
    fun conditionalReinitialize(condition: Boolean) {
        if (condition) {
            reinitializeFromLaunchedEffect("conditional-${System.currentTimeMillis()}")
        }
    }
    
    // Method for parametric LaunchedEffect initialization
    fun initializeWithParameters(userId: String, category: String) {
        _launchedEffectExecutions.value = _launchedEffectExecutions.value + 1
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                delay(1200)
                
                // Generate items based on parameters
                val parametricItems = listOf(
                    Item(
                        id = "user-$userId-1",
                        title = "User Item for $userId",
                        description = "Parametric item in $category category"
                    ),
                    Item(
                        id = "user-$userId-2", 
                        title = "Category $category Item",
                        description = "Item loaded with LaunchedEffect parameters"
                    )
                )
                
                _items.value = parametricItems
                _screenUiState.value = ScreenUiState.Succeed
                
            } catch (e: Exception) {
                _error.value = e.message
                _screenUiState.value = ScreenUiState.Failed(e.message ?: "Unknown error")
            } finally {
                _isLoading.value = false
            }
        }
    }
}