package today.pathos.myapplication.study.agent05.result002

import kotlinx.coroutines.delay
import today.pathos.myapplication.study.common.Item
import kotlin.random.Random

// Delegate interface for item operations
interface ItemOperationDelegate {
    suspend fun loadInitialItems(): Result<List<Item>>
    suspend fun addNewItem(title: String, description: String, existingItems: List<Item>): Result<Item>
    suspend fun refreshItems(): Result<List<Item>>
    suspend fun performOperation(operation: suspend () -> Unit): Result<Unit>
}

// Concrete delegate implementation
class ItemOperationDelegateImpl : ItemOperationDelegate {
    
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
    
    override suspend fun loadInitialItems(): Result<List<Item>> {
        delay(2000) // 2s delay for initial load
        
        return if (shouldSimulateError()) {
            Result.failure(Exception("Failed to load initial items"))
        } else {
            Result.success(generateInitialItems())
        }
    }
    
    override suspend fun addNewItem(title: String, description: String, existingItems: List<Item>): Result<Item> {
        return if (shouldSimulateError()) {
            Result.failure(Exception("Failed to add new item"))
        } else {
            val newId = (existingItems.size + 1).toString()
            val newItem = Item(
                id = "item_$newId",
                title = title,
                description = description
            )
            Result.success(newItem)
        }
    }
    
    override suspend fun refreshItems(): Result<List<Item>> {
        delay(1000) // 1s delay for refresh
        
        return if (shouldSimulateError()) {
            Result.failure(Exception("Failed to refresh items"))
        } else {
            Result.success(generateInitialItems())
        }
    }
    
    override suspend fun performOperation(operation: suspend () -> Unit): Result<Unit> {
        return try {
            if (shouldSimulateError()) {
                Result.failure(Exception("Operation failed"))
            } else {
                operation()
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}