package today.pathos.myapplication.study.agent04.result006

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
fun LaunchedEffectPatternScreen(
    viewModel: LaunchedEffectViewModel = viewModel()
) {
    // LaunchedEffect initialization - NO init{} in ViewModel
    // This runs only once when the composable enters the composition
    LaunchedEffect(Unit) {
        viewModel.initializeFromLaunchedEffect("initial-load")
    }
    
    val screenUiState by viewModel.screenUiState.collectAsStateWithLifecycle()
    val items by viewModel.items.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val launchedEffectExecutions by viewModel.launchedEffectExecutions.collectAsStateWithLifecycle()
    val isInitialized by viewModel.isInitialized.collectAsStateWithLifecycle()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<Item?>(null) }
    var selectedUserId by remember { mutableStateOf("user123") }
    var selectedCategory by remember { mutableStateOf("work") }
    var triggerReinitialize by remember { mutableStateOf(false) }
    
    // LaunchedEffect that runs when specific state changes
    LaunchedEffect(selectedUserId, selectedCategory) {
        if (isInitialized) {
            viewModel.initializeWithParameters(selectedUserId, selectedCategory)
        }
    }
    
    // Conditional LaunchedEffect
    LaunchedEffect(triggerReinitialize) {
        if (triggerReinitialize) {
            viewModel.conditionalReinitialize(true)
            triggerReinitialize = false
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with LaunchedEffect Pattern info
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "LaunchedEffect Pattern",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Initialization driven by Compose LaunchedEffect instead of ViewModel init{}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "LaunchedEffect Executions: $launchedEffectExecutions",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Initialized: ${if (isInitialized) "Yes" else "No"}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // LaunchedEffect Controls
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "LaunchedEffect Controls",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Parameter controls that trigger LaunchedEffect
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = selectedUserId,
                        onValueChange = { selectedUserId = it },
                        label = { Text("User ID") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = { selectedCategory = it },
                        label = { Text("Category") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { triggerReinitialize = true },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Trigger LaunchedEffect")
                    }
                    
                    OutlinedButton(
                        onClick = { viewModel.resetInitialization() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset")
                    }
                }
                
                Text(
                    text = "Note: Changing User ID or Category will trigger LaunchedEffect automatically",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
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
                        Text("LaunchedEffect initializing...")
                        Text(
                            text = "ViewModel has no init{} block",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
                            text = "LaunchedEffect Error",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = (screenUiState as ScreenUiState.Failed).error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { triggerReinitialize = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Retry with LaunchedEffect")
                        }
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
                
                if (items.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "LaunchedEffect completed, no items",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Try different parameters or add items manually",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                } else {
                    LazyColumn {
                        items(items) { item ->
                            ItemCard(
                                item = item,
                                launchedEffectExecutions = launchedEffectExecutions,
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
    launchedEffectExecutions: Int,
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
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AssistChip(
                        onClick = { },
                        label = { 
                            Text(
                                "LaunchedEffect #$launchedEffectExecutions", 
                                style = MaterialTheme.typography.bodySmall
                            ) 
                        },
                        modifier = Modifier.height(24.dp)
                    )
                }
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
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Note: This item is added directly, not via LaunchedEffect",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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