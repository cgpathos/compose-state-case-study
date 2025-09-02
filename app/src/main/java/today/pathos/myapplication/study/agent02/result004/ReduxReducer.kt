package today.pathos.myapplication.study.agent02.result004

object ReduxReducer {
    
    fun reduce(state: ReduxState, action: ReduxAction): ReduxState {
        return when (action) {
            is ReduxAction.LoadItems -> state
            
            is ReduxAction.LoadItemsStart -> state.copy(
                isLoading = true,
                error = null
            )
            
            is ReduxAction.LoadItemsSuccess -> state.copy(
                items = action.items,
                isLoading = false,
                isInitialized = true,
                error = null
            )
            
            is ReduxAction.LoadItemsFailure -> state.copy(
                isLoading = false,
                error = action.error
            )
            
            is ReduxAction.RefreshItems -> state
            
            is ReduxAction.RefreshItemsStart -> state.copy(
                isRefreshing = true,
                error = null
            )
            
            is ReduxAction.RefreshItemsSuccess -> state.copy(
                items = action.items,
                isRefreshing = false,
                error = null
            )
            
            is ReduxAction.RefreshItemsFailure -> state.copy(
                isRefreshing = false,
                error = action.error
            )
            
            is ReduxAction.AddItem -> state.copy(
                items = state.items + action.item
            )
            
            is ReduxAction.RemoveItem -> state.copy(
                items = state.items.filterNot { it.id == action.itemId }
            )
            
            is ReduxAction.UpdateItem -> state.copy(
                items = state.items.map { item ->
                    if (item.id == action.item.id) action.item else item
                }
            )
            
            is ReduxAction.ClearError -> state.copy(
                error = null
            )
        }
    }
}