package today.pathos.myapplication.study.agent01.result007

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.PlayArrow
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Screen(
    viewModel: FactoryViewModel = viewModel()
) {
    val screenUiState by viewModel.screenUiState.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Factory Method Pattern (No init{})",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "팩토리 메서드로 상태 생성, 수동 초기화 방식",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        when (screenUiState) {
            is ScreenUiState.Initializing -> {
                when (uiState) {
                    is FactoryUiState.Uninitialized -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Ready to Initialize",
                                    style = MaterialTheme.typography.headlineSmall
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "팩토리 메서드로 상태를 생성합니다",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { viewModel.initialize() }
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("초기화 시작")
                                }
                            }
                        }
                    }
                    
                    is FactoryUiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Factory creating states...")
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "상태 팩토리가 작동 중입니다",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                    else -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Unexpected state during initialization")
                        }
                    }
                }
            }
            
            is ScreenUiState.Failed -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Factory Initialization Failed",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = (screenUiState as ScreenUiState.Failed).error,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            when (val currentUiState = uiState) {
                                is FactoryUiState.Error -> {
                                    if (currentUiState.canRetry) {
                                        Button(
                                            onClick = { viewModel.retry() }
                                        ) {
                                            Text("재시도")
                                        }
                                    } else {
                                        Text(
                                            text = "Fatal error - cannot retry",
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                                else -> {
                                    Button(
                                        onClick = { viewModel.initialize() }
                                    ) {
                                        Text("재시도")
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            is ScreenUiState.Succeed -> {
                when (val currentUiState = uiState) {
                    is FactoryUiState.Success -> {
                        SuccessContent(
                            uiState = currentUiState,
                            viewModel = viewModel
                        )
                    }
                    
                    is FactoryUiState.Error -> {
                        ErrorContent(
                            errorState = currentUiState,
                            onRetry = { viewModel.retry() },
                            onClear = { viewModel.clearError() }
                        )
                    }
                    
                    else -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Unexpected state in succeed screen")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SuccessContent(
    uiState: FactoryUiState.Success,
    viewModel: FactoryViewModel
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedStrategy by remember { mutableStateOf(FactoryViewModel.ItemCreationStrategy.DEFAULT) }
    
    // 액션 버튼들
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
            Text("추가")
        }
        
        Button(
            onClick = { viewModel.refresh() },
            modifier = Modifier.weight(1f),
            enabled = !uiState.isRefreshing
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(4.dp))
            Text("새로고침")
        }
    }
    
    // 전략 선택
    Spacer(modifier = Modifier.height(8.dp))
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Item Creation Strategy:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FactoryViewModel.ItemCreationStrategy.values().forEach { strategy ->
                    FilterChip(
                        onClick = { selectedStrategy = strategy },
                        label = { 
                            Text(
                                text = strategy.name,
                                style = MaterialTheme.typography.labelSmall
                            ) 
                        },
                        selected = selectedStrategy == strategy
                    )
                }
            }
        }
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    
    // 새로고침 상태 표시
    if (uiState.isRefreshing) {
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
    }
    
    // 상태 정보
    Text(
        text = "Last updated: ${java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(uiState.lastUpdated))}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 16.dp)
    )
    
    // 아이템 목록
    if (uiState.items.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "No items created by factory",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "팩토리 메서드로 아이템을 생성해보세요",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = uiState.items,
                key = { it.id }
            ) { item ->
                ItemCard(
                    item = item,
                    onUpdate = { updatedItem ->
                        viewModel.updateItem(updatedItem)
                    },
                    onDelete = { viewModel.removeItem(item.id) }
                )
            }
        }
    }
    
    if (showAddDialog) {
        AddItemDialog(
            selectedStrategy = selectedStrategy,
            onDismiss = { showAddDialog = false },
            onAdd = { title, description ->
                viewModel.createItemWithStrategy(title, description, selectedStrategy)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun ErrorContent(
    errorState: FactoryUiState.Error,
    onRetry: () -> Unit,
    onClear: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Factory Error",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorState.message,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Last attempt: ${java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(errorState.lastAttempt))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (errorState.canRetry) {
                    Button(onClick = onRetry) {
                        Text("Retry")
                    }
                } else {
                    Button(
                        onClick = onClear,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text("Reset")
                    }
                }
                
                TextButton(onClick = onClear) {
                    Text("Clear")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemCard(
    item: Item,
    onUpdate: (Item) -> Unit,
    onDelete: () -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { showEditDialog = true }) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("수정")
                }
                TextButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("삭제")
                }
            }
        }
    }
    
    if (showEditDialog) {
        EditItemDialog(
            item = item,
            onDismiss = { showEditDialog = false },
            onUpdate = { updatedItem ->
                onUpdate(updatedItem)
                showEditDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddItemDialog(
    selectedStrategy: FactoryViewModel.ItemCreationStrategy,
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text("Factory Create Item") 
        },
        text = {
            Column {
                Text(
                    text = "Strategy: ${selectedStrategy.name}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("제목") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("설명") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(title, description) },
                enabled = title.isNotBlank() && description.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditItemDialog(
    item: Item,
    onDismiss: () -> Unit,
    onUpdate: (Item) -> Unit
) {
    var title by remember { mutableStateOf(item.title) }
    var description by remember { mutableStateOf(item.description) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("아이템 수정") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("제목") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("설명") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onUpdate(item.copy(title = title, description = description)) },
                enabled = title.isNotBlank() && description.isNotBlank()
            ) {
                Text("수정")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}