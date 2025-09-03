package today.pathos.myapplication.study.agent05.result007

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
fun ProviderPatternScreen(
    viewModel: ProviderViewModel = viewModel()
) {
    // Manual initialization with provider pattern - NO init{} in ViewModel
    LaunchedEffect(Unit) {
        viewModel.initializeWithProvider()
    }
    
    val screenUiState by viewModel.screenUiState.collectAsStateWithLifecycle()
    val items by viewModel.items.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val selectedConfig by viewModel.selectedConfig.collectAsStateWithLifecycle()
    val selectedDaoType by viewModel.selectedDaoType.collectAsStateWithLifecycle()
    val selectedApiType by viewModel.selectedApiType.collectAsStateWithLifecycle()
    val serviceCreationCount by viewModel.serviceCreationCount.collectAsStateWithLifecycle()
    val currentServiceConfig by viewModel.currentServiceConfig.collectAsStateWithLifecycle()
    val availableConfigs by viewModel.availableConfigs.collectAsStateWithLifecycle()
    val availableDaoTypes by viewModel.availableDaoTypes.collectAsStateWithLifecycle()
    val availableApiTypes by viewModel.availableApiTypes.collectAsStateWithLifecycle()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<Item?>(null) }
    var showConfigDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with Provider Pattern info
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Provider Pattern",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Dependency injection via provider pattern instead of ViewModel init{}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Service Creations: $serviceCreationCount",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Initialized: ${if (viewModel.isServiceInitialized()) "Yes" else "No"}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Text(
                    text = viewModel.getCurrentConfigSummary(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Provider Configuration
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Dependency Configuration",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    IconButton(onClick = { showConfigDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Configure")
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Config: $selectedConfig",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "DAO: $selectedDaoType",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "API: $selectedApiType",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    
                    currentServiceConfig?.let { config ->
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Max Items: ${config.maxItems}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Network: ${if (config.enableNetworkSync) "ON" else "OFF"}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Cache: ${config.cacheTimeout / 1000}s",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Preset configurations
                Text(
                    text = "Quick Presets:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            onClick = { viewModel.usePresetConfiguration("default") },
                            label = { Text("Default") },
                            selected = selectedConfig == "default" && !isLoading,
                            enabled = !isLoading
                        )
                    }
                    item {
                        FilterChip(
                            onClick = { viewModel.usePresetConfiguration("offline") },
                            label = { Text("Offline") },
                            selected = selectedConfig == "offline" && !isLoading,
                            enabled = !isLoading
                        )
                    }
                    item {
                        FilterChip(
                            onClick = { viewModel.usePresetConfiguration("performance") },
                            label = { Text("Performance") },
                            selected = selectedConfig == "performance" && !isLoading,
                            enabled = !isLoading
                        )
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
                Text("Reset")
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
                        Text("Provider initializing services...")
                        Text(
                            text = "Config: $selectedConfig | DAO: $selectedDaoType | API: $selectedApiType",
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
                            text = "Provider Error",
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
                                Text("Retry Service")
                            }
                            OutlinedButton(
                                onClick = { viewModel.usePresetConfiguration("offline") }
                            ) {
                                Text("Try Offline")
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
                                Icons.Default.Build,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Provider service returned no items",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Try different configuration or refresh",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                } else {
                    LazyColumn {
                        items(items) { item ->
                            ItemCard(
                                item = item,
                                configuration = "$selectedConfig/$selectedDaoType/$selectedApiType",
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
    
    // Configuration Dialog
    if (showConfigDialog) {
        ConfigurationDialog(
            availableConfigs = availableConfigs,
            availableDaoTypes = availableDaoTypes,
            availableApiTypes = availableApiTypes,
            selectedConfig = selectedConfig,
            selectedDaoType = selectedDaoType,
            selectedApiType = selectedApiType,
            onDismiss = { showConfigDialog = false },
            onApply = { config, dao, api ->
                viewModel.updateConfiguration(config, dao, api)
                showConfigDialog = false
            }
        )
    }
    
    // Add Item Dialog
    if (showAddDialog) {
        AddItemDialog(
            configuration = "$selectedConfig configuration",
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
private fun ConfigurationDialog(
    availableConfigs: List<String>,
    availableDaoTypes: List<String>,
    availableApiTypes: List<String>,
    selectedConfig: String,
    selectedDaoType: String,
    selectedApiType: String,
    onDismiss: () -> Unit,
    onApply: (String, String, String) -> Unit
) {
    var configChoice by remember { mutableStateOf(selectedConfig) }
    var daoChoice by remember { mutableStateOf(selectedDaoType) }
    var apiChoice by remember { mutableStateOf(selectedApiType) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configure Dependencies") },
        text = {
            Column {
                Text("Configuration Type:", fontWeight = FontWeight.Medium)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    items(availableConfigs) { config ->
                        FilterChip(
                            onClick = { configChoice = config },
                            label = { Text(config.uppercase()) },
                            selected = config == configChoice
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("DAO Type:", fontWeight = FontWeight.Medium)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    items(availableDaoTypes) { dao ->
                        FilterChip(
                            onClick = { daoChoice = dao },
                            label = { Text(dao.uppercase()) },
                            selected = dao == daoChoice
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("API Type:", fontWeight = FontWeight.Medium)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    items(availableApiTypes) { api ->
                        FilterChip(
                            onClick = { apiChoice = api },
                            label = { Text(api.uppercase()) },
                            selected = api == apiChoice
                        )
                    }
                }
                
                Text(
                    text = "Provider will create new service with selected dependencies",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onApply(configChoice, daoChoice, apiChoice) }
            ) {
                Text("Apply Configuration")
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
private fun ItemCard(
    item: Item,
    configuration: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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
                                configuration, 
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
    configuration: String,
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Item via Provider") },
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
                    text = "Using $configuration",
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