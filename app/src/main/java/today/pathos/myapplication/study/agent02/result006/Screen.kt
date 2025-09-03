package today.pathos.myapplication.study.agent02.result006

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import today.pathos.myapplication.study.common.Item
import today.pathos.myapplication.study.common.ScreenUiState
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommandPatternScreen(
    viewModel: CommandViewModel = viewModel()
) {
    // Manual initialization on first composition - NO init{} in ViewModel
    LaunchedEffect(Unit) {
        viewModel.initialize()
    }
    
    val screenUiState by viewModel.screenUiState.collectAsStateWithLifecycle()
    val items by viewModel.items.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val hasUndoableCommands by remember {
        derivedStateOf { viewModel.hasUndoableCommands() }
    }
    
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<Item?>(null) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with Command Pattern info
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Command Pattern",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Commands are queued and executed sequentially. Supports undo operations.",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Pending: ${viewModel.getPendingCommandsCount()}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Undoable: ${if (hasUndoableCommands) "Yes" else "No"}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add")
            }
            
            Button(
                onClick = { viewModel.refreshItems() },
                modifier = Modifier.weight(1f),
                enabled = !isLoading
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Refresh")
            }
            
            Button(
                onClick = { viewModel.undoLastCommand() },
                modifier = Modifier.weight(1f),
                enabled = hasUndoableCommands
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Undo")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Content based on screen state
        when (screenUiState) {
            is ScreenUiState.Initializing -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Initializing command system...")
                    }
                }
            }
            
            is ScreenUiState.Failed -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Error",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = (screenUiState as ScreenUiState.Failed).error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
            
            is ScreenUiState.Succeed -> {
                if (isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                error?.let { errorMessage ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Text(
                            text = errorMessage,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                LazyColumn {
                    items(items) { item ->
                        ItemCard(
                            item = item,
                            onEdit = {
                                editingItem = item
                                showEditDialog = true
                            },
                            onDelete = {
                                viewModel.removeItem(item.id)
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
    
    // Add Item Dialog
    if (showAddDialog) {
        AddItemDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { title, description ->
                val newItem = Item(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    description = description
                )
                viewModel.addItem(newItem)
                showAddDialog = false
            }
        )
    }
    
    // Edit Item Dialog
    if (showEditDialog) {
        editingItem?.let { item ->
            EditItemDialog(
                item = item,
                onDismiss = {
                    showEditDialog = false
                    editingItem = null
                },
                onUpdate = { title, description ->
                    val updatedItem = item.copy(
                        title = title,
                        description = description
                    )
                    viewModel.updateItem(updatedItem)
                    showEditDialog = false
                    editingItem = null
                }
            )
        }
    }
}

@Composable
private fun ItemCard(
    item: Item,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}

@Composable
private fun AddItemDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Item") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onAdd(title, description) },
                enabled = title.isNotBlank() && description.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun EditItemDialog(
    item: Item,
    onDismiss: () -> Unit,
    onUpdate: (String, String) -> Unit
) {
    var title by remember { mutableStateOf(item.title) }
    var description by remember { mutableStateOf(item.description) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Item") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onUpdate(title, description) },
                enabled = title.isNotBlank() && description.isNotBlank()
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}