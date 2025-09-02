package today.pathos.myapplication.study.agent03.result005

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import today.pathos.myapplication.study.common.BaseViewModel
import today.pathos.myapplication.study.common.Item
import today.pathos.myapplication.study.common.ScreenUiState

class HotColdStreamViewModel : BaseViewModel() {
    
    // Initial screen state (only used for initialization)
    var screenUiState by mutableStateOf<ScreenUiState>(ScreenUiState.Initializing)
        private set
    
    // Hot streams (SharedFlow) - shared among all collectors
    private val _operationEventsShared = MutableSharedFlow<OperationEvent>(
        replay = 10, // Keep last 10 events
        extraBufferCapacity = 100
    )
    val operationEvents: SharedFlow<OperationEvent> = _operationEventsShared.asSharedFlow()
    
    private val _realTimeUpdatesShared = MutableSharedFlow<RealTimeUpdate>(
        replay = 1, // Keep last update
        extraBufferCapacity = 50
    )
    val realTimeUpdates: SharedFlow<RealTimeUpdate> = _realTimeUpdatesShared.asSharedFlow()
    
    // Cold streams (Flow) - each collector gets its own instance
    private val _itemsState = MutableStateFlow<List<Item>>(emptyList())
    private val _loadingState = MutableStateFlow(false)
    private val _errorState = MutableStateFlow<String?>(null)
    
    // Hot stream for broadcasting status updates
    private val _statusBroadcast = MutableSharedFlow<String>(replay = 1)
    val statusBroadcast: SharedFlow<String> = _statusBroadcast.asSharedFlow()
    
    // Cold streams for individual consumers
    val itemsFlow: StateFlow<List<Item>> = _itemsState.asStateFlow()
    val isLoadingFlow: StateFlow<Boolean> = _loadingState.asStateFlow()
    val errorFlow: StateFlow<String?> = _errorState.asStateFlow()
    
    // Mixed hot/cold stream for filtered data (cold base, hot events)
    val filteredItemsFlow: Flow<List<Item>> = _itemsState
        .combineTransform(_operationEventsShared) { items, event ->
            // This creates a cold stream that reacts to hot events
            when (event.type) {
                OperationEventType.FILTER_HIGH_PRIORITY -> {
                    emit(items.filter { it.title.contains("1") || it.title.contains("3") })
                }
                OperationEventType.FILTER_RECENT -> {
                    val recentThreshold = System.currentTimeMillis() - 5000
                    emit(items.filter { it.timestamp > recentThreshold })
                }
                else -> emit(items)
            }
        }
    
