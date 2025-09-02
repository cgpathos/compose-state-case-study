package today.pathos.myapplication.study.agent01.result002

import today.pathos.myapplication.study.common.Item

data class UiState(
    val isLoading: Boolean = false,
    val items: List<Item> = emptyList(),
    val error: String? = null,
    val isRefreshing: Boolean = false
) {
    val hasError: Boolean get() = error != null
    val isEmpty: Boolean get() = items.isEmpty() && !isLoading && !hasError
}