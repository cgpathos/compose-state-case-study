package today.pathos.myapplication.study.agent04.result002

import today.pathos.myapplication.study.common.Item

// Note: Parcelable functionality removed due to missing parcelize plugin configuration
data class ParcelableItem(
    val id: String,
    val title: String,
    val description: String,
    val timestamp: Long
) {
    
    constructor(item: Item) : this(
        id = item.id,
        title = item.title,
        description = item.description,
        timestamp = item.timestamp
    )
    
    fun toItem(): Item = Item(
        id = id,
        title = title,
        description = description,
        timestamp = timestamp
    )
}

// Note: Parcelable functionality removed due to missing parcelize plugin configuration
data class ItemListState(
    val items: List<ParcelableItem>,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)