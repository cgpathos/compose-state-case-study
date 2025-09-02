package today.pathos.myapplication.study.agent02.result001

import today.pathos.myapplication.study.common.Item

sealed class ViewIntent {
    object LoadItems : ViewIntent()
    object RefreshItems : ViewIntent()
    data class AddItem(val item: Item) : ViewIntent()
    data class RemoveItem(val itemId: String) : ViewIntent()
    data class UpdateItem(val item: Item) : ViewIntent()
}