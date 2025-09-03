package today.pathos.myapplication.study.agent02.result002

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import today.pathos.myapplication.study.common.Item
import today.pathos.myapplication.study.common.ScreenUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MvpScreen(
    presenter: MvpPresenter = viewModel()
) {
    // MVP View state
    var screenUiState by remember { mutableStateOf<ScreenUiState>(ScreenUiState.Initializing) }
    var items by remember { mutableStateOf<List<Item>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    
    // MVP View implementation
    val mvpView = remember {
        object : MvpContract.View {
            override fun showScreenState(state: ScreenUiState) {
                screenUiState = state
            }
            
            override fun showItems(newItems: List<Item>) {
                items = newItems
            }
            
            override fun showLoading(loading: Boolean) {
                isLoading = loading
            }
            
            override fun showRefreshing(refreshing: Boolean) {
                isRefreshing = refreshing
            }
            
            override fun showError(errorMessage: String?) {
                error = errorMessage
            }
            
            override fun clearError() {
                error = null
            }
        }
    }
    
    // Attach/detach view lifecycle
    DisposableEffect(presenter) {
        presenter.attachView(mvpView)
        onDispose {
            presenter.detachView()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "MVP with Compose Pattern",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { presenter.refreshItems() },
                modifier = Modifier.weight(1f),
                enabled = !isRefreshing
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isRefreshing) "Refreshing..." else "Refresh")
            }
            
            Button(
                onClick = {
                    val newItem = Item(
                        id = "new_${System.currentTimeMillis()}",
                        title = "New Item",
                        description = "Added via MVP"
                    )
                    presenter.addItem(newItem)
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        when (screenUiState) {
            is ScreenUiState.Initializing -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            is ScreenUiState.Failed -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Failed to initialize: ${(screenUiState as ScreenUiState.Failed).error}")
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { presenter.loadItems() }) {
                            Text("Retry")
                        }
                    }
                }
            }
            
            is ScreenUiState.Succeed -> {
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    
                    error != null -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                        ) {
                            Text(
                                text = "Error: $error",
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items) { item ->
                        MvpItemCard(
                            item = item,
                            onRemove = { presenter.removeItem(item.id) },
                            onUpdate = { updatedItem ->
                                presenter.updateItem(updatedItem)
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MvpItemCard(
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
            }
            
            Row {
                IconButton(
                    onClick = {
                        val updatedItem = item.copy(
                            title = "${item.title} (Updated)",
                            description = "${item.description} - Updated via MVP"
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