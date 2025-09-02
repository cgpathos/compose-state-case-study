package today.pathos.myapplication.study.agent05.result004

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import today.pathos.myapplication.study.common.Item

// Event definitions for the event bus
sealed class ItemEvent {
    object LoadItems : ItemEvent()
    data class AddItem(val title: String, val description: String) : ItemEvent()
    data class RemoveItem(val itemId: String) : ItemEvent()
    data class UpdateItem(val item: Item) : ItemEvent()
    object RefreshItems : ItemEvent()
    object ClearError : ItemEvent()
}

// State change notifications
sealed class ItemStateChange {
    object LoadingStarted : ItemStateChange()
    object LoadingFinished : ItemStateChange()
    data class ItemsLoaded(val items: List<Item>) : ItemStateChange()
    data class ItemAdded(val item: Item) : ItemStateChange()
    data class ItemRemoved(val itemId: String) : ItemStateChange()
    data class ItemUpdated(val item: Item) : ItemStateChange()
    data class ErrorOccurred(val error: String) : ItemStateChange()
    object ErrorCleared : ItemStateChange()
    data class OperationCompleted(val message: String) : ItemStateChange()
}

// Event Bus implementation
interface EventBus {
    val events: SharedFlow<ItemEvent>
    val stateChanges: SharedFlow<ItemStateChange>
    
    suspend fun publishEvent(event: ItemEvent)
    suspend fun publishStateChange(stateChange: ItemStateChange)
}

class EventBusImpl : EventBus {
    private val _events = MutableSharedFlow<ItemEvent>()
    override val events: SharedFlow<ItemEvent> = _events.asSharedFlow()
    
    private val _stateChanges = MutableSharedFlow<ItemStateChange>()
    override val stateChanges: SharedFlow<ItemStateChange> = _stateChanges.asSharedFlow()
    
    override suspend fun publishEvent(event: ItemEvent) {
        _events.emit(event)
    }
    
    override suspend fun publishStateChange(stateChange: ItemStateChange) {
        _stateChanges.emit(stateChange)
    }
}