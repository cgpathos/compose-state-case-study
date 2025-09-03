package today.pathos.myapplication.study.agent05.result007

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import today.pathos.myapplication.study.common.BaseViewModel
import today.pathos.myapplication.study.common.Item
import today.pathos.myapplication.study.common.ScreenUiState

class ProviderViewModel : BaseViewModel() {
    
    private val _screenUiState = MutableStateFlow<ScreenUiState>(ScreenUiState.Initializing)
    val screenUiState: StateFlow<ScreenUiState> = _screenUiState.asStateFlow()
    
    private val _items = MutableStateFlow<List<Item>>(emptyList())
    val items: StateFlow<List<Item>> = _items.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // Provider configuration
    private val _selectedConfig = MutableStateFlow("default")
    val selectedConfig: StateFlow<String> = _selectedConfig.asStateFlow()
    
    private val _selectedDaoType = MutableStateFlow("mock")
    val selectedDaoType: StateFlow<String> = _selectedDaoType.asStateFlow()
    
    private val _selectedApiType = MutableStateFlow("mock")
    val selectedApiType: StateFlow<String> = _selectedApiType.asStateFlow()
    
    // Current service instance - provided by dependency injection pattern
    private var itemService: ItemService? = null
    
    // Provider statistics
    private val _serviceCreationCount = MutableStateFlow(0)
    val serviceCreationCount: StateFlow<Int> = _serviceCreationCount.asStateFlow()
    
    private val _currentServiceConfig = MutableStateFlow<AppConfig?>(null)
    val currentServiceConfig: StateFlow<AppConfig?> = _currentServiceConfig.asStateFlow()
    
    // Available options from provider
    private val _availableConfigs = MutableStateFlow(ProviderRegistry.dependencyProvider.getAvailableConfigurations())
    val availableConfigs: StateFlow<List<String>> = _availableConfigs.asStateFlow()
    
    private val _availableDaoTypes = MutableStateFlow(ProviderRegistry.dependencyProvider.getAvailableDaoTypes())
    val availableDaoTypes: StateFlow<List<String>> = _availableDaoTypes.asStateFlow()
    
    private val _availableApiTypes = MutableStateFlow(ProviderRegistry.dependencyProvider.getAvailableApiTypes())
    val availableApiTypes: StateFlow<List<String>> = _availableApiTypes.asStateFlow()
    
    // NO init{} block - dependencies injected via provider pattern
    
    fun initializeWithProvider() {
        createServiceFromProvider()
        loadItems()
    }
    
    private fun createServiceFromProvider() {
        itemService = ProviderRegistry.dependencyProvider.provideItemService(
            configType = _selectedConfig.value,
            daoType = _selectedDaoType.value,
            apiType = _selectedApiType.value
        )
        
        _serviceCreationCount.value = _serviceCreationCount.value + 1
        
        // Update current configuration
        _currentServiceConfig.value = ProviderRegistry.dependencyProvider.provideAppConfig(_selectedConfig.value)
    }
    
    fun updateConfiguration(
        configType: String = _selectedConfig.value,
        daoType: String = _selectedDaoType.value,
        apiType: String = _selectedApiType.value
    ) {
        _selectedConfig.value = configType
        _selectedDaoType.value = daoType
        _selectedApiType.value = apiType
        
        // Recreate service with new configuration
        createServiceFromProvider()
        loadItems()
    }
    
    fun usePresetConfiguration(preset: String) {
        val service = when (preset) {
            "default" -> ProviderRegistry.createDefaultService()
            "offline" -> ProviderRegistry.createOfflineService()
            "performance" -> ProviderRegistry.createHighPerformanceService()
            else -> ProviderRegistry.createDefaultService()
        }
        
        itemService = service
        _serviceCreationCount.value = _serviceCreationCount.value + 1
        
        // Update configuration states
        when (preset) {
            "offline" -> {
                _selectedConfig.value = "offline"
                _currentServiceConfig.value = ProviderRegistry.dependencyProvider.provideAppConfig("offline")
            }
            "performance" -> {
                _selectedConfig.value = "performance"
                _currentServiceConfig.value = ProviderRegistry.dependencyProvider.provideAppConfig("performance")
            }
            else -> {
                _selectedConfig.value = "default"
                _currentServiceConfig.value = ProviderRegistry.dependencyProvider.provideAppConfig("default")
            }
        }
        
        loadItems()
    }
    
    private fun loadItems() {
        val service = itemService ?: return
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val loadedItems = service.loadItems()
                _items.value = loadedItems
                _screenUiState.value = ScreenUiState.Succeed
                
            } catch (e: Exception) {
                _error.value = e.message
                _screenUiState.value = ScreenUiState.Failed(e.message ?: "Unknown error")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun refreshItems() {
        val service = itemService ?: return
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val refreshedItems = service.refreshItems()
                _items.value = refreshedItems
                
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun addItem(item: Item) {
        val service = itemService ?: return
        
        viewModelScope.launch {
            try {
                service.addItem(item)
                // Reload to get updated list
                val updatedItems = service.loadItems()
                _items.value = updatedItems
                
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    fun removeItem(itemId: String) {
        val service = itemService ?: return
        
        viewModelScope.launch {
            try {
                service.removeItem(itemId)
                // Reload to get updated list
                val updatedItems = service.loadItems()
                _items.value = updatedItems
                
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    fun updateItem(item: Item) {
        val service = itemService ?: return
        
        viewModelScope.launch {
            try {
                service.updateItem(item)
                // Reload to get updated list
                val updatedItems = service.loadItems()
                _items.value = updatedItems
                
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun resetStatistics() {
        _serviceCreationCount.value = 0
    }
    
    fun getCurrentConfigSummary(): String {
        val config = _currentServiceConfig.value ?: return "No configuration"
        return "Network: ${config.enableNetworkSync}, Cache: ${config.cacheTimeout}ms, Max: ${config.maxItems}"
    }
    
    fun isServiceInitialized(): Boolean = itemService != null
}