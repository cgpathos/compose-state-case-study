package today.pathos.myapplication.study.agent02.result007

import kotlinx.coroutines.delay
import today.pathos.myapplication.study.common.Item
import java.util.*

// Strategy interface
interface DataStrategy {
    suspend fun loadItems(): List<Item>
    suspend fun refreshItems(): List<Item>
    val name: String
    val description: String
}

// Concrete Strategies
class FastLoadStrategy : DataStrategy {
    override val name = "Fast Load"
    override val description = "Quick loading with minimal data"
    
    override suspend fun loadItems(): List<Item> {
        delay(500) // Fast loading
        return listOf(
            Item(
                id = UUID.randomUUID().toString(),
                title = "Fast Item 1",
                description = "Quickly loaded item with minimal processing"
            ),
            Item(
                id = UUID.randomUUID().toString(),
                title = "Fast Item 2", 
                description = "Another quick item for fast strategy"
            )
        )
    }
    
    override suspend fun refreshItems(): List<Item> {
        delay(300)
        return listOf(
            Item(
                id = UUID.randomUUID().toString(),
                title = "Fast Refresh 1",
                description = "Refreshed quickly with fast strategy"
            ),
            Item(
                id = UUID.randomUUID().toString(),
                title = "Fast Refresh 2",
                description = "Another fast refresh item"
            )
        )
    }
}

class DetailedLoadStrategy : DataStrategy {
    override val name = "Detailed Load"
    override val description = "Thorough loading with comprehensive data"
    
    override suspend fun loadItems(): List<Item> {
        delay(2000) // Slower but more detailed
        return listOf(
            Item(
                id = UUID.randomUUID().toString(),
                title = "Detailed Analysis Report",
                description = "Comprehensive analysis with full metadata, tags, and extensive validation"
            ),
            Item(
                id = UUID.randomUUID().toString(),
                title = "Complex Data Structure",
                description = "Multi-layered data with relationships, dependencies, and cross-references"
            ),
            Item(
                id = UUID.randomUUID().toString(),
                title = "Processed Information",
                description = "Information that underwent filtering, sorting, and enrichment"
            )
        )
    }
    
    override suspend fun refreshItems(): List<Item> {
        delay(1500)
        return listOf(
            Item(
                id = UUID.randomUUID().toString(),
                title = "Updated Detailed Report",
                description = "Refreshed comprehensive analysis with latest data validation"
            ),
            Item(
                id = UUID.randomUUID().toString(),
                title = "Synchronized Complex Data",
                description = "Updated multi-layered structure with refreshed relationships"
            )
        )
    }
}

class CachedStrategy : DataStrategy {
    override val name = "Cached Load"
    override val description = "Uses cached data when available"
    
    private var cachedItems: List<Item>? = null
    private var lastCacheTime = 0L
    private val cacheValidityMs = 10000L // 10 seconds
    
    override suspend fun loadItems(): List<Item> {
        val currentTime = System.currentTimeMillis()
        
        // Return cached data if available and valid
        cachedItems?.let { cached ->
            if (currentTime - lastCacheTime < cacheValidityMs) {
                delay(100) // Minimal delay for cache hit
                return cached
            }
        }
        
        // Load fresh data
        delay(1000)
        val freshItems = listOf(
            Item(
                id = UUID.randomUUID().toString(),
                title = "Cached Item 1",
                description = "Item loaded and cached for future use"
            ),
            Item(
                id = UUID.randomUUID().toString(),
                title = "Cached Item 2",
                description = "Another cached item with timestamp"
            ),
            Item(
                id = UUID.randomUUID().toString(),
                title = "Fresh Data",
                description = "Newly loaded data now available in cache"
            )
        )
        
        cachedItems = freshItems
        lastCacheTime = currentTime
        return freshItems
    }
    
    override suspend fun refreshItems(): List<Item> {
        delay(800)
        val refreshedItems = listOf(
            Item(
                id = UUID.randomUUID().toString(),
                title = "Refreshed Cache 1",
                description = "Cache invalidated and refreshed with new data"
            ),
            Item(
                id = UUID.randomUUID().toString(),
                title = "Updated Cache 2",
                description = "Fresh data replacing stale cache entries"
            )
        )
        
        cachedItems = refreshedItems
        lastCacheTime = System.currentTimeMillis()
        return refreshedItems
    }
}

class MockNetworkStrategy : DataStrategy {
    override val name = "Mock Network"
    override val description = "Simulates network requests with potential failures"
    
    override suspend fun loadItems(): List<Item> {
        delay(1200)
        
        // Simulate network failures occasionally
        if (Math.random() < 0.3) {
            throw Exception("Network timeout - please try again")
        }
        
        return listOf(
            Item(
                id = UUID.randomUUID().toString(),
                title = "Network Item 1",
                description = "Data fetched from simulated network endpoint"
            ),
            Item(
                id = UUID.randomUUID().toString(),
                title = "Remote Data",
                description = "Information retrieved via mock HTTP request"
            ),
            Item(
                id = UUID.randomUUID().toString(),
                title = "API Response",
                description = "Parsed JSON response from simulated API call"
            )
        )
    }
    
    override suspend fun refreshItems(): List<Item> {
        delay(900)
        
        if (Math.random() < 0.2) {
            throw Exception("Refresh failed - network unavailable")
        }
        
        return listOf(
            Item(
                id = UUID.randomUUID().toString(),
                title = "Refreshed Network Data",
                description = "Updated information from network refresh"
            ),
            Item(
                id = UUID.randomUUID().toString(),
                title = "Latest API Data",
                description = "Most recent data from API endpoint"
            )
        )
    }
}