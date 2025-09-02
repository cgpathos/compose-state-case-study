package today.pathos.myapplication.study.agent04.result005

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import today.pathos.myapplication.study.common.Item
import today.pathos.myapplication.study.common.ScreenUiState

@Composable
fun Agent04Result005Screen() {
    // Create ItemSnapshot instance with Compose's snapshot system
    val itemSnapshot = remember { ItemSnapshot() }
    
    // Initial screen state using ScreenUiState for loading phase only
    var screenState by remember { mutableStateOf<ScreenUiState>(ScreenUiState.Initializing) }

    // Initialize data on first composition
    LaunchedEffect(Unit) {
        val success = itemSnapshot.loadInitialItems()
        screenState = if (success) {
            ScreenUiState.Succeed
        } else {
            ScreenUiState.Failed("Failed to load initial data")
        }
    }

    when (screenState) {
        is ScreenUiState.Initializing -> {
            LoadingContent()
        }
        is ScreenUiState.Failed -> {
            ErrorContent(
                error = screenState.error,
                onRetry = {
                    screenState = ScreenUiState.Initializing
                }
            )
        }
        is ScreenUiState.Succeed -> {
            MainContent(itemSnapshot = itemSnapshot)
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Error: $error")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@Composable
private fun MainContent(itemSnapshot: ItemSnapshot) {
    Column(modifier = Modifier.fillMaxSize()) {
        ActionButtonsSection(itemSnapshot = itemSnapshot)
        ErrorMessageSection(itemSnapshot = itemSnapshot)
        ItemsListSection(itemSnapshot = itemSnapshot)
    }
}

@Composable
private fun ActionButtonsSection(itemSnapshot: ItemSnapshot) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = { itemSnapshot.addItem() }
        ) {
            Text("Add Item")
        }
        
        RefreshButton(itemSnapshot = itemSnapshot)
        
        if (itemSnapshot.items.isNotEmpty()) {
            Button(
                onClick = { itemSnapshot.removeLastItem() }
            ) {
                Text("Remove Last")
            }
        }
        
        // Demonstrate batch operations with snapshot system
        Button(
            onClick = {
                // Perform multiple operations atomically
                itemSnapshot.performBatchOperation {
                    repeat(3) {
                        itemSnapshot.addItem()
                    }
                }
            }
        ) {
            Text("Add 3")
        }
    }
}

@Composable
private fun RefreshButton(itemSnapshot: ItemSnapshot) {
    var triggerRefresh by remember { mutableStateOf(false) }
    
    Button(
        onClick = { triggerRefresh = !triggerRefresh },
        enabled = !itemSnapshot.isRefreshing
    ) {
        Text(if (itemSnapshot.isRefreshing) "Refreshing..." else "Refresh")
    }
    
    LaunchedEffect(triggerRefresh) {
        if (triggerRefresh && !itemSnapshot.isRefreshing) {
            itemSnapshot.refreshItems()
        }
    }
}

@Composable
private fun ErrorMessageSection(itemSnapshot: ItemSnapshot) {
    itemSnapshot.errorMessage?.let { error ->
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
                    onClick = { itemSnapshot.clearError() }
                ) {
                    Text("Dismiss")
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun ItemsListSection(itemSnapshot: ItemSnapshot) {
    // Automatically recomposes when items in SnapshotStateList change
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = itemSnapshot.items,
            key = { item -> item.id } // Use key for better performance
        ) { item ->
            SnapshotItemCard(
                item = item,
                onUpdate = { updatedItem ->
                    itemSnapshot.updateItem(updatedItem)
                },
                onDelete = { itemToDelete ->
                    itemSnapshot.deleteItem(itemToDelete)
                }
            )
        }
    }
}

@Composable
private fun SnapshotItemCard(
    item: Item,
    onUpdate: (Item) -> Unit,
    onDelete: (Item) -> Unit
) {
    // Local UI state for editing
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
                            // Update using snapshot system
                            Snapshot.withMutableSnapshot {
                                onUpdate(item.copy(title = editedTitle))
                                isEditing = false
                            }
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
                Text(
                    text = "ID: ${item.id}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { isEditing = true }) {
                        Text("Edit")
                    }
                    OutlinedButton(
                        onClick = { 
                            // Delete using snapshot system
                            Snapshot.withMutableSnapshot {
                                onDelete(item)
                            }
                        }
                    ) {
                        Text("Delete")
                    }
                }
            }
        }
    }
}