package today.pathos.myapplication.study.agent01.result004

import today.pathos.myapplication.study.common.Item

data class UiState(
    val items: List<Item> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
) {
    val hasError: Boolean get() = error != null
    val isEmpty: Boolean get() = items.isEmpty() && !isLoading && !hasError
    val isContentLoading: Boolean get() = isLoading && items.isEmpty()
}