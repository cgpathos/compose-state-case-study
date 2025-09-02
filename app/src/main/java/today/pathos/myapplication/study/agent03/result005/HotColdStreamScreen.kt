package today.pathos.myapplication.study.agent03.result005

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotColdStreamScreen(
    viewModel: HotColdStreamViewModel = viewModel()
) {
    val screenState = viewModel.screenUiState
    val items by viewModel.itemsFlow.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingFlow.collectAsStateWithLifecycle()
    val error by viewModel.errorFlow.collectAsStateWithLifecycle()
    
    // Hot streams
    val operationEvents by viewModel.operationEvents.collectAsStateWithLifecycle(
        initialValue = HotColdStreamViewModel.OperationEvent(
            HotColdStreamViewModel.OperationEventType.INITIAL_LOAD,
            0L,
            "No events yet"
        )
    )
    val realTimeUpdates by viewModel.realTimeUpdates.collectAsStateWithLifecycle(
        initialValue = HotColdStreamViewModel.RealTimeUpdate(
            "System starting...",
            System.currentTimeMillis(),
            HotColdStreamViewModel.Severity.INFO
        )
    )
    val statusBroadcast by viewModel.statusBroadcast.collectAsStateWithLifecycle(initialValue = "Initializing...")
    
    // Cold stream with hot events
    val filteredItems by viewModel.filteredItemsFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text(
            text = "Agent03 Result005: Hot/Cold Stream Mixed",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        when (screenState) {
            is ScreenUiState.Initializing -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Text(
                            text = "Initializing Hot/Cold Streams...",
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Text(
                            text = statusBroadcast,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
            is ScreenUiState.Failed -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Failed to initialize: ${screenState.error}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Button(onClick = { viewModel.refresh() }) {
                        Text("Retry")
                    }
                }
            }
            is ScreenUiState.Succeed -> {
                // Hot stream status display
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Hot Stream Status",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = statusBroadcast,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Last Event:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = operationEvents.type.name,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = operationEvents.details,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Real-time:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Box(
                                    modifier = Modifier
                                        .background(
                                            when (realTimeUpdates.severity) {
                                                HotColdStreamViewModel.Severity.ERROR -> MaterialTheme.colorScheme.error
                                                HotColdStreamViewModel.Severity.WARNING -> MaterialTheme.colorScheme.secondary
                                                HotColdStreamViewModel.Severity.INFO -> MaterialTheme.colorScheme.primary
                                            },
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = realTimeUpdates.severity.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                                Text(
                                    text = realTimeUpdates.message,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
                
                // Action buttons including filter controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Button(
                        onClick = { viewModel.refresh() },
                        enabled = !isLoading,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Refresh", style = MaterialTheme.typography.bodySmall)
                    }
                    Button(
                        onClick = { viewModel.addItem() },
                        enabled = !isLoading,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Add", style = MaterialTheme.typography.bodySmall)
                    }
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Button(
                        onClick = { viewModel.filterHighPriority() },
                        enabled = !isLoading,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Filter High", style = MaterialTheme.typography.bodySmall)
                    }
                    Button(
                        onClick = { viewModel.filterRecent() },
                        enabled = !isLoading,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Filter Recent", style = MaterialTheme.typography.bodySmall)
                    }
                    Button(
                        onClick = { viewModel.clearFilter() },
                        enabled = !isLoading,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Clear", style = MaterialTheme.typography.bodySmall)
                    }
                }

                // Error message
                error?.let { errorMessage ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = "Hot/Cold Stream Error: $errorMessage",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                // Loading indicator
                if (isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )
                }

                // Display both regular items and filtered items
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Regular items (Cold stream)
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Text(
                                text = "All Items (Cold Stream)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        items(items, key = { "all_${it.id}" }) { item ->
                            HotColdItemCard(
                                item = item,
                                streamType = "Cold",
                                onUpdate = { viewModel.updateItem(item) },
                                onRemove = { viewModel.removeItem(item.id) },
                                isOperationInProgress = isLoading,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Filtered items (Mixed Hot/Cold stream)
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Text(
                                text = "Filtered (Hot Events + Cold Data)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        items(filteredItems, key = { "filtered_${it.id}" }) { item ->
                            HotColdItemCard(
                                item = item,
                                streamType = "Mixed",
                                onUpdate = { viewModel.updateItem(item) },
                                onRemove = { viewModel.removeItem(item.id) },
                                isOperationInProgress = isLoading,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HotColdItemCard(
    item: Item,
    streamType: String,
    onUpdate: () -> Unit,
    onRemove: () -> Unit,
    isOperationInProgress: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = when (streamType) {
                "Cold" -> MaterialTheme.colorScheme.surfaceVariant
                "Mixed" -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall
                )
                Badge(
                    containerColor = when (streamType) {
                        "Cold" -> MaterialTheme.colorScheme.primary
                        "Mixed" -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.tertiary
                    }
                ) {
                    Text(
                        text = streamType,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 2.dp)
            )
            Text(
                text = "ID: ${item.id}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Button(
                    onClick = onUpdate,
                    enabled = !isOperationInProgress,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Update", style = MaterialTheme.typography.labelSmall)
                }
                Button(
                    onClick = onRemove,
                    enabled = !isOperationInProgress,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Remove", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}