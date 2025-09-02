package today.pathos.myapplication.study.agent02.result003

import today.pathos.myapplication.study.common.Item

class GetItemsUseCase(
    private val repository: ItemRepository
) {
    suspend operator fun invoke(): List<Item> {
        return repository.getItems()
    }
}