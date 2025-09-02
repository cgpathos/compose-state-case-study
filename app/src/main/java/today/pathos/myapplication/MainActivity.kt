package today.pathos.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import today.pathos.myapplication.ui.theme.MyApplicationTheme
import today.pathos.myapplication.study.StudyHomeScreen
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
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Text(
                        text = "25개의 구현체가 성공적으로 생성되었습니다!\n각 Agent별로 5개씩 총 25개의 다양한 방식으로 구현되었습니다.",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
