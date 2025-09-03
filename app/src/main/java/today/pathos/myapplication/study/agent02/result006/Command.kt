package today.pathos.myapplication.study.agent02.result006

import today.pathos.myapplication.study.common.Item

// Command interface
interface Command {
    suspend fun execute()
    fun canUndo(): Boolean = false
    suspend fun undo() {}
}

// Concrete Commands
class LoadItemsCommand(
    private val viewModel: CommandViewModel
) : Command {
    override suspend fun execute() {
        viewModel.executeLoadItems()
    }
}

class RefreshItemsCommand(
    private val viewModel: CommandViewModel
) : Command {
    override suspend fun execute() {
        viewModel.executeRefreshItems()
    }
}

class AddItemCommand(
    private val viewModel: CommandViewModel,
    private val item: Item
) : Command {
    override suspend fun execute() {
        viewModel.executeAddItem(item)
    }
    
    override fun canUndo(): Boolean = true
    
    override suspend fun undo() {
        viewModel.executeRemoveItem(item.id)
    }
}

class RemoveItemCommand(
    private val viewModel: CommandViewModel,
    private val itemId: String
) : Command {
    private var removedItem: Item? = null
    
    override suspend fun execute() {
        removedItem = viewModel.findItemById(itemId)
        viewModel.executeRemoveItem(itemId)
    }
    
    override fun canUndo(): Boolean = removedItem != null
    
    override suspend fun undo() {
        removedItem?.let { item ->
            viewModel.executeAddItem(item)
        }
    }
}

class UpdateItemCommand(
    private val viewModel: CommandViewModel,
    private val newItem: Item
) : Command {
    private var oldItem: Item? = null
    
    override suspend fun execute() {
        oldItem = viewModel.findItemById(newItem.id)
        viewModel.executeUpdateItem(newItem)
    }
    
    override fun canUndo(): Boolean = oldItem != null
    
    override suspend fun undo() {
        oldItem?.let { item ->
            viewModel.executeUpdateItem(item)
        }
    }
}