package today.pathos.myapplication.study.agent01.result005

import androidx.compose.runtime.*
import kotlinx.coroutines.flow.*
import today.pathos.myapplication.study.common.Item

/**
 * Simulates Molecule-like pattern where state is derived from reactive streams
 * and updates are managed through emitted events/signals
 */

data class MoleculeState(
    val items: List<Item> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
) {
    val hasError: Boolean get() = error != null
    val isEmpty: Boolean get() = items.isEmpty() && !isLoading && !hasError
    val isContentLoading: Boolean get() = isLoading && items.isEmpty()
}

sealed class Action {
    data object LoadItems : Action()
    data object AddItem : Action()
    data class RemoveItem(val itemId: String) : Action()
    data class UpdateItem(val item: Item) : Action()
    data object Refresh : Action()
    data object ClearError : Action()
}

sealed class Signal {
    data class ItemsLoaded(val items: List<Item>) : Signal()
    data class ItemAdded(val item: Item) : Signal()
    data class ItemRemoved(val itemId: String) : Signal()
    data class ItemUpdated(val item: Item) : Signal()
    data class ItemsRefreshed(val items: List<Item>) : Signal()
    data class Error(val message: String) : Signal()
    data class LoadingStateChanged(val isLoading: Boolean) : Signal()
    data class RefreshingStateChanged(val isRefreshing: Boolean) : Signal()
}