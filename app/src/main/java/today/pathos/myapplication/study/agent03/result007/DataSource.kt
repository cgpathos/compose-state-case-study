package today.pathos.myapplication.study.agent03.result007

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import today.pathos.myapplication.study.common.Item
import java.util.*

// Data Source interface
interface DataSource {
    val name: String
    val description: String
    fun getItemsFlow(): Flow<List<Item>>
    fun getRefreshFlow(): Flow<List<Item>>
}

// Local Cache Data Source
class LocalCacheSource : DataSource {
    override val name = "Local Cache"
    override val description = "Fast local cached data"
    
    private val cachedItems = listOf(
        Item(
            id = UUID.randomUUID().toString(),
            title = "Cached Document",
            description = "Locally stored document for offline access"
        ),
        Item(
            id = UUID.randomUUID().toString(),
            title = "Offline Data",
            description = "Data available without network connection"
        )
    )
    
    override fun getItemsFlow(): Flow<List<Item>> = flow {
        delay(200) // Fast cache access
        emit(cachedItems)
    }
    
    override fun getRefreshFlow(): Flow<List<Item>> = flow {
        delay(100) // Even faster cache refresh
        emit(cachedItems.map { it.copy(description = "Refreshed: ${it.description}") })
    }
}

// Network API Data Source
class NetworkApiSource : DataSource {
    override val name = "Network API"
    override val description = "Real-time data from remote server"
    
    override fun getItemsFlow(): Flow<List<Item>> = flow {
        delay(1500) // Network latency
        
        // Simulate network failures
        if (Math.random() < 0.25) {
            throw Exception("Network timeout - API unavailable")
        }
        
        emit(listOf(
            Item(
                id = UUID.randomUUID().toString(),
                title = "API Response Data",
                description = "Fresh data retrieved from remote API endpoint"
            ),
            Item(
                id = UUID.randomUUID().toString(),
                title = "Live Network Data",
                description = "Real-time information from server database"
            ),
            Item(
                id = UUID.randomUUID().toString(),
                title = "Remote Content",
                description = "Content synchronized from cloud storage"
            )
        ))
    }
    
    override fun getRefreshFlow(): Flow<List<Item>> = flow {
        delay(1200)
        
        if (Math.random() < 0.3) {
            throw Exception("Refresh failed - server error")
        }
        
        emit(listOf(
            Item(
                id = UUID.randomUUID().toString(),
                title = "Updated API Data",
                description = "Latest data from API refresh operation"
            ),
            Item(
                id = UUID.randomUUID().toString(),
                title = "Synchronized Content",
                description = "Content updated via network refresh"
            )
        ))
    }
}

// Database Data Source
class DatabaseSource : DataSource {
    override val name = "Local Database"
    override val description = "Persistent local database storage"
    
    override fun getItemsFlow(): Flow<List<Item>> = flow {
        delay(800) // Database query time
        
        emit(listOf(
            Item(
                id = UUID.randomUUID().toString(),
                title = "Database Record 1",
                description = "Persistent data stored in local SQLite database"
            ),
            Item(
                id = UUID.randomUUID().toString(),
                title = "Database Record 2",
                description = "Structured data with relational integrity"
            ),
            Item(
                id = UUID.randomUUID().toString(),
                title = "Indexed Content",
                description = "Optimized database content with full-text search"
            ),
            Item(
                id = UUID.randomUUID().toString(),
                title = "Transaction Data",
                description = "ACID-compliant data from database transactions"
            )
        ))
    }
    
    override fun getRefreshFlow(): Flow<List<Item>> = flow {
        delay(600)
        
        emit(listOf(
            Item(
                id = UUID.randomUUID().toString(),
                title = "Refreshed DB Record",
                description = "Updated database record from refresh operation"
            ),
            Item(
                id = UUID.randomUUID().toString(),
                title = "Updated Index",
                description = "Reindexed database content with new entries"
            )
        ))
    }
}

// Mock WebSocket Data Source
class WebSocketSource : DataSource {
    override val name = "WebSocket Stream"
    override val description = "Real-time streaming data"
    
    override fun getItemsFlow(): Flow<List<Item>> = flow {
        delay(600) // Connection establishment
        
        // Emit initial data
        emit(listOf(
            Item(
                id = UUID.randomUUID().toString(),
                title = "WebSocket Message 1",
                description = "Real-time message received via WebSocket connection"
            )
        ))
        
        // Simulate streaming updates
        repeat(3) { index ->
            delay(1000)
            emit(listOf(
                Item(
                    id = UUID.randomUUID().toString(),
                    title = "Stream Update ${index + 2}",
                    description = "Live update #${index + 2} from WebSocket stream"
                )
            ))
        }
    }
    
    override fun getRefreshFlow(): Flow<List<Item>> = flow {
        delay(400)
        
        // WebSocket refresh reconnects and gets latest
        emit(listOf(
            Item(
                id = UUID.randomUUID().toString(),
                title = "Reconnected Stream",
                description = "Fresh WebSocket connection with latest stream data"
            ),
            Item(
                id = UUID.randomUUID().toString(),
                title = "Live Refresh",
                description = "Real-time data from WebSocket refresh"
            )
        ))
    }
}

// Hybrid Source (combines multiple sources)
class HybridSource : DataSource {
    override val name = "Hybrid Multi-Source"
    override val description = "Combines cache, database, and network"
    
    private val cacheSource = LocalCacheSource()
    private val dbSource = DatabaseSource()
    private val apiSource = NetworkApiSource()
    
    override fun getItemsFlow(): Flow<List<Item>> = flow {
        // First emit cached data quickly
        try {
            cacheSource.getItemsFlow().collect { cachedItems ->
                emit(cachedItems.map { it.copy(title = "Cache: ${it.title}") })
            }
        } catch (e: Exception) {
            // Continue to next source
        }
        
        delay(500)
        
        // Then emit database data
        try {
            dbSource.getItemsFlow().collect { dbItems ->
                emit(dbItems.map { it.copy(title = "DB: ${it.title}") })
            }
        } catch (e: Exception) {
            // Continue to next source
        }
        
        delay(800)
        
        // Finally try network data
        try {
            apiSource.getItemsFlow().collect { apiItems ->
                emit(apiItems.map { it.copy(title = "API: ${it.title}") })
            }
        } catch (e: Exception) {
            // Fallback complete - keep last successful data
        }
    }
    
    override fun getRefreshFlow(): Flow<List<Item>> = flow {
        delay(1000)
        
        // Hybrid refresh prioritizes fresh network data
        try {
            apiSource.getRefreshFlow().collect { freshItems ->
                emit(freshItems.map { it.copy(title = "Fresh API: ${it.title}") })
            }
        } catch (e: Exception) {
            // Fallback to database refresh
            dbSource.getRefreshFlow().collect { dbItems ->
                emit(dbItems.map { it.copy(title = "DB Refresh: ${it.title}") })
            }
        }
    }
}