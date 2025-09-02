package today.pathos.myapplication.study.agent02.result004

import today.pathos.myapplication.study.common.Item

data class ReduxState(
    val items: List<Item> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val isInitialized: Boolean = false
)