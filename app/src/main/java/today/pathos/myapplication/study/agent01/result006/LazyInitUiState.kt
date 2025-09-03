package today.pathos.myapplication.study.agent01.result006

import today.pathos.myapplication.study.common.Item

data class LazyInitUiState(
    val isInitialized: Boolean = false,
    val items: List<Item> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
) {
    val hasError: Boolean get() = error != null
    val isEmpty: Boolean get() = items.isEmpty() && !isLoading && isInitialized
    val shouldShowContent: Boolean get() = isInitialized && !hasError
}