    // Temperature monitoring (simulating hot stream behavior)
    private val temperatureFlow: Flow<Int> = flow {
        while (true) {
            emit((20..30).random()) // Simulate temperature readings
            delay(2000)
        }
    }.shareIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        replay = 1
    )
    
    // Data classes for events
    data class OperationEvent(
        val type: OperationEventType,
        val timestamp: Long,
        val details: String
    )
    
    data class RealTimeUpdate(
        val message: String,
        val timestamp: Long,
        val severity: Severity
    )
    
    enum class OperationEventType {
        INITIAL_LOAD,
        REFRESH,
        ADD_ITEM,
        REMOVE_ITEM,
        UPDATE_ITEM,
        FILTER_HIGH_PRIORITY,
        FILTER_RECENT,
        CLEAR_FILTER
    }
    
    enum class Severity { INFO, WARNING, ERROR }
    
    init {
        setupHotColdStreams()
        triggerInitialLoad()
    }
    
    private fun setupHotColdStreams() {
        // Hot stream for real-time status updates
        viewModelScope.launch {
            _statusBroadcast.emit("Hot/Cold Stream System Initialized")
        }
        
        // Monitor temperature (hot stream example)
        temperatureFlow
            .onEach { temp ->
                if (temp > 28) {
                    _realTimeUpdatesShared.emit(
                        RealTimeUpdate(
                            message = "High temperature detected: ${temp}°C",
                            timestamp = System.currentTimeMillis(),
                            severity = Severity.WARNING
                        )
                    )
                }
            }
            .launchIn(viewModelScope)
        
        // Operation event processor (reacts to hot events, produces cold streams)
        operationEvents
            .filter { it.type in listOf(OperationEventType.ADD_ITEM, OperationEventType.REMOVE_ITEM, OperationEventType.UPDATE_ITEM) }
            .onEach { event ->
                _realTimeUpdatesShared.emit(
                    RealTimeUpdate(
                        message = "Operation completed: ${event.details}",
                        timestamp = event.timestamp,
                        severity = Severity.INFO
                    )
                )
            }
            .launchIn(viewModelScope)
    }
    
    private fun triggerInitialLoad() {
        viewModelScope.launch {
            try {
                _loadingState.value = true
                _errorState.value = null
                
                broadcastOperationEvent(
                    OperationEventType.INITIAL_LOAD,
                    "Starting initial load with 2s delay"
                )
                
                delay(2000)
                
                if (shouldSimulateError()) {
                    throw Exception("Initial load failed")
                }
                
                val items = generateInitialItems()
                _itemsState.value = items
                
                broadcastOperationEvent(
                    OperationEventType.INITIAL_LOAD,
                    "Initial load completed with ${items.size} items"
                )
                
                _statusBroadcast.emit("System ready - Hot streams active")
                updateScreenStateAfterInitialLoad(null)
                
            } catch (e: Exception) {
                _errorState.value = e.message
                _realTimeUpdatesShared.emit(
                    RealTimeUpdate(
                        message = "Initial load failed: ${e.message}",
                        timestamp = System.currentTimeMillis(),
                        severity = Severity.ERROR
                    )
                )
                updateScreenStateAfterInitialLoad(e.message)
            } finally {
                _loadingState.value = false
            }
        }
    }
    
    private fun updateScreenStateAfterInitialLoad(errorMessage: String?) {
        viewModelScope.launch {
            screenUiState = if (errorMessage != null) {
                ScreenUiState.Failed(errorMessage)
            } else {
                ScreenUiState.Succeed
            }
        }
    }
    
    private suspend fun broadcastOperationEvent(type: OperationEventType, details: String) {
        _operationEventsShared.emit(
            OperationEvent(
                type = type,
                timestamp = System.currentTimeMillis(),
                details = details
            )
        )
    }
    
    fun refresh() {
        viewModelScope.launch {
            try {
                _loadingState.value = true
                _errorState.value = null
                
                broadcastOperationEvent(OperationEventType.REFRESH, "Refreshing data...")
                
                delay(1000)
                
                if (shouldSimulateError()) {
                    throw Exception("Refresh failed")
                }
                
                val items = generateInitialItems()
                _itemsState.value = items
                
                broadcastOperationEvent(
                    OperationEventType.REFRESH,
                    "Refresh completed with ${items.size} items"
                )
                
            } catch (e: Exception) {
                _errorState.value = e.message
                _realTimeUpdatesShared.emit(
                    RealTimeUpdate(
                        message = "Refresh failed: ${e.message}",
                        timestamp = System.currentTimeMillis(),
                        severity = Severity.ERROR
                    )
                )
            } finally {
                _loadingState.value = false
            }
        }
    }
    
    fun addItem() {
        viewModelScope.launch {
            try {
                _loadingState.value = true
                _errorState.value = null
                
                delay(300)
                
                if (shouldSimulateError()) {
                    throw Exception("Add operation failed")
                }
                
                val newItem = generateNewItem(_itemsState.value.size)
                _itemsState.value = _itemsState.value + newItem
                
                broadcastOperationEvent(
                    OperationEventType.ADD_ITEM,
                    "Added item: ${newItem.id}"
                )
                
            } catch (e: Exception) {
                _errorState.value = e.message
            } finally {
                _loadingState.value = false
            }
        }
    }
    
    fun removeItem(itemId: String) {
        viewModelScope.launch {
            try {
                _loadingState.value = true
                _errorState.value = null
                
                delay(300)
                
                if (shouldSimulateError()) {
                    throw Exception("Remove operation failed")
                }
                
                _itemsState.value = _itemsState.value.filter { it.id != itemId }
                
                broadcastOperationEvent(
                    OperationEventType.REMOVE_ITEM,
                    "Removed item: $itemId"
                )
                
            } catch (e: Exception) {
                _errorState.value = e.message
            } finally {
                _loadingState.value = false
            }
        }
    }
    
    fun updateItem(item: Item) {
        viewModelScope.launch {
            try {
                _loadingState.value = true
                _errorState.value = null
                
                delay(300)
                
                if (shouldSimulateError()) {
                    throw Exception("Update operation failed")
                }
                
                val updatedItem = item.copy(
                    title = "${item.title} (Updated)",
                    timestamp = System.currentTimeMillis()
                )
                
                _itemsState.value = _itemsState.value.map { currentItem ->
                    if (currentItem.id == updatedItem.id) updatedItem else currentItem
                }
                
                broadcastOperationEvent(
                    OperationEventType.UPDATE_ITEM,
                    "Updated item: ${item.id}"
                )
                
            } catch (e: Exception) {
                _errorState.value = e.message
            } finally {
                _loadingState.value = false
            }
        }
    }
    
    fun filterHighPriority() {
        viewModelScope.launch {
            broadcastOperationEvent(
                OperationEventType.FILTER_HIGH_PRIORITY,
                "Applied high priority filter"
            )
        }
    }
    
    fun filterRecent() {
        viewModelScope.launch {
            broadcastOperationEvent(
                OperationEventType.FILTER_RECENT,
                "Applied recent items filter"
            )
        }
    }
    
    fun clearFilter() {
        viewModelScope.launch {
            broadcastOperationEvent(
                OperationEventType.CLEAR_FILTER,
                "Cleared all filters"
            )
        }
    }
}