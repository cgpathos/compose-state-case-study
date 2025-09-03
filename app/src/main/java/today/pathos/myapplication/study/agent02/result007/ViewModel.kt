package today.pathos.myapplication.study.agent02.result007

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import today.pathos.myapplication.study.common.BaseViewModel
import today.pathos.myapplication.study.common.Item
import today.pathos.myapplication.study.common.ScreenUiState

class StrategyViewModel : BaseViewModel() {
    
    private val _screenUiState = MutableStateFlow<ScreenUiState>(ScreenUiState.Initializing)
    val screenUiState: StateFlow<ScreenUiState> = _screenUiState.asStateFlow()
    
    private val _items = MutableStateFlow<List<Item>>(emptyList())
    val items: StateFlow<List<Item>> = _items.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // Available strategies
    private val availableStrategies = listOf(
        FastLoadStrategy(),
        DetailedLoadStrategy(),
        CachedStrategy(),
        MockNetworkStrategy()
    )
    
    private val _currentStrategy = MutableStateFlow<DataStrategy>(availableStrategies[0])
    val currentStrategy: StateFlow<DataStrategy> = _currentStrategy.asStateFlow()
    
    private val _strategyOptions = MutableStateFlow(availableStrategies)
    val strategyOptions: StateFlow<List<DataStrategy>> = _strategyOptions.asStateFlow()
    
    // Manual initialization method - NO init{} block
    fun initialize() {
        loadItems()
    }
    
    fun setStrategy(strategy: DataStrategy) {
        _currentStrategy.value = strategy
        // Automatically reload with new strategy
        loadItems()
    }
    
    fun loadItems() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val strategy = _currentStrategy.value
                val loadedItems = strategy.loadItems()
                
                _items.value = loadedItems
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
                val strategy = _currentStrategy.value
                val refreshedItems = strategy.refreshItems()
                
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
    
    fun getCurrentStrategyName(): String = _currentStrategy.value.name
    fun getCurrentStrategyDescription(): String = _currentStrategy.value.description
}