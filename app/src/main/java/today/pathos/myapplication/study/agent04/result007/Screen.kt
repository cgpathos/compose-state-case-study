package today.pathos.myapplication.study.agent04.result007

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
fun ProduceStatePatternScreen(
    viewModel: ProduceStateViewModel = viewModel()
) {
    // State managed by ViewModel
    val localItems by viewModel.localItems.collectAsStateWithLifecycle()
    val isLocalLoading by viewModel.isLocalLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val refreshTrigger by viewModel.refreshTrigger.collectAsStateWithLifecycle()
    val loadDelay by viewModel.loadDelay.collectAsStateWithLifecycle()
    val shouldSimulateError by viewModel.shouldSimulateError.collectAsStateWithLifecycle()
    val produceStateExecutions by viewModel.produceStateExecutions.collectAsStateWithLifecycle()
    
    // produceState for initial loading - NO init{} in ViewModel
    val initialState by produceState<Pair<ScreenUiState, List<Item>>?>(
        initialValue = null
    ) {
        try {
            value = ScreenUiState.Initializing to emptyList()
            val result = viewModel.loadItemsForProduceState()
            value = result
        } catch (e: Exception) {
            value = ScreenUiState.Failed(e.message ?: "Unknown error") to emptyList()
        }
    }
    
    // produceState for refresh operations (reactive to refreshTrigger)
    val refreshState by produceState<Pair<ScreenUiState, List<Item>>?>(
        initialValue = null,
        key1 = refreshTrigger
    ) {
        if (refreshTrigger > 0) {
            try {
                value = ScreenUiState.Initializing to (value?.second ?: emptyList())
                val result = viewModel.refreshItemsForProduceState(refreshTrigger)
                value = result
            } catch (e: Exception) {
                value = ScreenUiState.Failed(e.message ?: "Unknown error") to (value?.second ?: emptyList())
            }
        }
    }
    
    // produceState for configuration changes
    val configurationState by produceState<String>(
        initialValue = "Default Configuration",
        key1 = loadDelay,
        key2 = shouldSimulateError
    ) {
        value = "Loading delay: ${loadDelay}ms, Error simulation: ${if (shouldSimulateError) "ON" else "OFF"}"
    }
    
    // Determine which state to use
    val currentState = refreshState ?: initialState
    val (screenUiState, produceStateItems) = currentState ?: (ScreenUiState.Initializing to emptyList())
    
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<Item?>(null) }
    var selectedTab by remember { mutableStateOf(0) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with produceState Pattern info
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "produceState Pattern",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "State generation with Compose produceState instead of ViewModel init{}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "ProduceState Executions: $produceStateExecutions",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Refresh Trigger: $refreshTrigger",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Text(
                    text = configurationState,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Configuration Controls
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "ProduceState Configuration",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Delay:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    FilterChip(
                        onClick = { viewModel.setLoadDelay(500) },
                        label = { Text("500ms") },
                        selected = loadDelay == 500L
                    )
                    FilterChip(
                        onClick = { viewModel.setLoadDelay(2000) },
                        label = { Text("2s") },
                        selected = loadDelay == 2000L
                    )
                    FilterChip(
                        onClick = { viewModel.setLoadDelay(5000) },
                        label = { Text("5s") },
                        selected = loadDelay == 5000L
                    )
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = shouldSimulateError,
                        onCheckedChange = { viewModel.toggleSimulateError() }
                    )
                    Text(
                        text = "Simulate Errors",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.triggerProduceStateRefresh() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Trigger produceState")
                    }
                    
                    OutlinedButton(
                        onClick = { viewModel.resetStatistics() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset")
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Tab Selection
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("ProduceState Items") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Local ViewModel Items") }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        when (selectedTab) {
            0 -> {
                // ProduceState Tab
                ProduceStateContent(
                    screenUiState = screenUiState,
                    items = produceStateItems,
                    onRefresh = { viewModel.triggerProduceStateRefresh() },
                    onAddItem = { showAddDialog = true },
                    onEditItem = { item ->
                        editingItem = item
                        showEditDialog = true
                    },
                    onDeleteItem = { /* ProduceState items are read-only */ }
                )
            }
            
            1 -> {
                // Local ViewModel Tab
                LocalViewModelContent(
                    items = localItems,
                    isLoading = isLocalLoading,
                    error = error,
                    onRefresh = { viewModel.refreshLocalItems() },
                    onAddItem = { showAddDialog = true },
                    onEditItem = { item ->
                        editingItem = item
                        showEditDialog = true
                    },
                    onDeleteItem = { itemId ->
                        viewModel.removeLocalItem(itemId)
                    },
                    onClearError = { viewModel.clearError() }
                )
            }
        }
    }
    
    // Add Item Dialog
    if (showAddDialog) {
        AddItemDialog(
            tabType = if (selectedTab == 0) "ProduceState" else "Local ViewModel",
            onDismiss = { showAddDialog = false },
            onAdd = { title, description ->
                val newItem = Item(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    description = description
                )
                if (selectedTab == 1) {
                    viewModel.addLocalItem(newItem)
                }
                showAddDialog = false
            }
        )
    }
    
    // Edit Item Dialog
    if (showEditDialog) {
        editingItem?.let { item ->
            EditItemDialog(
                item = item,
                isProduceState = selectedTab == 0,
                onDismiss = {
                    showEditDialog = false
                    editingItem = null
                },
                onUpdate = { title, description ->
                    if (selectedTab == 1) {
                        val updatedItem = item.copy(
                            title = title,
                            description = description
                        )
                        viewModel.updateLocalItem(updatedItem)
                    }
                    showEditDialog = false
                    editingItem = null
                }
            )
        }
    }
}

