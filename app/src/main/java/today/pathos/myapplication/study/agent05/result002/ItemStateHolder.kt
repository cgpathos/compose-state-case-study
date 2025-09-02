package today.pathos.myapplication.study.agent05.result002

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import today.pathos.myapplication.study.common.Item

// State holder interface for delegation
interface ItemStateHolder {
    val items: StateFlow<List<Item>>
    val isLoading: StateFlow<Boolean>
    val error: StateFlow<String?>
    
    fun updateItems(items: List<Item>)
    fun setLoading(loading: Boolean)
    fun setError(error: String?)
    fun clearError()
    fun addItem(item: Item)
    fun removeItem(itemId: String)
    fun updateItem(item: Item)
}

// Concrete implementation of state holder
class ItemStateHolderImpl : ItemStateHolder {
    private val _items = MutableStateFlow<List<Item>>(emptyList())
    override val items: StateFlow<List<Item>> = _items.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    override val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    override val error: StateFlow<String?> = _error.asStateFlow()
    
    override fun updateItems(items: List<Item>) {
        _items.value = items
    }
    
    override fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }
    
    override fun setError(error: String?) {
        _error.value = error
    }
    
    override fun clearError() {
        _error.value = null
    }
    
    override fun addItem(item: Item) {
        val currentItems = _items.value.toMutableList()
        currentItems.add(item)
        _items.value = currentItems
    }
    
    override fun removeItem(itemId: String) {
        val currentItems = _items.value.toMutableList()
        currentItems.removeAll { it.id == itemId }
        _items.value = currentItems
    }
    
    override fun updateItem(item: Item) {
        val currentItems = _items.value.toMutableList()
        val index = currentItems.indexOfFirst { it.id == item.id }
        if (index != -1) {
            currentItems[index] = item
            _items.value = currentItems
        }
    }
}