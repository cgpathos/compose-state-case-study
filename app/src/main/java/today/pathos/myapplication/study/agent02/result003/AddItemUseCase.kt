package today.pathos.myapplication.study.agent02.result003

import today.pathos.myapplication.study.common.Item

class AddItemUseCase(
    private val repository: ItemRepository
) {
    suspend operator fun invoke(item: Item) {
        repository.addItem(item)
    }
}