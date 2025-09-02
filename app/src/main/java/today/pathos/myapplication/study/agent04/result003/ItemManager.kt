package today.pathos.myapplication.study.agent04.result003

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import today.pathos.myapplication.study.common.Item
import kotlin.random.Random

class ItemManager {
    var items by mutableStateOf<List<Item>>(emptyList())
        private set
    
    var isRefreshing by mutableStateOf(false)
        private set
    
    var errorMessage by mutableStateOf<String?>(null)
        private set

    suspend fun loadInitialItems(): Boolean {
        delay(2000) // 2 second loading simulation
        
        return if (shouldSimulateError()) {
            false
        } else {
            items = generateInitialItems()
            true
        }
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

    suspend fun refreshItems() {
        if (isRefreshing) return
        
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

    fun clearError() {
        errorMessage = null
    }

    // Utility functions
    private fun generateInitialItems(): List<Item> {
        return (1..5).map { index ->
            Item(
                id = "item_$index",
                title = "Item $index",
                description = "Description for item $index"
            )
        }
    }

    private fun shouldSimulateError(): Boolean {
        return Random.nextDouble() < 0.2 // 20% chance of error
    }

    private fun generateNewItem(existingCount: Int): Item {
        val newId = existingCount + 1
        return Item(
            id = "item_$newId",
            title = "New Item $newId",
            description = "Description for new item $newId"
        )
    }
}