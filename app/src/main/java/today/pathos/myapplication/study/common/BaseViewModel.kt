package today.pathos.myapplication.study.common

import androidx.lifecycle.ViewModel
import kotlin.random.Random

abstract class BaseViewModel : ViewModel() {
    
    protected fun generateInitialItems(): List<Item> {
        return (1..5).map { index ->
            Item(
                id = "item_$index",
                title = "Item $index",
                description = "Description for item $index"
            )
        }
    }
    
    protected fun shouldSimulateError(): Boolean {
        return Random.nextDouble() < 0.2 // 20% chance of error
    }
    
    protected fun generateNewItem(existingCount: Int): Item {
        val newId = existingCount + 1
        return Item(
            id = "item_$newId",
            title = "New Item $newId",
            description = "Description for new item $newId"
        )
    }
}