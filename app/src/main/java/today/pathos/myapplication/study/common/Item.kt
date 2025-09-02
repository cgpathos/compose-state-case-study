package today.pathos.myapplication.study.common

data class Item(
    val id: String,
    val title: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)