@Composable
private fun ProduceStateContent(
    screenUiState: ScreenUiState,
    items: List<Item>,
    onRefresh: () -> Unit,
    onAddItem: () -> Unit,
    onEditItem: (Item) -> Unit,
    onDeleteItem: (String) -> Unit
) {
    Column {
        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onAddItem,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add (Read-Only)")
            }
            
            Button(
                onClick = onRefresh,
                modifier = Modifier.weight(1f),
                enabled = screenUiState != ScreenUiState.Initializing
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Refresh")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        when (screenUiState) {
            is ScreenUiState.Initializing -> {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("produceState executing...")
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
                            text = "ProduceState Error",
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
                            onClick = onRefresh,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Retry produceState")
                        }
                    }
                }
            }
            
            is ScreenUiState.Succeed -> {
                if (items.isEmpty()) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Build,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "ProduceState completed, no items",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                } else {
                    LazyColumn {
                        items(items) { item ->
                            ItemCard(
                                item = item,
                                type = "ProduceState",
                                isReadOnly = true,
                                onEdit = { onEditItem(item) },
                                onDelete = { onDeleteItem(item.id) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LocalViewModelContent(
    items: List<Item>,
    isLoading: Boolean,
    error: String?,
    onRefresh: () -> Unit,
    onAddItem: () -> Unit,
    onEditItem: (Item) -> Unit,
    onDeleteItem: (String) -> Unit,
    onClearError: () -> Unit
) {
    Column {
        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onAddItem,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add")
            }
            
            Button(
                onClick = onRefresh,
                modifier = Modifier.weight(1f),
                enabled = !isLoading
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Refresh")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
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
                    IconButton(onClick = onClearError) {
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
            Card(modifier = Modifier.fillMaxWidth()) {
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
                        text = "No local items",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        } else {
            LazyColumn {
                items(items) { item ->
                    ItemCard(
                        item = item,
                        type = "Local ViewModel",
                        isReadOnly = false,
                        onEdit = { onEditItem(item) },
                        onDelete = { onDeleteItem(item.id) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun ItemCard(
    item: Item,
    type: String,
    isReadOnly: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = if (type == "ProduceState") {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
        } else {
            CardDefaults.cardColors()
        }
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
                AssistChip(
                    onClick = { },
                    label = { Text(type, style = MaterialTheme.typography.bodySmall) },
                    modifier = Modifier.padding(top = 4.dp).height(24.dp)
                )
            }
            
            Row {
                if (!isReadOnly) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                } else {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Read-only",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun AddItemDialog(
    tabType: String,
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Item to $tabType") },
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
                if (tabType == "ProduceState") {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Note: ProduceState items are read-only and will be replaced on refresh",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onAdd(title, description) },
                enabled = title.isNotBlank() && description.isNotBlank() && tabType != "ProduceState"
            ) {
                Text(if (tabType == "ProduceState") "Cannot Add" else "Add")
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
    isProduceState: Boolean,
    onDismiss: () -> Unit,
    onUpdate: (String, String) -> Unit
) {
    var title by remember { mutableStateOf(item.title) }
    var description by remember { mutableStateOf(item.description) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isProduceState) "View ProduceState Item" else "Edit Item") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isProduceState
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isProduceState
                )
                if (isProduceState) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "ProduceState items are read-only",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            if (!isProduceState) {
                TextButton(
                    onClick = { onUpdate(title, description) },
                    enabled = title.isNotBlank() && description.isNotBlank()
                ) {
                    Text("Update")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isProduceState) "Close" else "Cancel")
            }
        }
    )
}