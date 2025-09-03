package today.pathos.myapplication.study.agent01.result007

import today.pathos.myapplication.study.common.Item

sealed class FactoryUiState {
    data object Uninitialized : FactoryUiState()
    data object Loading : FactoryUiState()
    data class Success(
        val items: List<Item>,
        val isRefreshing: Boolean = false,
        val lastUpdated: Long = System.currentTimeMillis()
    ) : FactoryUiState()
    data class Error(
        val message: String,
        val canRetry: Boolean = true,
        val lastAttempt: Long = System.currentTimeMillis()
    ) : FactoryUiState()
    
    companion object {
        fun createInitialState(): FactoryUiState = Uninitialized
        
        fun createLoadingState(): FactoryUiState = Loading
        
        fun createSuccessState(items: List<Item>): FactoryUiState = Success(items)
        
        fun createSuccessStateWithRefresh(items: List<Item>): FactoryUiState = 
            Success(items, isRefreshing = true)
        
        fun createErrorState(message: String, canRetry: Boolean = true): FactoryUiState = 
            Error(message, canRetry)
        
        fun createFatalErrorState(message: String): FactoryUiState = 
            Error(message, canRetry = false)
    }
}