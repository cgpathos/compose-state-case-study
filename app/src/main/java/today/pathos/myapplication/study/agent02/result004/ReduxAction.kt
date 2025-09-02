package today.pathos.myapplication.study.agent02.result004

import today.pathos.myapplication.study.common.Item

sealed class ReduxAction {
    object LoadItems : ReduxAction()
    object LoadItemsStart : ReduxAction()
    data class LoadItemsSuccess(val items: List<Item>) : ReduxAction()
    data class LoadItemsFailure(val error: String) : ReduxAction()
    
    object RefreshItems : ReduxAction()
    object RefreshItemsStart : ReduxAction()
    data class RefreshItemsSuccess(val items: List<Item>) : ReduxAction()
    data class RefreshItemsFailure(val error: String) : ReduxAction()
    
    data class AddItem(val item: Item) : ReduxAction()
    data class RemoveItem(val itemId: String) : ReduxAction()
    data class UpdateItem(val item: Item) : ReduxAction()
    
    object ClearError : ReduxAction()
}