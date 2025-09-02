package today.pathos.myapplication.study.agent02.result005

import today.pathos.myapplication.study.common.Item

sealed class UdfEvent {
    object LoadItems : UdfEvent()
    object RefreshItems : UdfEvent()
    data class AddItem(val item: Item) : UdfEvent()
    data class RemoveItem(val itemId: String) : UdfEvent()
    data class UpdateItem(val item: Item) : UdfEvent()
    object RetryLoad : UdfEvent()
    object ClearError : UdfEvent()
}