package today.pathos.myapplication.study.agent02.result005

import today.pathos.myapplication.study.common.Item

data class UdfState(
    val items: List<Item> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val loadingStage: LoadingStage = LoadingStage.Initial
)

enum class LoadingStage {
    Initial,
    Loading,
    Loaded,
    Failed
}