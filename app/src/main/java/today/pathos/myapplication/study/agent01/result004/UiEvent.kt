package today.pathos.myapplication.study.agent01.result004

import today.pathos.myapplication.study.common.Item

sealed class UiEvent {
    data object ItemsLoaded : UiEvent()
    data class ItemAdded(val item: Item) : UiEvent()
    data class ItemRemoved(val itemId: String) : UiEvent()
    data class ItemUpdated(val item: Item) : UiEvent()
    data object ItemsRefreshed : UiEvent()
    data class Error(val message: String) : UiEvent()
    data object LoadingStarted : UiEvent()
    data object LoadingFinished : UiEvent()
    data object RefreshStarted : UiEvent()
    data object RefreshFinished : UiEvent()
}