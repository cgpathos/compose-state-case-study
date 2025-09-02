package today.pathos.myapplication.study.agent01.result001

import today.pathos.myapplication.study.common.Item

sealed class UiState {
    data object Loading : UiState()
    data class Success(val items: List<Item>) : UiState()
    data class Error(val message: String) : UiState()
}