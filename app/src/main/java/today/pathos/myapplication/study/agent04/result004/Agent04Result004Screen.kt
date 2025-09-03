package today.pathos.myapplication.study.agent04.result004

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import today.pathos.myapplication.study.common.Item
import today.pathos.myapplication.study.common.ScreenUiState

@Composable
fun Agent04Result004Screen(
    viewModel: Agent04Result004ViewModel = viewModel()
) {
    // State Hoisting Pattern: State is owned by ViewModel and hoisted to this composable
    // All child composables receive state and events as parameters
    
    when (viewModel.screenState) {
        is ScreenUiState.Initializing -> {
            LoadingScreen()
        }
        is ScreenUiState.Failed -> {
            ErrorScreen(
                error = (viewModel.screenState as ScreenUiState.Failed).error,
                onRetry = viewModel::retry
            )
        }
        is ScreenUiState.Succeed -> {
            MainScreen(
                items = viewModel.items,
                isRefreshing = viewModel.isRefreshing,
                errorMessage = viewModel.errorMessage,
                onAddItem = viewModel::addItem,
                onRemoveLastItem = viewModel::removeLastItem,
                onRefreshItems = viewModel::refreshItems,
                onUpdateItem = viewModel::updateItem,
                onDeleteItem = viewModel::deleteItem,
                onClearError = viewModel::clearError
            )
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorScreen(
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
private fun MainScreen(
    items: List<Item>,
    isRefreshing: Boolean,
    errorMessage: String?,
    onAddItem: () -> Unit,
    onRemoveLastItem: () -> Unit,
    onRefreshItems: () -> Unit,
    onUpdateItem: (Item) -> Unit,
    onDeleteItem: (Item) -> Unit,
    onClearError: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ActionButtonsSection(
            items = items,
            isRefreshing = isRefreshing,
            onAddItem = onAddItem,
            onRemoveLastItem = onRemoveLastItem,
            onRefreshItems = onRefreshItems
        )
        
        ErrorMessageSection(
            errorMessage = errorMessage,
            onClearError = onClearError
        )
        
        ItemsListSection(
            items = items,
            onUpdateItem = onUpdateItem,
            onDeleteItem = onDeleteItem
        )
    }
}

@Composable
private fun ActionButtonsSection(
    items: List<Item>,
    isRefreshing: Boolean,
    onAddItem: () -> Unit,
    onRemoveLastItem: () -> Unit,
    onRefreshItems: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(onClick = onAddItem) {
            Text("Add Item")
        }
        
        Button(
            onClick = onRefreshItems,
            enabled = !isRefreshing
        ) {
            Text(if (isRefreshing) "Refreshing..." else "Refresh")
        }
        
        if (items.isNotEmpty()) {
            Button(onClick = onRemoveLastItem) {
                Text("Remove Last")
            }
        }
    }
}

@Composable
private fun ErrorMessageSection(
    errorMessage: String?,
    onClearError: () -> Unit
) {
    errorMessage?.let { error ->
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
                TextButton(onClick = onClearError) {
                    Text("Dismiss")
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun ItemsListSection(
    items: List<Item>,
    onUpdateItem: (Item) -> Unit,
    onDeleteItem: (Item) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items) { item ->
            StatelessItemCard(
                item = item,
                onUpdate = onUpdateItem,
                onDelete = onDeleteItem
            )
        }
    }
}

@Composable
private fun StatelessItemCard(
    item: Item,
    onUpdate: (Item) -> Unit,
    onDelete: (Item) -> Unit
) {
    // Local UI state for editing - not hoisted as it's purely UI concern
    var isEditing by remember { mutableStateOf(false) }
    var editedTitle by remember(item.title) { mutableStateOf(item.title) }
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (isEditing) {
                EditingItemContent(
                    editedTitle = editedTitle,
                    onTitleChange = { editedTitle = it },
                    onSave = {
                        onUpdate(item.copy(title = editedTitle))
                        isEditing = false
                    },
                    onCancel = { isEditing = false }
                )
            } else {
                DisplayItemContent(
                    item = item,
                    onEdit = { isEditing = true },
                    onDelete = { onDelete(item) }
                )
            }
        }
    }
}

@Composable
private fun EditingItemContent(
    editedTitle: String,
    onTitleChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    OutlinedTextField(
        value = editedTitle,
        onValueChange = onTitleChange,
        label = { Text("Title") },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = onSave) {
            Text("Save")
        }
        OutlinedButton(onClick = onCancel) {
            Text("Cancel")
        }
    }
}

@Composable
private fun DisplayItemContent(
    item: Item,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
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
        OutlinedButton(onClick = onEdit) {
            Text("Edit")
        }
        OutlinedButton(onClick = onDelete) {
            Text("Delete")
        }
    }
}