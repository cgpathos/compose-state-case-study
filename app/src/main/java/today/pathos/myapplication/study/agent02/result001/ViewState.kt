package today.pathos.myapplication.study.agent02.result001

import today.pathos.myapplication.study.common.Item

data class ViewState(
    val items: List<Item> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRefreshing: Boolean = false
)