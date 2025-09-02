package today.pathos.myapplication.study.agent02.result003

class RemoveItemUseCase(
    private val repository: ItemRepository
) {
    suspend operator fun invoke(itemId: String) {
        repository.removeItem(itemId)
    }
}