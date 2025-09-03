package today.pathos.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import today.pathos.myapplication.ui.theme.MyApplicationTheme
import today.pathos.myapplication.study.StudyHomeScreen
import today.pathos.myapplication.study.agent01.result001.Screen as Agent01Result001Screen
import today.pathos.myapplication.study.agent01.result002.Screen as Agent01Result002Screen
import today.pathos.myapplication.study.agent01.result003.Screen as Agent01Result003Screen
import today.pathos.myapplication.study.agent01.result004.Screen as Agent01Result004Screen
import today.pathos.myapplication.study.agent01.result005.Screen as Agent01Result005Screen
import today.pathos.myapplication.study.agent01.result006.Screen as Agent01Result006Screen
import today.pathos.myapplication.study.agent01.result007.Screen as Agent01Result007Screen
import today.pathos.myapplication.study.agent02.result001.MviScreen
import today.pathos.myapplication.study.agent02.result002.MvpScreen
import today.pathos.myapplication.study.agent02.result003.CleanArchScreen
import today.pathos.myapplication.study.agent02.result004.ReduxScreen
import today.pathos.myapplication.study.agent02.result005.UdfScreen
import today.pathos.myapplication.study.agent03.result001.FlowChainScreen
import today.pathos.myapplication.study.agent03.result002.RxJavaScreen
import today.pathos.myapplication.study.agent03.result003.ActorModelScreen
import today.pathos.myapplication.study.agent03.result004.CombineFlowsScreen
import today.pathos.myapplication.study.agent03.result005.HotColdStreamScreen
import today.pathos.myapplication.study.agent04.result001.Agent04Result001Screen
import today.pathos.myapplication.study.agent04.result002.Agent04Result002Screen
import today.pathos.myapplication.study.agent04.result003.Agent04Result003Screen
import today.pathos.myapplication.study.agent04.result004.Agent04Result004Screen
import today.pathos.myapplication.study.agent04.result005.Agent04Result005Screen

/*
TODO : PRD
ScreenComposable + ViewModel 다양한 방식의 구현

## 필수 요구사항
- Initializing, Succeed, Failed 의 화면 초기 접근시 ScreenUiState 존재
- ScreenUiState 는 화면 초기화에만 사용되고 업데이트 되지 않음.
- 초기화 데이터 기반으로 화면을 지속적으로 상태 업데이트가 가능해야 함.
    - e.g. reload, list에 아이템 추가/삭제/업데이트
- 5개의 서브에이전트를 사용해서 병렬로 각기 다른 방식으로 5개의 결과물을 만들어낼것.
- today.pathos.myapplication.study 안에 에이전트 별로 서브 패키지를 만들어서 Screen,ViewModel을 생성할것
  e.g.  today.pathos.myapplication.study.agent01/result001/Screen.kt ...

 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                Scaffold(

                ) { paddingValues ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(paddingValues)
                    ) {
                        composable("home") {
                            StudyHomeScreen(navController)
                        }

                        // Agent01 routes
                        composable("agent01_result001") {
                            Agent01Result001Screen()
                        }
                        composable("agent01_result002") {
                            Agent01Result002Screen()
                        }
                        composable("agent01_result003") {
                            Agent01Result003Screen()
                        }
                        composable("agent01_result004") {
                            Agent01Result004Screen()
                        }
                        composable("agent01_result005") {
                            Agent01Result005Screen()
                        }
                        composable("agent01_result006") {
                            Agent01Result006Screen()
                        }
                        composable("agent01_result007") {
                            Agent01Result007Screen()
                        }
                        
                        // Agent02 routes
                        composable("agent02_result001") {
                            MviScreen()
                        }
                        composable("agent02_result002") {
                            MvpScreen()
                        }
                        composable("agent02_result003") {
                            CleanArchScreen()
                        }
                        composable("agent02_result004") {
                            ReduxScreen()
                        }
                        composable("agent02_result005") {
                            UdfScreen()
                        }
                        
                        // Agent03 routes
                        composable("agent03_result001") {
                            FlowChainScreen()
                        }
                        composable("agent03_result002") {
                            RxJavaScreen()
                        }
                        composable("agent03_result003") {
                            ActorModelScreen()
                        }
                        composable("agent03_result004") {
                            CombineFlowsScreen()
                        }
                        composable("agent03_result005") {
                            HotColdStreamScreen()
                        }
                        
                        // Agent04 routes
                        composable("agent04_result001") {
                            Agent04Result001Screen()
                        }
                        composable("agent04_result002") {
                            Agent04Result002Screen()
                        }
                        composable("agent04_result003") {
                            Agent04Result003Screen()
                        }
                        composable("agent04_result004") {
                            Agent04Result004Screen()
                        }
                        composable("agent04_result005") {
                            Agent04Result005Screen()
                        }
                    }
                }
            }
        }
    }
}
