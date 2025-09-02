package today.pathos.myapplication.study.agent02.result003

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import today.pathos.myapplication.study.common.BaseViewModel
import today.pathos.myapplication.study.common.Item
import today.pathos.myapplication.study.common.ScreenUiState

data class CleanArchUiState(
    val items: List<Item> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
)

class CleanArchViewModel(
    private val getItemsUseCase: GetItemsUseCase = GetItemsUseCase(ItemRepositoryImpl()),
    private val addItemUseCase: AddItemUseCase = AddItemUseCase(ItemRepositoryImpl()),
    private val removeItemUseCase: RemoveItemUseCase = RemoveItemUseCase(ItemRepositoryImpl()),
    private val updateItemUseCase: UpdateItemUseCase = UpdateItemUseCase(ItemRepositoryImpl())
) : BaseViewModel() {
    
    private val repository = ItemRepositoryImpl()
    
    // Use Cases with shared repository
    private val getItems = GetItemsUseCase(repository)
    private val addItem = AddItemUseCase(repository)
    private val removeItem = RemoveItemUseCase(repository)
    private val updateItem = UpdateItemUseCase(repository)
    
    private val _screenUiState = MutableStateFlow<ScreenUiState>(ScreenUiState.Initializing)
    val screenUiState: StateFlow<ScreenUiState> = _screenUiState.asStateFlow()
    
    private val _uiState = MutableStateFlow(CleanArchUiState())
    val uiState: StateFlow<CleanArchUiState> = _uiState.asStateFlow()
    
    init {
        loadItems()
    }
    
    fun loadItems() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                delay(1000) // Additional delay for initialization
                val items = getItems()
                
                _uiState.value = _uiState.value.copy(
                    items = items,
                    isLoading = false
                )
                _screenUiState.value = ScreenUiState.Succeed
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
                _screenUiState.value = ScreenUiState.Failed(e.message ?: "Unknown error")
            }
        }
    }
    
    fun refreshItems() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true, error = null)
            
            try {
                val items = getItems()
                
                _uiState.value = _uiState.value.copy(
                    items = items,
                    isRefreshing = false
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRefreshing = false,
                    error = e.message
                )
            }
        }
    }
    
    fun addNewItem(item: Item) {
        viewModelScope.launch {
            try {
                addItem(item)
                val updatedItems = getItems()
                _uiState.value = _uiState.value.copy(items = updatedItems)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun removeItemById(itemId: String) {
        viewModelScope.launch {
            try {
                removeItem(itemId)
                val updatedItems = getItems()
                _uiState.value = _uiState.value.copy(items = updatedItems)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun updateExistingItem(item: Item) {
        viewModelScope.launch {
            try {
                updateItem(item)
                val updatedItems = getItems()
                _uiState.value = _uiState.value.copy(items = updatedItems)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}