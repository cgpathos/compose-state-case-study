package today.pathos.myapplication.study.common

sealed class ScreenUiState {
    object Initializing : ScreenUiState()
    object Succeed : ScreenUiState()
    data class Failed(val error: String) : ScreenUiState()
}