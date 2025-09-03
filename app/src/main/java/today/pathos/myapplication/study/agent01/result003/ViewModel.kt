package today.pathos.myapplication.study.agent01.result003

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import today.pathos.myapplication.study.common.BaseViewModel
import today.pathos.myapplication.study.common.Item
import today.pathos.myapplication.study.common.ScreenUiState

class ViewModel : BaseViewModel() {
    
    private val _screenUiState = MutableStateFlow<ScreenUiState>(ScreenUiState.Initializing)
    val screenUiState: StateFlow<ScreenUiState> = _screenUiState.asStateFlow()
    
    private val _items = MutableLiveData<List<Item>>(emptyList())
    private val _isLoading = MutableLiveData(false)
    private val _isRefreshing = MutableLiveData(false)
    private val _error = MutableLiveData<String?>(null)
    
    val items: LiveData<List<Item>> = _items
    val isLoading: LiveData<Boolean> = _isLoading
    val isRefreshing: LiveData<Boolean> = _isRefreshing
    val error: LiveData<String?> = _error
    
    // Transformation LiveData
    val hasError: LiveData<Boolean> = _error.map { it != null }
    
    val isEmpty: LiveData<Boolean> = MediatorLiveData<Boolean>().apply {
        addSource(_items) { items -> value = combineEmptyState(items, _isLoading.value, _error.value) }
        addSource(_isLoading) { loading -> value = combineEmptyState(_items.value, loading, _error.value) }
        addSource(_error) { error -> value = combineEmptyState(_items.value, _isLoading.value, error) }
    }
    
    val isContentLoading: LiveData<Boolean> = MediatorLiveData<Boolean>().apply {
        addSource(_isLoading) { loading -> value = combineContentLoading(loading, _items.value) }
        addSource(_items) { items -> value = combineContentLoading(_isLoading.value, items) }
    }
    
    val itemsWithLoading: LiveData<Pair<List<Item>, Boolean>> = MediatorLiveData<Pair<List<Item>, Boolean>>().apply {
        addSource(_items) { items -> value = Pair(items ?: emptyList(), _isLoading.value ?: false) }
        addSource(_isLoading) { loading -> value = Pair(_items.value ?: emptyList(), loading ?: false) }
    }
    
    private fun combineEmptyState(items: List<Item>?, loading: Boolean?, error: String?): Boolean {
        return (items?.isEmpty() == true) && (loading != true) && (error == null)
    }
    
    private fun combineContentLoading(loading: Boolean?, items: List<Item>?): Boolean {
        return (loading == true) && (items?.isEmpty() == true)
    }
    
    init {
        initializeScreen()
    }
    
    private fun initializeScreen() {
        viewModelScope.launch {
            try {
                delay(2000) // 2 second delay for initialization
                
                if (shouldSimulateError()) {
                    _screenUiState.value = ScreenUiState.Failed("Failed to initialize screen")
                    return@launch
                }
                
                val initialItems = generateInitialItems()
                _screenUiState.value = ScreenUiState.Succeed
                _items.value = initialItems
            } catch (e: Exception) {
                _screenUiState.value = ScreenUiState.Failed(e.message ?: "Unknown error")
            }
        }
    }
    
    fun addItem() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            delay(1000) // Simulate network delay
            
            try {
                if (shouldSimulateError()) {
                    _error.value = "Failed to add item"
                    _isLoading.value = false
                    return@launch
                }
                
                val currentItems = _items.value ?: emptyList()
                val newItem = generateNewItem(currentItems.size)
                _items.value = currentItems + newItem
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
                _isLoading.value = false
            }
        }
    }
    
    fun removeItem(itemId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            delay(500) // Simulate network delay
            
            try {
                if (shouldSimulateError()) {
                    _error.value = "Failed to remove item"
                    _isLoading.value = false
                    return@launch
                }
                
                val currentItems = _items.value ?: emptyList()
                val updatedItems = currentItems.filter { it.id != itemId }
                _items.value = updatedItems
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
                _isLoading.value = false
            }
        }
    }
    
    fun updateItem(updatedItem: Item) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            delay(500) // Simulate network delay
            
            try {
                if (shouldSimulateError()) {
                    _error.value = "Failed to update item"
                    _isLoading.value = false
                    return@launch
                }
                
                val currentItems = _items.value ?: emptyList()
                val updatedItems = currentItems.map { item ->
                    if (item.id == updatedItem.id) updatedItem else item
                }
                _items.value = updatedItems
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
                _isLoading.value = false
            }
        }
    }
    
    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _error.value = null
            delay(1500) // Simulate network delay
            
            try {
                if (shouldSimulateError()) {
                    _error.value = "Failed to refresh"
                    _isRefreshing.value = false
                    return@launch
                }
                
                val refreshedItems = generateInitialItems()
                _items.value = refreshedItems
                _isRefreshing.value = false
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
                _isRefreshing.value = false
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}