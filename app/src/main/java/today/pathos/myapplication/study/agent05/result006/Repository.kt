package today.pathos.myapplication.study.agent05.result006

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import today.pathos.myapplication.study.common.Item
import java.util.*

// Repository interface
interface ItemRepository {
    suspend fun loadItems(): List<Item>
    suspend fun refreshItems(): List<Item>
    fun getItemsFlow(): Flow<List<Item>>
    suspend fun addItem(item: Item)
    suspend fun removeItem(itemId: String)
    suspend fun updateItem(item: Item)
    fun isInitialized(): Boolean
}

// Lazy initialization repository
class LazyItemRepository : ItemRepository {
    
    private var items: MutableList<Item>? = null
    private var isLazilyInitialized = false
    
    // Lazy initialization - called on first access
    private suspend fun initializeIfNeeded() {
        if (!isLazilyInitialized) {
            delay(1500) // Simulate initialization time
            
            items = mutableListOf(
                Item(
                    id = UUID.randomUUID().toString(),
                    title = "Lazy Initialized Item 1",
                    description = "This item was created during lazy repository initialization"
                ),
                Item(
                    id = UUID.randomUUID().toString(),
                    title = "Deferred Loading Item 2",
                    description = "Repository initialized only when first accessed, not during construction"
                ),
                Item(
                    id = UUID.randomUUID().toString(),
                    title = "On-Demand Item 3",
                    description = "Lazy pattern allows initialization to happen when actually needed"
                )
            )
            
            isLazilyInitialized = true
        }
    }
    
    override suspend fun loadItems(): List<Item> {
        initializeIfNeeded()
        return items?.toList() ?: emptyList()
    }
    
    override suspend fun refreshItems(): List<Item> {
        initializeIfNeeded()
        
        // Simulate refresh delay
        delay(1000)
        
        // Add a new refresh item
        val refreshItem = Item(
            id = UUID.randomUUID().toString(),
            title = "Refreshed Item ${System.currentTimeMillis()}",
            description = "Item added during lazy repository refresh operation"
        )
        
        items?.add(refreshItem)
        return items?.toList() ?: emptyList()
    }
    
    override fun getItemsFlow(): Flow<List<Item>> = flow {
        initializeIfNeeded()
        emit(items?.toList() ?: emptyList())
        
        // Simulate periodic updates
        repeat(3) {
            delay(2000)
            val periodicItem = Item(
                id = UUID.randomUUID().toString(),
                title = "Periodic Item ${it + 1}",
                description = "Item emitted periodically from lazy repository flow"
            )
            items?.add(periodicItem)
            emit(items?.toList() ?: emptyList())
        }
    }
    
    override suspend fun addItem(item: Item) {
        initializeIfNeeded()
        items?.add(item)
    }
    
    override suspend fun removeItem(itemId: String) {
        initializeIfNeeded()
        items?.removeAll { it.id == itemId }
    }
    
    override suspend fun updateItem(item: Item) {
        initializeIfNeeded()
        val index = items?.indexOfFirst { it.id == item.id } ?: -1
        if (index >= 0) {
            items?.set(index, item)
        }
    }
    
    override fun isInitialized(): Boolean = isLazilyInitialized
    
    fun forceReset() {
        items = null
        isLazilyInitialized = false
    }
}

// Cache-backed lazy repository
class CacheLazyRepository : ItemRepository {
    
    private var cachedItems: List<Item>? = null
    private var cacheTimestamp: Long = 0
    private val cacheValidityMs = 30000L // 30 seconds
    
    private suspend fun loadFromSource(): List<Item> {
        delay(800) // Simulate data source access
        
        return listOf(
            Item(
                id = UUID.randomUUID().toString(),
                title = "Cache-Lazy Item 1",
                description = "Item loaded from source and cached by lazy repository"
            ),
            Item(
                id = UUID.randomUUID().toString(),
                title = "Source-Backed Item 2",
                description = "Lazy repository checks cache validity before loading from source"
            )
        )
    }
    
    private suspend fun getValidCachedItems(): List<Item>? {
        val currentTime = System.currentTimeMillis()
        return if (cachedItems != null && (currentTime - cacheTimestamp) < cacheValidityMs) {
            cachedItems
        } else {
            null
        }
    }
    
