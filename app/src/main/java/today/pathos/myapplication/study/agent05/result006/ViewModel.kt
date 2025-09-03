package today.pathos.myapplication.study.agent05.result006

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import today.pathos.myapplication.study.common.BaseViewModel
import today.pathos.myapplication.study.common.Item
import today.pathos.myapplication.study.common.ScreenUiState

class LazyRepositoryViewModel : BaseViewModel() {
    
    private val _screenUiState = MutableStateFlow<ScreenUiState>(ScreenUiState.Initializing)
    val screenUiState: StateFlow<ScreenUiState> = _screenUiState.asStateFlow()
    
    private val _items = MutableStateFlow<List<Item>>(emptyList())
    val items: StateFlow<List<Item>> = _items.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // Repository instances - lazy initialization
    private val lazyRepository: Lazy<LazyItemRepository> = lazy {
        LazyItemRepository()
    }
    
    private val cacheRepository: Lazy<CacheLazyRepository> = lazy {
        CacheLazyRepository()
    }
    
    private val databaseRepository: Lazy<DatabaseLazyRepository> = lazy {
        DatabaseLazyRepository()
    }
    
    // Current repository selection
    private val _selectedRepositoryType = MutableStateFlow("lazy")
    val selectedRepositoryType: StateFlow<String> = _selectedRepositoryType.asStateFlow()
    
    private val _repositoryInitialized = MutableStateFlow(false)
    val repositoryInitialized: StateFlow<Boolean> = _repositoryInitialized.asStateFlow()
    
    // Repository access statistics
    private val _repositoryAccessCount = MutableStateFlow(0)
    val repositoryAccessCount: StateFlow<Int> = _repositoryAccessCount.asStateFlow()
    
    // NO init{} block - repositories are lazily initialized
    
    private fun getCurrentRepository(): ItemRepository {
        _repositoryAccessCount.value = _repositoryAccessCount.value + 1
        
        return when (_selectedRepositoryType.value) {
            "lazy" -> {
                // Accessing lazy property triggers initialization
                lazyRepository.value
            }
            "cache" -> {
                // Accessing lazy property triggers initialization  
                cacheRepository.value
            }
            "database" -> {
                // Accessing lazy property triggers initialization
                databaseRepository.value
            }
            else -> lazyRepository.value
        }
    }
    
    fun manualInitialization() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val repository = getCurrentRepository()
                val loadedItems = repository.loadItems()
                
                _items.value = loadedItems
                _repositoryInitialized.value = repository.isInitialized()
                _screenUiState.value = ScreenUiState.Succeed
                
            } catch (e: Exception) {
                _error.value = e.message
                _screenUiState.value = ScreenUiState.Failed(e.message ?: "Unknown error")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun switchRepository(repositoryType: String) {
        _selectedRepositoryType.value = repositoryType
        _repositoryInitialized.value = getCurrentRepository().isInitialized()
        
        // Load from new repository
        manualInitialization()
    }
    
    fun refreshItems() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val repository = getCurrentRepository()
                val refreshedItems = repository.refreshItems()
                
                _items.value = refreshedItems
                _repositoryInitialized.value = repository.isInitialized()
                
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun addItem(item: Item) {
        viewModelScope.launch {
            try {
                val repository = getCurrentRepository()
                repository.addItem(item)
                
                // Reload to get updated items
                val updatedItems = repository.loadItems()
                _items.value = updatedItems
                
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    fun removeItem(itemId: String) {
        viewModelScope.launch {
            try {
                val repository = getCurrentRepository()
                repository.removeItem(itemId)
                
                // Reload to get updated items
                val updatedItems = repository.loadItems()
                _items.value = updatedItems
                
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    fun updateItem(item: Item) {
        viewModelScope.launch {
            try {
                val repository = getCurrentRepository()
                repository.updateItem(item)
                
                // Reload to get updated items  
                val updatedItems = repository.loadItems()
                _items.value = updatedItems
                
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    fun startFlowCollection() {
        viewModelScope.launch {
            try {
                val repository = getCurrentRepository()
                repository.getItemsFlow()
                    .collect { flowItems ->
                        _items.value = flowItems
                        _repositoryInitialized.value = repository.isInitialized()
                        _screenUiState.value = ScreenUiState.Succeed
                    }
                    
            } catch (e: Exception) {
                _error.value = e.message
                _screenUiState.value = ScreenUiState.Failed(e.message ?: "Unknown error")
            }
        }
    }
    
    // Repository-specific operations
    fun resetLazyRepository() {
        if (lazyRepository.isInitialized()) {
            lazyRepository.value.forceReset()
            _repositoryInitialized.value = false
            _items.value = emptyList()
        }
    }
    
    fun invalidateCacheRepository() {
        if (cacheRepository.isInitialized()) {
            cacheRepository.value.invalidateCache()
            _repositoryInitialized.value = false
        }
    }
    
    fun closeDatabaseRepository() {
        if (databaseRepository.isInitialized()) {
            databaseRepository.value.closeConnection()
            _repositoryInitialized.value = false
            _items.value = emptyList()
        }
    }
    
    fun getCacheAge(): Long {
        return if (cacheRepository.isInitialized()) {
            cacheRepository.value.getCacheAge()
        } else {
            -1
        }
    }
    
    fun getDatabaseConnection(): String? {
        return if (databaseRepository.isInitialized()) {
            databaseRepository.value.getDatabaseConnection()
        } else {
            null
        }
    }
    
    fun isRepositoryLazilyInitialized(type: String): Boolean {
        return when (type) {
            "lazy" -> lazyRepository.isInitialized()
            "cache" -> cacheRepository.isInitialized()
            "database" -> databaseRepository.isInitialized()
            else -> false
        }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun resetStatistics() {
        _repositoryAccessCount.value = 0
    }
}