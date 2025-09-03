package today.pathos.myapplication.study.agent05.result007

import kotlinx.coroutines.delay
import today.pathos.myapplication.study.common.Item
import java.util.*

// Service interface
interface ItemService {
    suspend fun loadItems(): List<Item>
    suspend fun refreshItems(): List<Item>
    suspend fun addItem(item: Item)
    suspend fun removeItem(itemId: String)
    suspend fun updateItem(item: Item)
}

// Data Access Object interface
interface ItemDao {
    suspend fun getAllItems(): List<Item>
    suspend fun insertItem(item: Item)
    suspend fun deleteItem(itemId: String)
    suspend fun updateItem(item: Item)
}

// Network API interface
interface NetworkApi {
    suspend fun fetchItems(): List<Item>
    suspend fun syncItems(): List<Item>
}

// Configuration interface
interface AppConfig {
    val enableNetworkSync: Boolean
    val cacheTimeout: Long
    val maxItems: Int
}

// Concrete implementations
class LocalItemService(
    private val dao: ItemDao,
    private val api: NetworkApi,
    private val config: AppConfig
) : ItemService {
    
    private var cachedItems: MutableList<Item> = mutableListOf()
    
    override suspend fun loadItems(): List<Item> {
        delay(800) // Simulate service processing time
        
        if (config.enableNetworkSync) {
            try {
                val networkItems = api.fetchItems()
                cachedItems.clear()
                cachedItems.addAll(networkItems)
                
                // Sync with local storage
                networkItems.forEach { dao.insertItem(it) }
                
                return cachedItems.take(config.maxItems)
            } catch (e: Exception) {
                // Fallback to local data
                val localItems = dao.getAllItems()
                cachedItems.clear()
                cachedItems.addAll(localItems)
                return cachedItems.take(config.maxItems)
            }
        } else {
            val localItems = dao.getAllItems()
            cachedItems.clear()
            cachedItems.addAll(localItems)
            return cachedItems.take(config.maxItems)
        }
    }
    
    override suspend fun refreshItems(): List<Item> {
        delay(600)
        
        val refreshedItems = if (config.enableNetworkSync) {
            api.syncItems()
        } else {
            dao.getAllItems().map { it.copy(description = "Refreshed: ${it.description}") }
        }
        
        cachedItems.clear()
        cachedItems.addAll(refreshedItems)
        return cachedItems.take(config.maxItems)
    }
    
    override suspend fun addItem(item: Item) {
        dao.insertItem(item)
        cachedItems.add(item)
    }
    
    override suspend fun removeItem(itemId: String) {
        dao.deleteItem(itemId)
        cachedItems.removeAll { it.id == itemId }
    }
    
    override suspend fun updateItem(item: Item) {
        dao.updateItem(item)
        val index = cachedItems.indexOfFirst { it.id == item.id }
        if (index >= 0) {
            cachedItems[index] = item
        }
    }
}

class MockItemDao : ItemDao {
    private val items = mutableListOf<Item>()
    
    init {
        // Pre-populate with some items
        items.addAll(listOf(
            Item(
                id = "dao-${UUID.randomUUID()}",
                title = "DAO Item 1",
                description = "Item provided by MockItemDao implementation"
            ),
            Item(
                id = "dao-${UUID.randomUUID()}",
                title = "Local Storage Item",
                description = "Item from local data access object"
            )
        ))
    }
    
    override suspend fun getAllItems(): List<Item> {
        delay(200) // Simulate database query
        return items.toList()
    }
    
    override suspend fun insertItem(item: Item) {
        delay(100)
        items.add(item)
    }
    
    override suspend fun deleteItem(itemId: String) {
        delay(80)
        items.removeAll { it.id == itemId }
    }
    
    override suspend fun updateItem(item: Item) {
        delay(120)
        val index = items.indexOfFirst { it.id == item.id }
        if (index >= 0) {
            items[index] = item
        }
    }
}

class MockNetworkApi : NetworkApi {
    
    override suspend fun fetchItems(): List<Item> {
        delay(1200) // Simulate network latency
        
        // Simulate network failures
        if (Math.random() < 0.3) {
            throw Exception("Network fetch failed - connection timeout")
        }
        
        return listOf(
            Item(
                id = "api-${UUID.randomUUID()}",
                title = "Network Item 1",
                description = "Item fetched from MockNetworkApi"
            ),
            Item(
                id = "api-${UUID.randomUUID()}",
                title = "Remote Data",
                description = "Data provided by network API service"
            ),
            Item(
                id = "api-${UUID.randomUUID()}",
                title = "Cloud Item",
                description = "Item synchronized from cloud storage"
            )
        )
    }
    
    override suspend fun syncItems(): List<Item> {
        delay(1000)
        
        if (Math.random() < 0.25) {
            throw Exception("Sync failed - server unavailable")
        }
        
        return listOf(
            Item(
                id = "sync-${UUID.randomUUID()}",
                title = "Synced Item 1",
                description = "Item synchronized via MockNetworkApi"
            ),
            Item(
                id = "sync-${UUID.randomUUID()}",
                title = "Updated Remote Item",
                description = "Item updated through sync operation"
            )
        )
    }
}

class DefaultAppConfig : AppConfig {
    override val enableNetworkSync: Boolean = true
    override val cacheTimeout: Long = 30000L // 30 seconds
    override val maxItems: Int = 10
}

class OfflineAppConfig : AppConfig {
    override val enableNetworkSync: Boolean = false
    override val cacheTimeout: Long = 60000L // 60 seconds
    override val maxItems: Int = 5
}

class HighPerformanceConfig : AppConfig {
    override val enableNetworkSync: Boolean = true
    override val cacheTimeout: Long = 5000L // 5 seconds
    override val maxItems: Int = 20
}

// Dependency Provider
class DependencyProvider {
    
    private val configurations = mapOf(
        "default" to DefaultAppConfig(),
        "offline" to OfflineAppConfig(),
        "performance" to HighPerformanceConfig()
    )
    
    private val daos = mapOf<String, () -> ItemDao>(
        "mock" to { MockItemDao() },
        "memory" to { MockItemDao() } // Could be different implementations
    )
    
    private val apis = mapOf<String, () -> NetworkApi>(
        "mock" to { MockNetworkApi() },
        "production" to { MockNetworkApi() } // Could be different implementations
    )
    
    fun provideItemService(
        configType: String = "default",
        daoType: String = "mock",
        apiType: String = "mock"
    ): ItemService {
        val config = configurations[configType] ?: DefaultAppConfig()
        val dao = daos[daoType]?.invoke() ?: MockItemDao()
        val api = apis[apiType]?.invoke() ?: MockNetworkApi()
        
        return LocalItemService(dao, api, config)
    }
    
    fun provideAppConfig(configType: String): AppConfig {
        return configurations[configType] ?: DefaultAppConfig()
    }
    
    fun getAvailableConfigurations(): List<String> {
        return configurations.keys.toList()
    }
    
    fun getAvailableDaoTypes(): List<String> {
        return daos.keys.toList()
    }
    
    fun getAvailableApiTypes(): List<String> {
        return apis.keys.toList()
    }
}

// Singleton provider instance
object ProviderRegistry {
    val dependencyProvider = DependencyProvider()
    
    // Factory methods for common configurations
    fun createDefaultService(): ItemService {
        return dependencyProvider.provideItemService("default", "mock", "mock")
    }
    
    fun createOfflineService(): ItemService {
        return dependencyProvider.provideItemService("offline", "mock", "mock")
    }
    
    fun createHighPerformanceService(): ItemService {
        return dependencyProvider.provideItemService("performance", "mock", "mock")
    }
}