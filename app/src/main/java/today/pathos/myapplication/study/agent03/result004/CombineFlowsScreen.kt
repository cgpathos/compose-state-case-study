package today.pathos.myapplication.study.agent03.result004

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import today.pathos.myapplication.study.common.Item
import today.pathos.myapplication.study.common.ScreenUiState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CombineFlowsScreen(
    viewModel: CombineFlowsViewModel = viewModel()
) {
    val screenState = viewModel.screenUiState
    val combinedState by viewModel.combinedUiState.collectAsStateWithLifecycle()
    val itemCount by viewModel.itemCountFlow.collectAsStateWithLifecycle()
    val hasError by viewModel.hasErrorFlow.collectAsStateWithLifecycle()
    val recentItems by viewModel.recentItemsFlow.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text(
            text = "Agent03 Result004: Combine Multiple Flows",
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
                            text = "Combining Multiple Flows...",
                            modifier = Modifier.padding(top = 8.dp)
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
                // Combined state info card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Combined Flow State",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Items: $itemCount",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "Recent: ${recentItems.size}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "Operations: ${combinedState.operationCount}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Column {
                                Text(
                                    text = "Last Op: ${combinedState.lastOperation ?: "None"}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = if (combinedState.isDataStale) "Data: Stale" else "Data: Fresh",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (combinedState.isDataStale) 
                                        MaterialTheme.colorScheme.error 
                                    else 
                                        MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Error: ${if (hasError) "Yes" else "No"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (hasError) 
                                        MaterialTheme.colorScheme.error 
                                    else 
                                        MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        Text(
                            text = "Last Update: ${formatTimestamp(combinedState.lastUpdateTime)}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                
                // Action buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.refresh() },
                        enabled = !combinedState.isLoading
                    ) {
                        Text("Refresh")
                    }
                    Button(
                        onClick = { viewModel.addItem() },
                        enabled = !combinedState.isLoading
                    ) {
                        Text("Add Item")
                    }
                }

                // Error message
                combinedState.error?.let { errorMessage ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = "Combined Flow Error: $errorMessage",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                // Loading indicator
                if (combinedState.isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )
                }

                // Items list with combined state
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(combinedState.items, key = { it.id }) { item ->
                        CombinedFlowItemCard(
                            item = item,
                            isRecent = recentItems.contains(item),
                            onUpdate = { viewModel.updateItem(item) },
                            onRemove = { viewModel.removeItem(item.id) },
                            isOperationInProgress = combinedState.isLoading
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CombinedFlowItemCard(
    item: Item,
    isRecent: Boolean,
    onUpdate: () -> Unit,
    onRemove: () -> Unit,
    isOperationInProgress: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isRecent) 
                MaterialTheme.colorScheme.primaryContainer
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
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
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium
                )
                if (isRecent) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = "Recent",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
            
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Text(
                text = "ID: ${item.id} | Time: ${item.timestamp}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "State managed by combined flows",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onUpdate,
                    enabled = !isOperationInProgress,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Update")
                }
                Button(
                    onClick = onRemove,
                    enabled = !isOperationInProgress,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Remove")
                }
            }
        }
    }
}

@Composable
private fun formatTimestamp(timestamp: Long): String {
    val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return formatter.format(Date(timestamp))
}