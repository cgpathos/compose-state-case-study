package today.pathos.myapplication.study.agent04.result003

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import today.pathos.myapplication.study.common.Item
import today.pathos.myapplication.study.common.ScreenUiState

@Composable
fun Agent04Result003Screen() {
    // Create ItemManager instance to be provided via CompositionLocal
    val itemManager = remember { ItemManager() }
    
    // Provide ItemManager through CompositionLocal
    CompositionLocalProvider(LocalItemManager provides itemManager) {
        Agent04Result003Content()
    }
}

@Composable
private fun Agent04Result003Content() {
    // Access ItemManager from CompositionLocal
    val itemManager = LocalItemManager.current
    
    // Initial screen state using ScreenUiState for loading phase only
    var screenState by remember { mutableStateOf<ScreenUiState>(ScreenUiState.Initializing) }

    // Initialize data on first composition
    LaunchedEffect(Unit) {
        val success = itemManager.loadInitialItems()
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
                error = (screenState as ScreenUiState.Failed).error,
                onRetry = {
                    screenState = ScreenUiState.Initializing
                }
            )
        }
        is ScreenUiState.Succeed -> {
            MainContent()
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
private fun MainContent() {
    val itemManager = LocalItemManager.current
    
    Column(modifier = Modifier.fillMaxSize()) {
        ActionButtonsSection()
        ErrorMessageSection()
        ItemsList()
    }

    // Handle refresh operation
    LaunchedEffect(itemManager.isRefreshing) {
        if (itemManager.isRefreshing) {
            // Refresh is handled within ItemManager
        }
    }
}

@Composable
private fun ActionButtonsSection() {
    val itemManager = LocalItemManager.current
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = { itemManager.addItem() }
        ) {
            Text("Add Item")
        }
        
        RefreshButton()
        
        if (itemManager.items.isNotEmpty()) {
            Button(
                onClick = { itemManager.removeLastItem() }
            ) {
                Text("Remove Last")
            }
        }
    }
}

@Composable
private fun ErrorMessageSection() {
    val itemManager = LocalItemManager.current
    
    itemManager.errorMessage?.let { error ->
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
                    onClick = { itemManager.clearError() }
                ) {
                    Text("Dismiss")
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun ItemsList() {
    val itemManager = LocalItemManager.current
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(itemManager.items) { item ->
            ItemCard(
                item = item,
                onUpdate = { updatedItem ->
                    itemManager.updateItem(updatedItem)
                },
                onDelete = { itemToDelete ->
                    itemManager.deleteItem(itemToDelete)
                }
            )
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

@Composable
private fun RefreshButton() {
    val itemManager = LocalItemManager.current
    var triggerRefresh by remember { mutableStateOf(false) }
    
    Button(
        onClick = { triggerRefresh = !triggerRefresh },
        enabled = !itemManager.isRefreshing
    ) {
        Text(if (itemManager.isRefreshing) "Refreshing..." else "Refresh")
    }
    
    LaunchedEffect(triggerRefresh) {
        if (triggerRefresh && !itemManager.isRefreshing) {
            itemManager.refreshItems()
        }
    }
}