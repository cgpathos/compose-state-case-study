package today.pathos.myapplication.study.agent03.result006

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import today.pathos.myapplication.study.common.BaseViewModel
import today.pathos.myapplication.study.common.Item
import today.pathos.myapplication.study.common.ScreenUiState

@OptIn(FlowPreview::class)
class DebounceThrottleViewModel : BaseViewModel() {
    
    private val _screenUiState = MutableStateFlow<ScreenUiState>(ScreenUiState.Initializing)
    val screenUiState: StateFlow<ScreenUiState> = _screenUiState.asStateFlow()
    
    private val _items = MutableStateFlow<List<Item>>(emptyList())
    val items: StateFlow<List<Item>> = _items.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // Search functionality with debounce
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _filteredItems = MutableStateFlow<List<Item>>(emptyList())
    val filteredItems: StateFlow<List<Item>> = _filteredItems.asStateFlow()
    
    // Refresh requests with throttle
    private val refreshRequests = MutableSharedFlow<Unit>()
    
    // Add requests with debounce (for batch operations)
    private val addRequests = MutableSharedFlow<Item>()
    
    // Statistics
    private val _searchCount = MutableStateFlow(0)
    val searchCount: StateFlow<Int> = _searchCount.asStateFlow()
    
    private val _refreshCount = MutableStateFlow(0)
    val refreshCount: StateFlow<Int> = _refreshCount.asStateFlow()
    
    // Manual initialization method - NO init{} block
    fun initialize() {
        setupDebounceThrottleStreams()
        loadInitialData()
    }
    
    private fun setupDebounceThrottleStreams() {
        // Debounced search - waits 500ms after user stops typing
        searchQuery
            .debounce(500)
            .distinctUntilChanged()
            .onEach { query ->
                _searchCount.value = _searchCount.value + 1
                filterItems(query)
            }
            .launchIn(viewModelScope)
            
        // Throttled refresh - allows max one refresh per 2 seconds
        refreshRequests
            .sample(2000)
            .onEach {
                _refreshCount.value = _refreshCount.value + 1
                performRefresh()
            }
            .launchIn(viewModelScope)
            
        // Debounced add operations - batches add requests within 1 second
        addRequests
            .debounce(1000)
            .onEach { item ->
                batchAddItems(listOf(item))
            }
            .launchIn(viewModelScope)
    }
    
    private fun loadInitialData() {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                delay(1500) // Simulate loading
                val initialItems = generateInitialItems()
                _items.value = initialItems
                _filteredItems.value = initialItems
                _screenUiState.value = ScreenUiState.Succeed
                
            } catch (e: Exception) {
                _error.value = e.message
                _screenUiState.value = ScreenUiState.Failed(e.message ?: "Unknown error")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private fun filterItems(query: String) {
        val currentItems = _items.value
        val filtered = if (query.isBlank()) {
            currentItems
        } else {
            currentItems.filter { item ->
                item.title.contains(query, ignoreCase = true) ||
                item.description.contains(query, ignoreCase = true)
            }
        }
        _filteredItems.value = filtered
    }
    
    private suspend fun performRefresh() {
        _isLoading.value = true
        _error.value = null
        
        try {
            delay(1000) // Simulate refresh
            
            if (shouldSimulateError()) {
                throw Exception("Refresh failed - network timeout")
            }
            
            val refreshedItems = generateInitialItems()
            _items.value = refreshedItems
            
            // Reapply current filter
            filterItems(_searchQuery.value)
            
        } catch (e: Exception) {
            _error.value = e.message
        } finally {
            _isLoading.value = false
        }
    }
    
    private suspend fun batchAddItems(items: List<Item>) {
        val currentItems = _items.value
        val updatedItems = currentItems + items
        _items.value = updatedItems
        
        // Reapply current filter
        filterItems(_searchQuery.value)
    }
    
    // Public methods that use debounce/throttle
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        // The debounced filtering will happen automatically
    }
    
    fun refreshItems() {
        viewModelScope.launch {
            refreshRequests.emit(Unit)
            // The throttled refresh will happen automatically
        }
    }
    
    fun addItem(item: Item) {
        viewModelScope.launch {
            addRequests.emit(item)
            // The debounced batch add will happen automatically
        }
    }
    
    // Immediate operations (not debounced/throttled)
    fun removeItem(itemId: String) {
        val currentItems = _items.value
        val updatedItems = currentItems.filterNot { it.id == itemId }
        _items.value = updatedItems
        
        // Reapply current filter
        filterItems(_searchQuery.value)
    }
    
    fun updateItem(item: Item) {
        val currentItems = _items.value
        val updatedItems = currentItems.map { if (it.id == item.id) item else it }
        _items.value = updatedItems
        
        // Reapply current filter
        filterItems(_searchQuery.value)
    }
    
    // Force immediate operations (bypass debounce/throttle)
    fun forceRefresh() {
        viewModelScope.launch {
            performRefresh()
        }
    }
    
    fun resetStatistics() {
        _searchCount.value = 0
        _refreshCount.value = 0
    }
}