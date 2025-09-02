package today.pathos.myapplication.study.agent04.result004

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import today.pathos.myapplication.study.common.BaseViewModel
import today.pathos.myapplication.study.common.Item
import today.pathos.myapplication.study.common.ScreenUiState

class Agent04Result004ViewModel : BaseViewModel() {
    
    var screenState by mutableStateOf<ScreenUiState>(ScreenUiState.Initializing)
        private set
    
    var items by mutableStateOf<List<Item>>(emptyList())
        private set
    
    var isRefreshing by mutableStateOf(false)
        private set
    
    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            delay(2000) // 2 second loading simulation
            
            if (shouldSimulateError()) {
                screenState = ScreenUiState.Failed("Failed to load initial data")
            } else {
                items = generateInitialItems()
                screenState = ScreenUiState.Succeed
            }
        }
    }

    fun retry() {
        screenState = ScreenUiState.Initializing
        loadInitialData()
    }

    fun addItem() {
        val newItem = generateNewItem(items.size)
        items = items + newItem
        errorMessage = null
    }

    fun removeLastItem() {
        if (items.isNotEmpty()) {
            items = items.dropLast(1)
        }
    }

    fun updateItem(updatedItem: Item) {
        items = items.map { 
            if (it.id == updatedItem.id) updatedItem else it 
        }
    }

    fun deleteItem(itemToDelete: Item) {
        items = items.filter { it.id != itemToDelete.id }
    }

    fun refreshItems() {
        if (isRefreshing) return
        
        viewModelScope.launch {
            isRefreshing = true
            delay(1000)
            
            if (shouldSimulateError()) {
                errorMessage = "Refresh failed"
            } else {
                items = generateInitialItems()
                errorMessage = null
            }
            isRefreshing = false
        }
    }

    fun clearError() {
        errorMessage = null
    }
}