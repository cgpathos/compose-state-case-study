package today.pathos.myapplication.study.agent02.result005

sealed class UdfEffect {
    data class ShowSnackbar(val message: String) : UdfEffect()
    data class LogError(val error: String) : UdfEffect()
    object ScrollToTop : UdfEffect()
}