package today.pathos.myapplication.study

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

data class AgentInfo(
    val title: String,
    val description: String,
    val results: List<ResultInfo>
)

data class ResultInfo(
    val name: String,
    val description: String,
    val route: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyHomeScreen(navController: NavController) {
    val agents = listOf(
        AgentInfo(
            title = "Agent01: State Management",
            description = "다양한 상태 관리 방식을 보여주는 구현체",
            results = listOf(
                ResultInfo("StateFlow + Sealed", "StateFlow와 Sealed Class 패턴", "agent01_result001"),
                ResultInfo("MutableState + Data", "MutableState와 data class 패턴", "agent01_result002"),
                ResultInfo("LiveData + Transform", "LiveData와 Transformation 패턴", "agent01_result003"),
                ResultInfo("SharedFlow + Event", "SharedFlow와 Event 패턴", "agent01_result004"),
                ResultInfo("Molecule Library", "Molecule 라이브러리 스타일 패턴", "agent01_result005")
            )
        ),
        AgentInfo(
            title = "Agent02: Architecture Pattern",
            description = "다양한 아키텍처 패턴을 보여주는 구현체",
            results = listOf(
                ResultInfo("MVI Pattern", "Model-View-Intent 패턴", "agent02_result001"),
                ResultInfo("MVP with Compose", "MVP와 Compose 조합 패턴", "agent02_result002"),
                ResultInfo("Clean Architecture", "Clean Architecture 패턴", "agent02_result003"),
                ResultInfo("Redux-like", "Redux 스타일 패턴", "agent02_result004"),
                ResultInfo("Unidirectional Data Flow", "단방향 데이터 흐름 패턴", "agent02_result005")
            )
        ),
        AgentInfo(
            title = "Agent03: Reactive Programming",
            description = "다양한 반응형 프로그래밍 방식을 보여주는 구현체",
            results = listOf(
                ResultInfo("Flow Chain", "Coroutines Flow Chain 패턴", "agent03_result001"),
                ResultInfo("RxJava3 Integration", "RxJava3 통합 패턴", "agent03_result002"),
                ResultInfo("Channel + Actor", "Channel과 Actor 모델 패턴", "agent03_result003"),
                ResultInfo("Combined Flows", "여러 Flow 결합 패턴", "agent03_result004"),
                ResultInfo("Hot/Cold Streams", "Hot/Cold Stream 혼합 패턴", "agent03_result005")
            )
        ),
        AgentInfo(
            title = "Agent04: Compose State",
            description = "Compose 네이티브 상태 관리 방식을 보여주는 구현체",
            results = listOf(
                ResultInfo("remember + mutableStateOf", "기본 remember 패턴", "agent04_result001"),
                ResultInfo("rememberSaveable", "Parcelable과 Saveable 패턴", "agent04_result002"),
                ResultInfo("CompositionLocal", "CompositionLocal Provider 패턴", "agent04_result003"),
                ResultInfo("State Hoisting", "State Hoisting 패턴", "agent04_result004"),
                ResultInfo("Snapshot State", "Snapshot State System 패턴", "agent04_result005")
            )
        ),
        AgentInfo(
            title = "Agent05: Hybrid Approach",
            description = "하이브리드 및 고급 아키텍처 방식을 보여주는 구현체",
            results = listOf(
                ResultInfo("UseCase Pattern", "ViewModel + Repository + UseCase 패턴", "agent05_result001"),
                ResultInfo("Delegate Pattern", "StateHolder + Delegate 패턴", "agent05_result002"),
                ResultInfo("Singleton Manager", "Singleton State Manager 패턴", "agent05_result003"),
                ResultInfo("Event Bus", "Event Bus + State Store 패턴", "agent05_result004"),
                ResultInfo("Factory + DI", "Factory Pattern + DI 패턴", "agent05_result005")
            )
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "ScreenComposable + ViewModel Study",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Text(
            text = "25개의 다양한 구현 방식을 탐색해보세요",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(agents) { agent ->
                AgentCard(
                    agent = agent,
                    onResultClick = { route ->
                        navController.navigate(route)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentCard(
    agent: AgentInfo,
    onResultClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = agent.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = agent.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )
            
            agent.results.forEach { result ->
                Card(
                    onClick = { onResultClick(result.route) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = result.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = result.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}