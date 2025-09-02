package today.pathos.myapplication.study.agent02.result004

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReduxStore(private val scope: CoroutineScope) {
    
    private val _state = MutableStateFlow(ReduxState())
    val state: StateFlow<ReduxState> = _state.asStateFlow()
    
    fun dispatch(action: ReduxAction) {
        val currentState = _state.value
        val newState = ReduxReducer.reduce(currentState, action)
        _state.value = newState
        
        // Handle side effects (async actions)
        handleSideEffects(action)
    }
    
    private fun handleSideEffects(action: ReduxAction) {
        when (action) {
            is ReduxAction.LoadItems -> {
                scope.launch {
                    dispatch(ReduxAction.LoadItemsStart)
                    try {
                        val items = loadItemsFromRepository()
                        dispatch(ReduxAction.LoadItemsSuccess(items))
                    } catch (e: Exception) {
                        dispatch(ReduxAction.LoadItemsFailure(e.message ?: "Unknown error"))
                    }
                }
            }
            
            is ReduxAction.RefreshItems -> {
                scope.launch {
                    dispatch(ReduxAction.RefreshItemsStart)
                    try {
                        val items = loadItemsFromRepository()
                        dispatch(ReduxAction.RefreshItemsSuccess(items))
                    } catch (e: Exception) {
                        dispatch(ReduxAction.RefreshItemsFailure(e.message ?: "Refresh failed"))
                    }
                }
            }
            
            else -> {
                // No side effects for other actions
            }
        }
    }
    
    private suspend fun loadItemsFromRepository(): List<today.pathos.myapplication.study.common.Item> {
        kotlinx.coroutines.delay(2000) // Simulate network delay
        
        if (kotlin.random.Random.nextDouble() < 0.2) { // 20% chance of error
            throw Exception("Redux async operation failed")
        }
        
        return (1..5).map { index ->
            today.pathos.myapplication.study.common.Item(
                id = "item_$index",
                title = "Redux Item $index",
                description = "Description for Redux item $index"
            )
        }
    }
}