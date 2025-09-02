package today.pathos.myapplication.study.agent05.result001

import today.pathos.myapplication.study.common.Item

// Use cases for different item operations
class GetItemsUseCase(private val repository: ItemRepository) {
    suspend operator fun invoke(): Result<List<Item>> {
        return repository.getItems()
    }
}

class AddItemUseCase(private val repository: ItemRepository) {
    suspend operator fun invoke(title: String, description: String): Result<List<Item>> {
        val existingItems = repository.getItems().getOrElse { emptyList() }
        val newId = (existingItems.size + 1).toString()
        val newItem = Item(
            id = "item_$newId",
            title = title,
            description = description
        )
        return repository.addItem(newItem)
    }
}

class RemoveItemUseCase(private val repository: ItemRepository) {
    suspend operator fun invoke(itemId: String): Result<List<Item>> {
        return repository.removeItem(itemId)
    }
}

class UpdateItemUseCase(private val repository: ItemRepository) {
    suspend operator fun invoke(item: Item): Result<List<Item>> {
        return repository.updateItem(item)
    }
}

class RefreshItemsUseCase(private val repository: ItemRepository) {
    suspend operator fun invoke(): Result<List<Item>> {
        return repository.refreshItems()
    }
}

// Use case container for dependency injection
data class ItemUseCases(
    val getItems: GetItemsUseCase,
    val addItem: AddItemUseCase,
    val removeItem: RemoveItemUseCase,
    val updateItem: UpdateItemUseCase,
    val refreshItems: RefreshItemsUseCase
) {
    companion object {
        fun create(repository: ItemRepository): ItemUseCases {
            return ItemUseCases(
                getItems = GetItemsUseCase(repository),
                addItem = AddItemUseCase(repository),
                removeItem = RemoveItemUseCase(repository),
                updateItem = UpdateItemUseCase(repository),
                refreshItems = RefreshItemsUseCase(repository)
            )
        }
    }
}