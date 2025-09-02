package today.pathos.myapplication.study.agent04.result005

import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import today.pathos.myapplication.study.common.Item
import kotlin.random.Random

class ItemSnapshot {
    // Using SnapshotStateList for automatic recomposition when list changes
    val items: SnapshotStateList<Item> = mutableStateListOf()
    
    var isRefreshing by mutableStateOf(false)
        private set
    
    var errorMessage by mutableStateOf<String?>(null)
        private set

    suspend fun loadInitialItems(): Boolean {
        return Snapshot.withMutableSnapshot {
            delay(2000) // 2 second loading simulation
            
            if (shouldSimulateError()) {
                false
            } else {
                items.clear()
                items.addAll(generateInitialItems())
                true
            }
        }
    }

    fun addItem() {
        Snapshot.withMutableSnapshot {
            val newItem = generateNewItem(items.size)
            items.add(newItem)
            errorMessage = null
        }
    }

    fun removeLastItem() {
        Snapshot.withMutableSnapshot {
            if (items.isNotEmpty()) {
                items.removeAt(items.size - 1)
            }
        }
    }

    fun updateItem(updatedItem: Item) {
        Snapshot.withMutableSnapshot {
            val index = items.indexOfFirst { it.id == updatedItem.id }
            if (index != -1) {
                items[index] = updatedItem
            }
        }
    }

    fun deleteItem(itemToDelete: Item) {
        Snapshot.withMutableSnapshot {
            items.removeIf { it.id == itemToDelete.id }
        }
    }

    suspend fun refreshItems() {
        if (isRefreshing) return
        
        Snapshot.withMutableSnapshot {
            isRefreshing = true
        }
        
        delay(1000)
        
        Snapshot.withMutableSnapshot {
            if (shouldSimulateError()) {
                errorMessage = "Refresh failed"
            } else {
                items.clear()
                items.addAll(generateInitialItems())
                errorMessage = null
            }
            isRefreshing = false
        }
    }

    fun clearError() {
        Snapshot.withMutableSnapshot {
            errorMessage = null
        }
    }

    // Create atomic snapshot for complex operations
    fun performBatchOperation(operation: () -> Unit) {
        Snapshot.withMutableSnapshot {
            operation()
        }
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