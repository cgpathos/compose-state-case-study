package today.pathos.myapplication.study.agent03.result007

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import today.pathos.myapplication.study.common.BaseViewModel
import today.pathos.myapplication.study.common.Item
import today.pathos.myapplication.study.common.ScreenUiState

class SwitchableSourceViewModel : BaseViewModel() {
    
    private val _screenUiState = MutableStateFlow<ScreenUiState>(ScreenUiState.Initializing)
    val screenUiState: StateFlow<ScreenUiState> = _screenUiState.asStateFlow()
    
    private val _items = MutableStateFlow<List<Item>>(emptyList())
    val items: StateFlow<List<Item>> = _items.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // Available data sources
    private val availableSources = listOf(
        LocalCacheSource(),
        NetworkApiSource(),
        DatabaseSource(),
        WebSocketSource(),
        HybridSource()
    )
    
    private val _currentSource = MutableStateFlow<DataSource>(availableSources[0])
    val currentSource: StateFlow<DataSource> = _currentSource.asStateFlow()
    
    private val _availableSourcesList = MutableStateFlow(availableSources)
    val availableSourcesList: StateFlow<List<DataSource>> = _availableSourcesList.asStateFlow()
    
    // Source switching trigger
    private val sourceSwitch = MutableSharedFlow<DataSource>()
    
    // Active jobs for cancellation
    private var currentLoadJob: Job? = null
    
    // Statistics
    private val _sourceHistory = MutableStateFlow<List<String>>(emptyList())
    val sourceHistory: StateFlow<List<String>> = _sourceHistory.asStateFlow()
    
    private val _totalSwitches = MutableStateFlow(0)
    val totalSwitches: StateFlow<Int> = _totalSwitches.asStateFlow()
    
    // Manual initialization method - NO init{} block
    fun initialize() {
        setupSourceSwitching()
        loadFromCurrentSource()
    }
    
    private fun setupSourceSwitching() {
        // Reactive source switching
        sourceSwitch
            .onEach { newSource ->
                // Cancel previous loading
                currentLoadJob?.cancel()
                
                // Update current source
                _currentSource.value = newSource
                _totalSwitches.value = _totalSwitches.value + 1
                
                // Update history
                val currentHistory = _sourceHistory.value.toMutableList()
                currentHistory.add(newSource.name)
                if (currentHistory.size > 5) {
                    currentHistory.removeAt(0) // Keep only last 5
                }
                _sourceHistory.value = currentHistory
                
                // Load from new source
                loadFromCurrentSource()
            }
            .launchIn(viewModelScope)
    }
    
    private fun loadFromCurrentSource() {
        currentLoadJob?.cancel()
        
        currentLoadJob = viewModelScope.launch {
            val source = _currentSource.value
            _isLoading.value = true
            _error.value = null
            
            try {
                source.getItemsFlow()
                    .catch { exception ->
                        _error.value = "Source '${source.name}': ${exception.message}"
                        _screenUiState.value = ScreenUiState.Failed(exception.message ?: "Unknown error")
                    }
                    .collect { itemsList ->
                        _items.value = itemsList
                        _screenUiState.value = ScreenUiState.Succeed
                    }
                    
            } catch (e: Exception) {
                _error.value = "Source '${source.name}': ${e.message}"
                _screenUiState.value = ScreenUiState.Failed(e.message ?: "Unknown error")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Public methods
    fun switchToSource(source: DataSource) {
        viewModelScope.launch {
            sourceSwitch.emit(source)
        }
    }
    
    fun refreshCurrentSource() {
        currentLoadJob?.cancel()
        
        currentLoadJob = viewModelScope.launch {
            val source = _currentSource.value
            _isLoading.value = true
            _error.value = null
            
            try {
                source.getRefreshFlow()
                    .catch { exception ->
                        _error.value = "Refresh '${source.name}': ${exception.message}"
                    }
                    .collect { itemsList ->
                        _items.value = itemsList
                    }
                    
            } catch (e: Exception) {
                _error.value = "Refresh '${source.name}': ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun switchToNextSource() {
        val currentIndex = availableSources.indexOf(_currentSource.value)
        val nextIndex = (currentIndex + 1) % availableSources.size
        val nextSource = availableSources[nextIndex]
        switchToSource(nextSource)
    }
    
    fun switchToPreviousSource() {
        val currentIndex = availableSources.indexOf(_currentSource.value)
        val previousIndex = if (currentIndex == 0) availableSources.size - 1 else currentIndex - 1
        val previousSource = availableSources[previousIndex]
        switchToSource(previousSource)
    }
    
    fun addItem(item: Item) {
        val currentItems = _items.value
        _items.value = currentItems + item
    }
    
    fun removeItem(itemId: String) {
        val currentItems = _items.value
        _items.value = currentItems.filterNot { it.id == itemId }
    }
    
    fun updateItem(item: Item) {
        val currentItems = _items.value
        _items.value = currentItems.map { if (it.id == item.id) item else it }
    }
    
    fun clearHistory() {
        _sourceHistory.value = emptyList()
        _totalSwitches.value = 0
    }
    
    fun cancelCurrentOperation() {
        currentLoadJob?.cancel()
        _isLoading.value = false
    }
    
    fun getCurrentSourceIndex(): Int = availableSources.indexOf(_currentSource.value)
    
    override fun onCleared() {
        super.onCleared()
        currentLoadJob?.cancel()
    }
}