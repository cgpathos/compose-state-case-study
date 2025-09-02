package today.pathos.myapplication.study.agent02.result003

import today.pathos.myapplication.study.common.Item

class UpdateItemUseCase(
    private val repository: ItemRepository
) {
    suspend operator fun invoke(item: Item) {
        repository.updateItem(item)
    }
}