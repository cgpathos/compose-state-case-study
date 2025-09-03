package today.pathos.myapplication.study.agent05.result006

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
fun LazyRepositoryScreen(
    viewModel: LazyRepositoryViewModel = viewModel()
) {
    // Manual initialization on first composition - NO init{} in ViewModel
    LaunchedEffect(Unit) {
        viewModel.manualInitialization()
    }
    
    val screenUiState by viewModel.screenUiState.collectAsStateWithLifecycle()
    val items by viewModel.items.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val selectedRepositoryType by viewModel.selectedRepositoryType.collectAsStateWithLifecycle()
    val repositoryInitialized by viewModel.repositoryInitialized.collectAsStateWithLifecycle()
    val repositoryAccessCount by viewModel.repositoryAccessCount.collectAsStateWithLifecycle()
    
    val cacheAge by remember {
        derivedStateOf { viewModel.getCacheAge() }
    }
    val databaseConnection by remember {
        derivedStateOf { viewModel.getDatabaseConnection() }
    }
    
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<Item?>(null) }
    
    val repositoryTypes = listOf("lazy", "cache", "database")
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with Lazy Repository Pattern info
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Lazy Repository Pattern",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Repositories initialized lazily when first accessed, not in ViewModel init{}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Repository: $selectedRepositoryType",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Initialized: ${if (repositoryInitialized) "Yes" else "No"}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Access Count: $repositoryAccessCount",
                        style = MaterialTheme.typography.bodySmall
                    )
                    
                    when (selectedRepositoryType) {
                        "cache" -> {
                            Text(
                                text = if (cacheAge >= 0) "Cache: ${cacheAge / 1000}s old" else "Cache: Empty",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        "database" -> {
                            Text(
                                text = databaseConnection ?: "DB: Not connected",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        else -> {
                            Text(
                                text = "Lazy: ${if (viewModel.isRepositoryLazilyInitialized("lazy")) "Loaded" else "Not loaded"}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Repository Selection
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Repository Selection",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(repositoryTypes) { type ->
                        val isInitialized = viewModel.isRepositoryLazilyInitialized(type)
                        FilterChip(
                            onClick = { viewModel.switchRepository(type) },
                            label = { 
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(type.uppercase())
                                    if (isInitialized) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            },
                            selected = type == selectedRepositoryType,
                            enabled = !isLoading
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Repository-specific actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when (selectedRepositoryType) {
                        "lazy" -> {
                            OutlinedButton(
                                onClick = { viewModel.resetLazyRepository() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reset Lazy")
                            }
                        }
                        "cache" -> {
                            OutlinedButton(
                                onClick = { viewModel.invalidateCacheRepository() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Invalidate Cache")
                            }
                        }
                        "database" -> {
                            OutlinedButton(
                                onClick = { viewModel.closeDatabaseRepository() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Close DB")
                            }
                        }
                    }
                    
                    OutlinedButton(
                        onClick = { viewModel.startFlowCollection() },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Start Flow")
                    }
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
            
            OutlinedButton(
                onClick = { viewModel.resetStatistics() },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Info, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Reset Stats")
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
                        Text("Lazy repository initializing...")
                        Text(
                            text = "Repository: $selectedRepositoryType",
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
                            text = "Repository Error",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = (screenUiState as ScreenUiState.Failed).error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.refreshItems() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Retry Repository")
                            }
                            OutlinedButton(
                                onClick = { 
                                    val nextType = when (selectedRepositoryType) {
                                        "lazy" -> "cache"
                                        "cache" -> "database"
                                        else -> "lazy"
                                    }
                                    viewModel.switchRepository(nextType)
                                }
                            ) {
                                Text("Switch Repository")
                            }
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
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = errorMessage,
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            IconButton(onClick = { viewModel.clearError() }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Clear error",
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
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
                                Icons.Default.List,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Repository is empty",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Try refreshing or switching repositories",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                } else {
                    LazyColumn {
                        items(items) { item ->
                            ItemCard(
                                item = item,
                                repositoryType = selectedRepositoryType,
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
            repositoryType = selectedRepositoryType,
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
    repositoryType: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val containerColor = when (repositoryType) {
        "lazy" -> MaterialTheme.colorScheme.primaryContainer
        "cache" -> MaterialTheme.colorScheme.secondaryContainer
        "database" -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.surface
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
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
                                "${repositoryType.uppercase()} Repository", 
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
    repositoryType: String,
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Item to ${repositoryType.uppercase()} Repository") },
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
                    text = "Item will be added to the current lazy repository instance",
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