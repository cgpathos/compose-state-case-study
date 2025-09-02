package today.pathos.myapplication.study.agent05.result001

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import today.pathos.myapplication.study.common.BaseViewModel
import today.pathos.myapplication.study.common.Item
import today.pathos.myapplication.study.common.ScreenUiState

data class Agent05UiState(
    val screenState: ScreenUiState = ScreenUiState.Initializing,
    val items: List<Item> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class Agent05ViewModel(
    private val useCases: ItemUseCases
) : BaseViewModel() {
    
    private val _uiState = MutableStateFlow(Agent05UiState())
    val uiState: StateFlow<Agent05UiState> = _uiState.asStateFlow()
    
    init {
        loadInitialItems()
    }
    
    private fun loadInitialItems() {
        viewModelScope.launch {
            try {
                val result = useCases.getItems()
                result.fold(
                    onSuccess = { items ->
                        _uiState.value = _uiState.value.copy(
                            screenState = ScreenUiState.Succeed,
                            items = items,
                            isLoading = false,
                            error = null
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            screenState = ScreenUiState.Failed(exception.message ?: "Unknown error"),
                            isLoading = false,
                            error = exception.message
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    screenState = ScreenUiState.Failed(e.message ?: "Unknown error"),
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    fun addItem(title: String, description: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val result = useCases.addItem(title, description)
                result.fold(
                    onSuccess = { items ->
                        _uiState.value = _uiState.value.copy(
                            items = items,
                            isLoading = false,
                            error = null
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = exception.message
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    fun removeItem(itemId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val result = useCases.removeItem(itemId)
                result.fold(
                    onSuccess = { items ->
                        _uiState.value = _uiState.value.copy(
                            items = items,
                            isLoading = false,
                            error = null
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = exception.message
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    fun updateItem(item: Item) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val result = useCases.updateItem(item)
                result.fold(
                    onSuccess = { items ->
                        _uiState.value = _uiState.value.copy(
                            items = items,
                            isLoading = false,
                            error = null
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = exception.message
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    fun refreshItems() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val result = useCases.refreshItems()
                result.fold(
                    onSuccess = { items ->
                        _uiState.value = _uiState.value.copy(
                            items = items,
                            isLoading = false,
                            error = null
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = exception.message
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    companion object {
        fun create(): Agent05ViewModel {
            val repository = ItemRepositoryImpl()
            val useCases = ItemUseCases.create(repository)
            return Agent05ViewModel(useCases)
        }
    }
}