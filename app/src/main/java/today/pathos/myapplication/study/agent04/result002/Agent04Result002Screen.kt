package today.pathos.myapplication.study.agent04.result002

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import today.pathos.myapplication.study.common.Item
import today.pathos.myapplication.study.common.ScreenUiState
import kotlin.random.Random

@Composable
fun Agent04Result002Screen() {
    // Initial screen state using ScreenUiState for loading phase only
    var screenState by rememberSaveable { mutableStateOf<ScreenUiState>(ScreenUiState.Initializing) }
    
    // Saveable state for ongoing operations after initialization - survives configuration changes
    var itemListState by rememberSaveable {
        mutableStateOf(ItemListState(items = emptyList()))
    }

    // Initialize data on first composition
    LaunchedEffect(Unit) {
        if (screenState is ScreenUiState.Initializing) {
            delay(2000) // 2 second loading simulation
            
            if (shouldSimulateError()) {
                screenState = ScreenUiState.Failed("Failed to load initial data")
            } else {
                val initialItems = generateInitialItems().map { ParcelableItem(it) }
                itemListState = itemListState.copy(items = initialItems)
                screenState = ScreenUiState.Succeed
            }
        }
    }

    when (screenState) {
        is ScreenUiState.Initializing -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is ScreenUiState.Failed -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Error: ${(screenState as ScreenUiState.Failed).error}")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        screenState = ScreenUiState.Initializing
                    }) {
                        Text("Retry")
                    }
                }
            }
        }
        is ScreenUiState.Succeed -> {
            Column(modifier = Modifier.fillMaxSize()) {
                // Action buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val newItem = ParcelableItem(generateNewItem(itemListState.items.size))
                            itemListState = itemListState.copy(
                                items = itemListState.items + newItem
                            )
                        }
                    ) {
                        Text("Add Item")
                    }
                    
                    Button(
                        onClick = {
                            if (!itemListState.isRefreshing) {
                                itemListState = itemListState.copy(isRefreshing = true)
                            }
                        },
                        enabled = !itemListState.isRefreshing
                    ) {
                        Text(if (itemListState.isRefreshing) "Refreshing..." else "Refresh")
                    }
                    
                    if (itemListState.items.isNotEmpty()) {
                        Button(
                            onClick = {
                                itemListState = itemListState.copy(
                                    items = itemListState.items.dropLast(1)
                                )
                            }
                        ) {
                            Text("Remove Last")
                        }
                    }
                }

                // Error message display
                itemListState.errorMessage?.let { error ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(
                                onClick = {
                                    itemListState = itemListState.copy(errorMessage = null)
                                }
                            ) {
                                Text("Dismiss")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Items list
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(itemListState.items) { parcelableItem ->
                        ItemCard(
                            item = parcelableItem.toItem(),
                            onUpdate = { updatedItem ->
                                val updatedParcelableItem = ParcelableItem(updatedItem)
                                itemListState = itemListState.copy(
                                    items = itemListState.items.map { 
                                        if (it.id == updatedParcelableItem.id) updatedParcelableItem else it 
                                    }
                                )
                            },
                            onDelete = { itemToDelete ->
                                itemListState = itemListState.copy(
                                    items = itemListState.items.filter { it.id != itemToDelete.id }
                                )
                            }
                        )
                    }
                }
            }

            // Handle refresh operation
            LaunchedEffect(itemListState.isRefreshing) {
                if (itemListState.isRefreshing) {
                    delay(1000)
                    if (shouldSimulateError()) {
                        itemListState = itemListState.copy(
                            isRefreshing = false,
                            errorMessage = "Refresh failed"
                        )
                    } else {
                        val refreshedItems = generateInitialItems().map { ParcelableItem(it) }
                        itemListState = itemListState.copy(
                            items = refreshedItems,
                            isRefreshing = false,
                            errorMessage = null
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemCard(
    item: Item,
    onUpdate: (Item) -> Unit,
    onDelete: (Item) -> Unit
) {
    // Using rememberSaveable for edit state to survive configuration changes
    var isEditing by rememberSaveable { mutableStateOf(false) }
    var editedTitle by rememberSaveable(item.title) { mutableStateOf(item.title) }
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (isEditing) {
                OutlinedTextField(
                    value = editedTitle,
                    onValueChange = { editedTitle = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            onUpdate(item.copy(title = editedTitle))
                            isEditing = false
                        }
                    ) {
                        Text("Save")
                    }
                    OutlinedButton(onClick = { isEditing = false }) {
                        Text("Cancel")
                    }
                }
            } else {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { isEditing = true }) {
                        Text("Edit")
                    }
                    OutlinedButton(
                        onClick = { onDelete(item) }
                    ) {
                        Text("Delete")
                    }
                }
            }
        }
    }
}

// Utility functions matching BaseViewModel
private fun generateInitialItems(): List<Item> {
    return (1..5).map { index ->
        Item(
            id = "item_$index",
            title = "Item $index",
            description = "Description for item $index"
        )
    }
}

private fun shouldSimulateError(): Boolean {
    return Random.nextDouble() < 0.2 // 20% chance of error
}

private fun generateNewItem(existingCount: Int): Item {
    val newId = existingCount + 1
    return Item(
        id = "item_$newId",
        title = "New Item $newId",
        description = "Description for new item $newId"
    )
}