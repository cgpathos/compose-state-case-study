package today.pathos.myapplication.study.agent04.result001

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import today.pathos.myapplication.study.common.Item
import today.pathos.myapplication.study.common.ScreenUiState
import kotlin.random.Random

@Composable
fun Agent04Result001Screen() {
    // Initial screen state using ScreenUiState for loading phase only
    var screenState by remember { mutableStateOf<ScreenUiState>(ScreenUiState.Initializing) }
    
    // Compose-native state for ongoing operations after initialization
    var items by remember { mutableStateOf<List<Item>>(emptyList()) }
    var isRefreshing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Initialize data on first composition
    LaunchedEffect(Unit) {
        delay(2000) // 2 second loading simulation
        
        if (shouldSimulateError()) {
            screenState = ScreenUiState.Failed("Failed to load initial data")
        } else {
            items = generateInitialItems()
            screenState = ScreenUiState.Succeed
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
                    Text("Error: ${screenState.error}")
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
                            items = items + generateNewItem(items.size)
                        }
                    ) {
                        Text("Add Item")
                    }
                    
                    Button(
                        onClick = {
                            isRefreshing = true
                            // Use remember state for refresh operation
                        },
                        enabled = !isRefreshing
                    ) {
                        Text(if (isRefreshing) "Refreshing..." else "Refresh")
                    }
                    
                    if (items.isNotEmpty()) {
                        Button(
                            onClick = {
                                items = items.dropLast(1)
                            }
                        ) {
                            Text("Remove Last")
                        }
                    }
                }

                // Error message display
                errorMessage?.let { error ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Text(
                            text = error,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Items list
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items) { item ->
                        ItemCard(
                            item = item,
                            onUpdate = { updatedItem ->
                                items = items.map { 
                                    if (it.id == updatedItem.id) updatedItem else it 
                                }
                            },
                            onDelete = { itemToDelete ->
                                items = items.filter { it.id != itemToDelete.id }
                            }
                        )
                    }
                }
            }

            // Handle refresh operation
            LaunchedEffect(isRefreshing) {
                if (isRefreshing) {
                    delay(1000)
                    if (shouldSimulateError()) {
                        errorMessage = "Refresh failed"
                    } else {
                        items = generateInitialItems()
                        errorMessage = null
                    }
                    isRefreshing = false
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
    var isEditing by remember { mutableStateOf(false) }
    var editedTitle by remember(item.title) { mutableStateOf(item.title) }
    
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