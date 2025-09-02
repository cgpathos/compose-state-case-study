package today.pathos.myapplication.study.agent05.result005

import today.pathos.myapplication.study.common.Item
import kotlin.random.Random
import kotlinx.coroutines.delay

// Data Source interfaces and implementations
interface ItemDataSource {
    suspend fun getItems(): Result<List<Item>>
    suspend fun saveItems(items: List<Item>): Result<Unit>
}

class LocalItemDataSource : ItemDataSource {
    private var cachedItems: List<Item> = emptyList()
    
    override suspend fun getItems(): Result<List<Item>> {
        return if (Random.nextDouble() < 0.2) {
            Result.failure(Exception("Local data source error"))
        } else {
            Result.success(cachedItems)
        }
    }
    
    override suspend fun saveItems(items: List<Item>): Result<Unit> {
        return if (Random.nextDouble() < 0.2) {
            Result.failure(Exception("Failed to save items locally"))
        } else {
            cachedItems = items
            Result.success(Unit)
        }
    }
}

class RemoteItemDataSource : ItemDataSource {
    override suspend fun getItems(): Result<List<Item>> {
        delay(2000) // Simulate network delay
        return if (Random.nextDouble() < 0.2) {
            Result.failure(Exception("Network error"))
        } else {
            Result.success(generateInitialItems())
        }
    }
    
    override suspend fun saveItems(items: List<Item>): Result<Unit> {
        delay(500) // Simulate network delay
        return if (Random.nextDouble() < 0.2) {
            Result.failure(Exception("Failed to save to server"))
        } else {
            Result.success(Unit)
        }
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

// Repository interface and implementation
interface ItemRepository {
    suspend fun getItems(): Result<List<Item>>
    suspend fun addItem(item: Item): Result<List<Item>>
    suspend fun removeItem(itemId: String): Result<List<Item>>
    suspend fun updateItem(item: Item): Result<List<Item>>
    suspend fun refreshItems(): Result<List<Item>>
}

class ItemRepositoryImpl(
    private val localDataSource: ItemDataSource,
    private val remoteDataSource: ItemDataSource
) : ItemRepository {
    
    private var currentItems: MutableList<Item> = mutableListOf()
    
    override suspend fun getItems(): Result<List<Item>> {
        // Try local first, then remote
        val localResult = localDataSource.getItems()
        return if (localResult.isSuccess && localResult.getOrNull()?.isNotEmpty() == true) {
            currentItems = localResult.getOrNull()?.toMutableList() ?: mutableListOf()
            localResult
        } else {
            val remoteResult = remoteDataSource.getItems()
            if (remoteResult.isSuccess) {
                currentItems = remoteResult.getOrNull()?.toMutableList() ?: mutableListOf()
                localDataSource.saveItems(currentItems)
            }
            remoteResult
        }
    }
    
    override suspend fun addItem(item: Item): Result<List<Item>> {
        currentItems.add(item)
        val saveResult = localDataSource.saveItems(currentItems)
        return if (saveResult.isSuccess) {
            // Try to sync with remote in background (simplified here)
            remoteDataSource.saveItems(currentItems)
            Result.success(currentItems.toList())
        } else {
            currentItems.removeLastOrNull()
            saveResult.map { currentItems.toList() }
        }
    }
    
    override suspend fun removeItem(itemId: String): Result<List<Item>> {
        val removedItem = currentItems.find { it.id == itemId }
        currentItems.removeAll { it.id == itemId }
        val saveResult = localDataSource.saveItems(currentItems)
        return if (saveResult.isSuccess) {
            remoteDataSource.saveItems(currentItems)
            Result.success(currentItems.toList())
        } else {
            removedItem?.let { currentItems.add(it) }
            saveResult.map { currentItems.toList() }
        }
    }
    
    override suspend fun updateItem(item: Item): Result<List<Item>> {
        val originalIndex = currentItems.indexOfFirst { it.id == item.id }
        val originalItem = if (originalIndex != -1) currentItems[originalIndex] else null
        
        if (originalIndex != -1) {
            currentItems[originalIndex] = item
        }
        
        val saveResult = localDataSource.saveItems(currentItems)
        return if (saveResult.isSuccess) {
            remoteDataSource.saveItems(currentItems)
            Result.success(currentItems.toList())
        } else {
            originalItem?.let { 
                if (originalIndex != -1) currentItems[originalIndex] = it 
            }
            saveResult.map { currentItems.toList() }
        }
    }
    
    override suspend fun refreshItems(): Result<List<Item>> {
        val result = remoteDataSource.getItems()
        if (result.isSuccess) {
            currentItems = result.getOrNull()?.toMutableList() ?: mutableListOf()
            localDataSource.saveItems(currentItems)
        }
        return result
    }
}

// Service interfaces
interface ItemService {
    suspend fun loadItems(): Result<List<Item>>
    suspend fun addItem(title: String, description: String): Result<List<Item>>
    suspend fun removeItem(itemId: String): Result<List<Item>>
    suspend fun updateItem(item: Item): Result<List<Item>>
    suspend fun refreshItems(): Result<List<Item>>
}

class ItemServiceImpl(
    private val repository: ItemRepository
) : ItemService {
    
    override suspend fun loadItems(): Result<List<Item>> {
        return repository.getItems()
    }
    
    override suspend fun addItem(title: String, description: String): Result<List<Item>> {
        val existingItems = repository.getItems().getOrElse { emptyList() }
        val newId = (existingItems.size + 1).toString()
        val newItem = Item(
            id = "item_$newId",
            title = title,
            description = description
        )
        return repository.addItem(newItem)
    }
    
    override suspend fun removeItem(itemId: String): Result<List<Item>> {
        return repository.removeItem(itemId)
    }
    
    override suspend fun updateItem(item: Item): Result<List<Item>> {
        return repository.updateItem(item)
    }
    
    override suspend fun refreshItems(): Result<List<Item>> {
        return repository.refreshItems()
    }
}