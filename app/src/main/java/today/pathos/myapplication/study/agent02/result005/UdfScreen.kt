package today.pathos.myapplication.study.agent02.result005

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import today.pathos.myapplication.study.common.Item
import today.pathos.myapplication.study.common.ScreenUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UdfScreen(
    viewModel: UdfViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val screenUiState by viewModel.screenUiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val lazyListState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    
    // Handle side effects
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is UdfEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is UdfEffect.LogError -> {
                    println("UDF Log: ${effect.error}")
                }
                is UdfEffect.ScrollToTop -> {
                    scope.launch {
                        lazyListState.animateScrollToItem(0)
                    }
                }
            }
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Unidirectional Data Flow Pattern",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.handleEvent(UdfEvent.RefreshItems) },
                    modifier = Modifier.weight(1f),
                    enabled = !state.isRefreshing
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (state.isRefreshing) "Refreshing..." else "Refresh")
                }
                
                Button(
                    onClick = {
                        val newItem = Item(
                            id = "udf_${System.currentTimeMillis()}",
                            title = "UDF Item",
                            description = "Added via UDF event"
                        )
                        viewModel.handleEvent(UdfEvent.AddItem(newItem))
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // UDF State Debug Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "UDF State Flow",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Stage: ${state.loadingStage} | Items: ${state.items.size} | Loading: ${state.isLoading}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Event → State → UI → Effect",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            when (screenUiState) {
                is ScreenUiState.Initializing -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("UDF Initializing...")
                            Text(
                                text = "Stage: ${state.loadingStage}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                
                is ScreenUiState.Failed -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("UDF Failed: ${(screenUiState as ScreenUiState.Failed).error}")
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.handleEvent(UdfEvent.RetryLoad) }) {
                                Text("Retry UDF Load")
                            }
                        }
                    }
                }
                
                is ScreenUiState.Succeed -> {
                    when {
                        state.isLoading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator()
                                    Text("UDF Loading...")
                                }
                            }
                        }
                        
                        state.error != null -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "UDF Error: ${state.error}",
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { viewModel.handleEvent(UdfEvent.ClearError) }
                                    ) {
                                        Text("Clear Error")
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                    
                    UdfItemList(
                        state = state,
                        lazyListState = lazyListState,
                        onEvent = viewModel::handleEvent
                    )
                }
            }
        }
    }
}

@Composable
private fun UdfItemList(
    state: UdfState,
    lazyListState: LazyListState,
    onEvent: (UdfEvent) -> Unit
) {
    LazyColumn(
        state = lazyListState,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(state.items) { item ->
            UdfItemCard(
                item = item,
                onRemove = { onEvent(UdfEvent.RemoveItem(item.id)) },
                onUpdate = { updatedItem ->
                    onEvent(UdfEvent.UpdateItem(updatedItem))
                }
            )
        }
        
        if (state.items.isEmpty() && !state.isLoading && state.error == null && state.loadingStage == LoadingStage.Loaded) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "UDF state is empty. Send an AddItem event to add items.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UdfItemCard(
    item: Item,
    onRemove: () -> Unit,
    onUpdate: (Item) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "UDF: Event → State → UI → Effect",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Row {
                IconButton(
                    onClick = {
                        val updatedItem = item.copy(
                            title = "${item.title} (UDF Updated)",
                            description = "${item.description} - Updated via UDF event"
                        )
                        onUpdate(updatedItem)
                    }
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Update")
                }
                
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}