package today.pathos.myapplication.study.agent02.result003

import kotlinx.coroutines.delay
import today.pathos.myapplication.study.common.Item
import kotlin.random.Random

interface ItemRepository {
    suspend fun getItems(): List<Item>
    suspend fun addItem(item: Item)
    suspend fun removeItem(itemId: String)
    suspend fun updateItem(item: Item)
}

class ItemRepositoryImpl : ItemRepository {
    
    private val items = mutableListOf<Item>()
    
    override suspend fun getItems(): List<Item> {
        delay(1000) // Simulate network delay
        
        if (shouldSimulateError()) {
            throw Exception("Repository error occurred")
        }
        
        if (items.isEmpty()) {
            // Initialize with default items
            items.addAll(generateInitialItems())
        }
        
        return items.toList()
    }
    
    override suspend fun addItem(item: Item) {
        items.add(item)
    }
    
    override suspend fun removeItem(itemId: String) {
        items.removeAll { it.id == itemId }
    }
    
    override suspend fun updateItem(item: Item) {
        val index = items.indexOfFirst { it.id == item.id }
        if (index != -1) {
            items[index] = item
        }
    }
    
    private fun shouldSimulateError(): Boolean {
        return Random.nextDouble() < 0.2 // 20% chance of error
    }
    
    private fun generateInitialItems(): List<Item> {
        return (1..5).map { index ->
            Item(
                id = "item_$index",
                title = "Item $index",
                description = "Description for item $index"
            )
        }
    }
}