    override suspend fun loadItems(): List<Item> {
        // Check cache first (lazy pattern with caching)
        getValidCachedItems()?.let { cached ->
            return cached
        }
        
        // Load from source and cache
        val freshItems = loadFromSource()
        cachedItems = freshItems
        cacheTimestamp = System.currentTimeMillis()
        
        return freshItems
    }
    
    override suspend fun refreshItems(): List<Item> {
        // Force refresh bypasses cache
        val freshItems = loadFromSource()
        cachedItems = freshItems
        cacheTimestamp = System.currentTimeMillis()
        
        return freshItems
    }
    
    override fun getItemsFlow(): Flow<List<Item>> = flow {
        val items = loadItems()
        emit(items)
    }
    
    override suspend fun addItem(item: Item) {
        val currentItems = cachedItems?.toMutableList() ?: mutableListOf()
        currentItems.add(item)
        cachedItems = currentItems
    }
    
    override suspend fun removeItem(itemId: String) {
        val currentItems = cachedItems?.toMutableList() ?: mutableListOf()
        currentItems.removeAll { it.id == itemId }
        cachedItems = currentItems
    }
    
    override suspend fun updateItem(item: Item) {
        val currentItems = cachedItems?.toMutableList() ?: mutableListOf()
        val index = currentItems.indexOfFirst { it.id == item.id }
        if (index >= 0) {
            currentItems[index] = item
            cachedItems = currentItems
        }
    }
    
    override fun isInitialized(): Boolean = cachedItems != null
    
    fun invalidateCache() {
        cachedItems = null
        cacheTimestamp = 0
    }
    
    fun getCacheAge(): Long {
        return if (cachedItems != null) {
            System.currentTimeMillis() - cacheTimestamp
        } else {
            -1
        }
    }
}

// Database-backed lazy repository
class DatabaseLazyRepository : ItemRepository {
    
    private var databaseConnection: String? = null
    private var items: MutableList<Item>? = null
    
    private suspend fun connectToDatabase() {
        if (databaseConnection == null) {
            delay(2000) // Simulate database connection time
            databaseConnection = "Connected-${System.currentTimeMillis()}"
            
            // Load initial data from "database"
            items = mutableListOf(
                Item(
                    id = "db-${UUID.randomUUID()}",
                    title = "Database Item 1",
                    description = "Item loaded from database via lazy repository connection"
                ),
                Item(
                    id = "db-${UUID.randomUUID()}",
                    title = "Persistent Item 2",
                    description = "Database connection established lazily on first repository access"
                ),
                Item(
                    id = "db-${UUID.randomUUID()}",
                    title = "SQL Item 3",
                    description = "Lazy database repository with persistent storage simulation"
                )
            )
        }
    }
    
    override suspend fun loadItems(): List<Item> {
        connectToDatabase()
        return items?.toList() ?: emptyList()
    }
    
    override suspend fun refreshItems(): List<Item> {
        connectToDatabase()
        delay(600) // Simulate database query time
        
        // Simulate database refresh with updated timestamp
        val refreshedItems = items?.map { item ->
            item.copy(description = "Refreshed: ${item.description}")
        }?.toMutableList() ?: mutableListOf()
        
        items = refreshedItems
        return refreshedItems
    }
    
    override fun getItemsFlow(): Flow<List<Item>> = flow {
        connectToDatabase()
        emit(items?.toList() ?: emptyList())
    }
    
    override suspend fun addItem(item: Item) {
        connectToDatabase()
        delay(200) // Simulate database insert
        items?.add(item.copy(id = "db-${item.id}"))
    }
    
    override suspend fun removeItem(itemId: String) {
        connectToDatabase()
        delay(150) // Simulate database delete
        items?.removeAll { it.id == itemId }
    }
    
    override suspend fun updateItem(item: Item) {
        connectToDatabase()
        delay(180) // Simulate database update
        val index = items?.indexOfFirst { it.id == item.id } ?: -1
        if (index >= 0) {
            items?.set(index, item)
        }
    }
    
    override fun isInitialized(): Boolean = databaseConnection != null
    
    fun getDatabaseConnection(): String? = databaseConnection
    
    fun closeConnection() {
        databaseConnection = null
        items = null
    }
}