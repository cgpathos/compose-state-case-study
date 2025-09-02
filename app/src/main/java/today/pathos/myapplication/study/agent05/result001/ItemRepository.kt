package today.pathos.myapplication.study.agent05.result001

import today.pathos.myapplication.study.common.Item
import kotlinx.coroutines.delay
import kotlin.random.Random

interface ItemRepository {
    suspend fun getItems(): Result<List<Item>>
    suspend fun addItem(item: Item): Result<List<Item>>
    suspend fun removeItem(itemId: String): Result<List<Item>>
    suspend fun updateItem(item: Item): Result<List<Item>>
    suspend fun refreshItems(): Result<List<Item>>
}

class ItemRepositoryImpl : ItemRepository {
    private val items = mutableListOf<Item>()
    
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
    
    override suspend fun getItems(): Result<List<Item>> {
        delay(2000) // 2s delay for initial load
        
        return if (shouldSimulateError()) {
            Result.failure(Exception("Failed to load items from repository"))
        } else {
            if (items.isEmpty()) {
                items.addAll(generateInitialItems())
            }
            Result.success(items.toList())
        }
    }
    
    override suspend fun addItem(item: Item): Result<List<Item>> {
        return if (shouldSimulateError()) {
            Result.failure(Exception("Failed to add item"))
        } else {
            items.add(item)
            Result.success(items.toList())
        }
    }
    
    override suspend fun removeItem(itemId: String): Result<List<Item>> {
        return if (shouldSimulateError()) {
            Result.failure(Exception("Failed to remove item"))
        } else {
            items.removeAll { it.id == itemId }
            Result.success(items.toList())
        }
    }
    
    override suspend fun updateItem(item: Item): Result<List<Item>> {
        return if (shouldSimulateError()) {
            Result.failure(Exception("Failed to update item"))
        } else {
            val index = items.indexOfFirst { it.id == item.id }
            if (index != -1) {
                items[index] = item
            }
            Result.success(items.toList())
        }
    }
    
    override suspend fun refreshItems(): Result<List<Item>> {
        delay(1000) // 1s delay for refresh
        
        return if (shouldSimulateError()) {
            Result.failure(Exception("Failed to refresh items"))
        } else {
            items.clear()
            items.addAll(generateInitialItems())
            Result.success(items.toList())
        }
    }
}