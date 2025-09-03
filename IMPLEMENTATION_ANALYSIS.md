# ScreenComposable + ViewModel 25가지 구현 분석

## 📋 프로젝트 개요

이 프로젝트는 Android Compose에서 ScreenComposable과 ViewModel을 활용한 상태 관리의 다양한 접근법을 학습하기 위해 **25개의 서로 다른 구현체**를 제작한 연구 프로젝트입니다.

### 🎯 PRD 핵심 요구사항

1. **ScreenUiState**: Initializing, Succeed, Failed 상태를 가진 화면 초기 접근 상태
2. **초기화 전용**: ScreenUiState는 화면 초기화에만 사용되고 업데이트되지 않음
3. **지속적 상태 업데이트**: 초기화 후 reload, 리스트 아이템 추가/삭제/업데이트 가능
4. **5개 서브에이전트**: 각각 다른 방식으로 5개씩 총 25개 결과물
5. **패키지 구조**: `today.pathos.myapplication.study.agentXX.resultXXX` 형태

### 🏗️ 전체 아키텍처

```
study/
├── common/           # 공통 모델과 기본 클래스
│   ├── Item.kt
│   ├── ScreenUiState.kt
│   └── BaseViewModel.kt
├── agent01/          # State Management 전문
├── agent02/          # Architecture Pattern 전문  
├── agent03/          # Reactive Programming 전문
├── agent04/          # Compose State 전문
└── agent05/          # Hybrid Approach 전문
```

## 🔍 Agent별 상세 분석

### Agent01: State Management 전문

#### Result001: StateFlow + Sealed Class Pattern
```kotlin
// UiState.kt - 타입 안전한 상태 표현
sealed class UiState {
    object Loading : UiState()
    data class Success(val items: List<Item>) : UiState()
    data class Error(val message: String) : UiState()
}

// ViewModel.kt - StateFlow로 반응형 상태 관리
class StateFlowViewModel : BaseViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    private val _screenUiState = MutableStateFlow<ScreenUiState>(ScreenUiState.Initializing)
    val screenUiState: StateFlow<ScreenUiState> = _screenUiState.asStateFlow()
}
```

**핵심 특징:**
- **불변성**: Sealed class로 상태의 불변성 보장
- **타입 안전성**: 컴파일 타임에 모든 상태 케이스 처리 강제
- **명확한 상태 전환**: when 문으로 명시적 상태 처리
- **성능**: StateFlow의 효율적인 상태 전파

#### Result002: MutableState + Data Class Pattern
```kotlin
// UiState.kt - 단순한 데이터 클래스 접근법
data class UiState(
    val items: List<Item> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
) {
    val hasError: Boolean get() = error != null
    val isEmpty: Boolean get() = items.isEmpty() && !isLoading
}

// ViewModel.kt - Compose의 mutableStateOf 직접 사용
class MutableStateViewModel : BaseViewModel() {
    var uiState by mutableStateOf(UiState(isLoading = true))
        private set
}
```

**핵심 특징:**
- **단순성**: 복잡한 StateFlow 설정 불필요
- **Compose 네이티브**: mutableStateOf를 통한 자동 리컴포지션
- **직관적**: 상태 업데이트가 직접적이고 이해하기 쉬움
- **계산된 속성**: 상태로부터 파생된 값들을 getter로 제공

#### Result003: LiveData + Transformation Pattern
```kotlin
// ViewModel.kt - LiveData의 Transformation 활용
class LiveDataViewModel : BaseViewModel() {
    private val _items = MutableLiveData<List<Item>>()
    val items: LiveData<List<Item>> = _items
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    // Transformation을 통한 파생 상태
    val hasItems: LiveData<Boolean> = Transformations.map(items) { 
        it.isNotEmpty() 
    }
}
```

**핵심 특징:**
- **생명주기 인식**: 자동 구독/해제로 메모리 누수 방지
- **Transformation**: map, switchMap을 통한 파생 상태 생성
- **기존 코드 호환성**: 전통적 Android 앱과의 호환성
- **관찰 패턴**: 명시적 옵저버 패턴 구현

#### Result004: SharedFlow + Event Pattern
```kotlin
// UiEvent.kt - 이벤트 기반 상태 업데이트
sealed class UiEvent {
    object LoadItems : UiEvent()
    object RefreshItems : UiEvent()
    data class AddItem(val item: Item) : UiEvent()
    data class RemoveItem(val itemId: String) : UiEvent()
}

// ViewModel.kt - SharedFlow로 이벤트 스트림 처리
class SharedFlowViewModel : BaseViewModel() {
    private val _events = MutableSharedFlow<UiEvent>()
    
    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()
    
    init {
        _events.collect { event ->
            when (event) {
                is UiEvent.LoadItems -> handleLoadItems()
                is UiEvent.AddItem -> handleAddItem(event.item)
                // ...
            }
        }
    }
}
```

**핵심 특징:**
- **이벤트 중심**: 사용자 액션을 이벤트로 모델링
- **백프레셔 처리**: SharedFlow의 버퍼링과 오버플로우 전략
- **디버깅 용이**: 이벤트 스트림 추적 가능
- **확장성**: 새로운 이벤트 타입 추가 용이

#### Result005: Molecule-like Pattern
```kotlin
// MoleculeState.kt - 함수형 반응 프로그래밍 스타일
class MoleculeState {
    sealed class Action {
        object Initialize : Action()
        object Refresh : Action()
        data class AddItem(val item: Item) : Action()
    }
    
    data class Signal(
        val action: Action,
        val timestamp: Long = System.currentTimeMillis()
    )
}

// ViewModel.kt - Signal과 scan을 활용한 상태 머신
class MoleculeViewModel : BaseViewModel() {
    private val signalFlow = MutableSharedFlow<Signal>()
    
    val uiState: StateFlow<UiState> = signalFlow
        .scan(UiState()) { state, signal ->
            when (signal.action) {
                is Action.Initialize -> state.copy(isLoading = true)
                is Action.AddItem -> state.copy(
                    items = state.items + signal.action.item
                )
                // ...
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, UiState())
}
```

**핵심 특징:**
- **함수형 접근**: scan을 통한 상태 누적 변환
- **시간 추적**: 액션에 타임스탬프 포함
- **예측 가능성**: 순수 함수를 통한 상태 변환
- **테스트 용이**: 액션과 상태 변환 로직 분리

### Agent02: Architecture Pattern 전문

#### Result001: MVI (Model-View-Intent) Pattern
```kotlin
// ViewIntent.kt - 사용자 의도 모델링
sealed class ViewIntent {
    object LoadItems : ViewIntent()
    object RefreshItems : ViewIntent()
    data class AddItem(val title: String, val description: String) : ViewIntent()
    data class RemoveItem(val itemId: String) : ViewIntent()
    data class UpdateItem(val item: Item) : ViewIntent()
}

// ViewState.kt - 단일 상태 표현
data class ViewState(
    val screenState: ScreenUiState = ScreenUiState.Initializing,
    val items: List<Item> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
)

// MviViewModel.kt - Intent 처리와 상태 변환
class MviViewModel : BaseViewModel() {
    private val _viewState = MutableStateFlow(ViewState())
    val viewState: StateFlow<ViewState> = _viewState.asStateFlow()
    
    fun handleIntent(intent: ViewIntent) {
        when (intent) {
            is ViewIntent.LoadItems -> loadItems()
            is ViewIntent.AddItem -> addItem(intent.title, intent.description)
            // ...
        }
    }
}
```

**핵심 특징:**
- **단방향 데이터 흐름**: Intent → Model → View의 명확한 흐름
- **상태 중앙화**: 단일 ViewState로 모든 UI 상태 관리
- **의도 기반**: 사용자 의도를 명시적으로 모델링
- **예측 가능성**: 동일한 Intent는 항상 동일한 상태 변화 유발

#### Result002: MVP with Compose Pattern
```kotlin
// MvpContract.kt - View와 Presenter 간 계약 정의
interface MvpContract {
    interface View {
        fun showLoading()
        fun hideLoading()
        fun showItems(items: List<Item>)
        fun showError(error: String)
        fun clearError()
    }
    
    interface Presenter {
        fun attachView(view: View)
        fun detachView()
        fun loadItems()
        fun addItem(title: String, description: String)
        fun removeItem(itemId: String)
        fun refresh()
    }
}

// MvpPresenter.kt - 비즈니스 로직 처리
class MvpPresenter : BaseViewModel(), MvpContract.Presenter {
    private var view: MvpContract.View? = null
    
    override fun loadItems() {
        view?.showLoading()
        viewModelScope.launch {
            delay(2000) // 시뮬레이션
            if (shouldSimulateError()) {
                view?.showError("Failed to load items")
            } else {
                val items = generateInitialItems()
                view?.showItems(items)
            }
            view?.hideLoading()
        }
    }
}
```

**핵심 특징:**
- **관심사 분리**: View와 Presenter의 명확한 역할 분담
- **테스트 용이성**: Presenter 로직을 View와 독립적으로 테스트
- **계약 기반**: 인터페이스를 통한 느슨한 결합
- **생명주기 관리**: Presenter의 View 참조 생명주기 관리

#### Result003: Clean Architecture Pattern
```kotlin
// ItemRepository.kt - 데이터 계층 추상화
interface ItemRepository {
    suspend fun getItems(): List<Item>
    suspend fun addItem(item: Item)
    suspend fun removeItem(itemId: String)
    suspend fun updateItem(item: Item)
}

class ItemRepositoryImpl : ItemRepository {
    private val items = mutableListOf<Item>()
    
    override suspend fun getItems(): List<Item> {
        delay(2000) // 네트워크 지연 시뮬레이션
        if (Random.nextDouble() < 0.2) {
            throw Exception("Failed to fetch items")
        }
        return items.toList()
    }
}

// GetItemsUseCase.kt - 도메인 계층 비즈니스 로직
class GetItemsUseCase(private val repository: ItemRepository) {
    suspend operator fun invoke(): List<Item> {
        return repository.getItems()
    }
}

// CleanArchViewModel.kt - 프레젠테이션 계층
class CleanArchViewModel(
    private val getItemsUseCase: GetItemsUseCase,
    private val addItemUseCase: AddItemUseCase,
    private val removeItemUseCase: RemoveItemUseCase
) : BaseViewModel() {
    // ViewModel은 UseCase를 통해서만 데이터에 접근
}
```

**핵심 특징:**
- **계층 분리**: Data, Domain, Presentation 계층의 명확한 분리
- **의존성 역전**: 고수준 모듈이 저수준 모듈에 의존하지 않음
- **단일 책임**: 각 UseCase는 하나의 비즈니스 로직만 담당
- **테스트 용이성**: 각 계층을 독립적으로 테스트 가능

#### Result004: Redux-like Pattern
```kotlin
// ReduxAction.kt - 모든 가능한 액션 정의
sealed class ReduxAction {
    object InitializeScreen : ReduxAction()
    object LoadItemsStart : ReduxAction()
    data class LoadItemsSuccess(val items: List<Item>) : ReduxAction()
    data class LoadItemsError(val error: String) : ReduxAction()
    data class AddItem(val item: Item) : ReduxAction()
}

// ReduxState.kt - 불변 상태 표현
data class ReduxState(
    val screenState: ScreenUiState = ScreenUiState.Initializing,
    val items: List<Item> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

// ReduxReducer.kt - 순수 함수로 상태 변환
object ReduxReducer {
    fun reduce(state: ReduxState, action: ReduxAction): ReduxState {
        return when (action) {
            is ReduxAction.LoadItemsStart -> state.copy(isLoading = true, error = null)
            is ReduxAction.LoadItemsSuccess -> state.copy(
                items = action.items,
                isLoading = false,
                screenState = ScreenUiState.Succeed
            )
            is ReduxAction.AddItem -> state.copy(
                items = state.items + action.item
            )
            // ...
        }
    }
}

// ReduxStore.kt - 중앙 집중식 상태 관리
class ReduxStore {
    private val _state = MutableStateFlow(ReduxState())
    val state: StateFlow<ReduxState> = _state.asStateFlow()
    
    fun dispatch(action: ReduxAction) {
        val currentState = _state.value
        val newState = ReduxReducer.reduce(currentState, action)
        _state.value = newState
    }
}
```

**핵심 특징:**
- **예측 가능성**: 동일한 상태와 액션은 항상 동일한 결과 생성
- **시간 여행**: 액션 로그를 통한 디버깅과 상태 되돌리기
- **중앙 집중**: 모든 상태 변화가 스토어를 통과
- **순수 함수**: Reducer는 사이드 이펙트 없는 순수 함수

#### Result005: Unidirectional Data Flow Pattern
```kotlin
// UdfEvent.kt - 사용자 및 시스템 이벤트
sealed class UdfEvent {
    object LoadItems : UdfEvent()
    object RefreshItems : UdfEvent()
    data class AddItem(val title: String, val description: String) : UdfEvent()
    data class RemoveItem(val itemId: String) : UdfEvent()
}

// UdfEffect.kt - 사이드 이펙트 정의
sealed class UdfEffect {
    data class ShowSnackbar(val message: String) : UdfEffect()
    data class LogEvent(val event: String) : UdfEffect()
    object ScrollToTop : UdfEffect()
}

// UdfState.kt - UI 상태와 로딩 단계 분리
data class UdfState(
    val screenState: ScreenUiState = ScreenUiState.Initializing,
    val items: List<Item> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val loadingStage: String = ""
)

// UdfViewModel.kt - 이벤트 처리와 이펙트 방출
class UdfViewModel : BaseViewModel() {
    private val _uiState = MutableStateFlow(UdfState())
    val uiState: StateFlow<UdfState> = _uiState.asStateFlow()
    
    private val _effects = MutableSharedFlow<UdfEffect>()
    val effects: SharedFlow<UdfEffect> = _effects.asSharedFlow()
    
    fun handleEvent(event: UdfEvent) {
        when (event) {
            is UdfEvent.AddItem -> {
                // 상태 업데이트
                val newItem = Item(...)
                _uiState.update { it.copy(items = it.items + newItem) }
                
                // 이펙트 방출
                _effects.tryEmit(UdfEffect.ShowSnackbar("Item added"))
                _effects.tryEmit(UdfEffect.ScrollToTop)
            }
        }
    }
}
```

**핵심 특징:**
- **명확한 데이터 흐름**: Event → State Update → Effect의 단방향 흐름
- **사이드 이펙트 분리**: UI 상태와 사이드 이펙트의 명확한 분리
- **반응형 UI**: SharedFlow를 통한 실시간 이펙트 처리
- **확장성**: 새로운 이벤트와 이펙트 타입 추가 용이

### Agent03: Reactive Programming 전문

#### Result001: Coroutines Flow Chain Pattern
```kotlin
// FlowChainViewModel.kt - Flow 체이닝을 통한 데이터 변환
class FlowChainViewModel : BaseViewModel() {
    private val operationTrigger = MutableSharedFlow<FlowOperation>()
    
    val uiState: StateFlow<FlowUiState> = operationTrigger
        .onStart { emit(FlowOperation.Initialize) }
        .flatMapLatest { operation ->
            when (operation) {
                is FlowOperation.LoadItems -> loadItemsFlow()
                is FlowOperation.RefreshItems -> refreshItemsFlow()
                is FlowOperation.AddItem -> addItemFlow(operation.item)
            }
        }
        .catch { emit(FlowUiState.Error(it.message ?: "Unknown error")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FlowUiState.Loading
        )
    
    private fun loadItemsFlow(): Flow<FlowUiState> = flow {
        emit(FlowUiState.Loading)
        delay(2000)
        
        if (shouldSimulateError()) {
            emit(FlowUiState.Error("Failed to load items"))
        } else {
            val items = generateInitialItems()
            emit(FlowUiState.Success(items))
        }
    }
}
```

**핵심 특징:**
- **체이닝**: flatMapLatest를 통한 연속적인 Flow 변환
- **백프레셔 처리**: Flow의 자동 백프레셔 관리
- **취소 지원**: 코루틴 기반 자동 취소 처리
- **에러 처리**: catch 연산자를 통한 통합 에러 처리

#### Result002: RxJava3 Integration Pattern
```kotlin
// RxJavaViewModel.kt - RxJava와 Android 통합
class RxJavaViewModel : BaseViewModel() {
    private val compositeDisposable = CompositeDisposable()
    
    private val operationSubject = PublishSubject.create<RxOperation>()
    private val itemsSubject = BehaviorSubject.createDefault<List<Item>>(emptyList())
    private val loadingSubject = BehaviorSubject.createDefault(false)
    
    val uiState: StateFlow<RxUiState> = Observable
        .combineLatest(
            itemsSubject.distinctUntilChanged(),
            loadingSubject.distinctUntilChanged(),
            errorSubject.distinctUntilChanged()
        ) { items, isLoading, error ->
            RxUiState(items = items, isLoading = isLoading, error = error)
        }
        .observeOn(AndroidSchedulers.mainThread())
        .toFlow() // RxJava를 Flow로 브리지
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = RxUiState()
        )
    
    init {
        operationSubject
            .throttleFirst(300, TimeUnit.MILLISECONDS) // 중복 클릭 방지
            .observeOn(Schedulers.io())
            .subscribe { operation ->
                when (operation) {
                    is RxOperation.LoadItems -> handleLoadItems()
                    is RxOperation.AddItem -> handleAddItem(operation.item)
                }
            }
            .addTo(compositeDisposable)
    }
}
```

**핵심 특징:**
- **스케줄러 관리**: IO, Main 스레드 간 적절한 스케줄링
- **연산자 활용**: throttleFirst, distinctUntilChanged 등 RxJava 연산자
- **메모리 관리**: CompositeDisposable을 통한 구독 해제
- **브리지**: RxJava Observable을 Kotlin Flow로 변환

#### Result003: Channel + Actor Model Pattern
```kotlin
// ActorModelViewModel.kt - Actor 기반 동시성 처리
class ActorModelViewModel : BaseViewModel() {
    
    // State Actor - 상태 관리 담당
    private val stateActor = actor<StateMessage>(capacity = Channel.UNLIMITED) {
        var state = ActorState()
        
        for (message in channel) {
            state = when (message) {
                is StateMessage.UpdateItems -> state.copy(items = message.items)
                is StateMessage.SetLoading -> state.copy(isLoading = message.loading)
                is StateMessage.SetError -> state.copy(error = message.error)
            }
            
            _uiState.value = state.toUiState()
        }
    }
    
    // Operation Actor - 비즈니스 로직 담당
    private val operationActor = actor<OperationMessage>(capacity = Channel.UNLIMITED) {
        for (message in channel) {
            when (message) {
                is OperationMessage.LoadItems -> {
                    stateActor.send(StateMessage.SetLoading(true))
                    
                    try {
                        delay(2000)
                        val items = generateInitialItems()
                        stateActor.send(StateMessage.UpdateItems(items))
                        stateActor.send(StateMessage.SetLoading(false))
                    } catch (e: Exception) {
                        stateActor.send(StateMessage.SetError(e.message))
                        stateActor.send(StateMessage.SetLoading(false))
                    }
                }
            }
        }
    }
    
    // UI로부터 오는 명령을 Operation Actor에게 전달
    fun loadItems() {
        operationActor.trySend(OperationMessage.LoadItems)
    }
}
```

**핵심 특징:**
- **Actor 모델**: 메시지 기반 동시성 처리
- **상태 안전성**: 각 Actor 내에서만 상태 변경
- **메시지 큐**: Channel을 통한 순서 보장
- **격리**: 각 Actor는 독립적으로 동작

#### Result004: Combined Flows Pattern
```kotlin
// CombineFlowsViewModel.kt - 여러 Flow 결합
class CombineFlowsViewModel : BaseViewModel() {
    
    private val itemsFlow = MutableStateFlow<List<Item>>(emptyList())
    private val loadingFlow = MutableStateFlow(false)
    private val errorFlow = MutableStateFlow<String?>(null)
    private val refreshingFlow = MutableStateFlow(false)
    private val operationCountFlow = MutableStateFlow(0)
    private val lastUpdateFlow = MutableStateFlow(0L)
    
    // 6개의 Flow를 결합하여 단일 UI 상태 생성
    val uiState: StateFlow<CombinedUiState> = combine(
        itemsFlow,
        loadingFlow,
        errorFlow,
        refreshingFlow,
        operationCountFlow,
        lastUpdateFlow
    ) { items, isLoading, error, isRefreshing, operationCount, lastUpdate ->
        CombinedUiState(
            items = items,
            isLoading = isLoading,
            error = error,
            isRefreshing = isRefreshing,
            operationCount = operationCount,
            lastUpdate = lastUpdate,
            isDataStale = isDataStale(lastUpdate)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CombinedUiState()
    )
    
    // 파생된 Flow들
    val itemCountFlow: StateFlow<Int> = itemsFlow
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)
    
    val hasErrorFlow: StateFlow<Boolean> = errorFlow
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)
    
    val recentItemsFlow: StateFlow<List<Item>> = itemsFlow
        .map { items -> 
            items.sortedByDescending { it.timestamp }.take(3)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
}
```

**핵심 특징:**
- **복합 상태**: 여러 독립적 상태를 결합하여 단일 UI 상태 생성
- **파생 상태**: 기본 상태로부터 계산된 파생 상태들
- **효율성**: 변경된 Flow만 재계산되는 효율적 업데이트
- **복잡성 관리**: 복잡한 상태 로직을 여러 작은 Flow로 분해

#### Result005: Hot/Cold Stream Pattern
```kotlin
// HotColdStreamViewModel.kt - Hot/Cold Stream 혼합 사용
class HotColdStreamViewModel : BaseViewModel() {
    
    // Hot Stream - 모든 구독자가 동일한 데이터 스트림 공유
    private val hotEvents = MutableSharedFlow<HotEvent>(
        replay = 1, // 마지막 1개 이벤트를 새 구독자에게 전달
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    
    // Cold Stream - 각 구독자마다 독립적인 스트림
    private fun createColdItemStream(): Flow<List<Item>> = flow {
        emit(emptyList()) // 초기값
        delay(2000)
        
        repeat(Int.MAX_VALUE) {
            delay(5000) // 5초마다 새로운 데이터
            val newItems = generateRandomItems()
            emit(newItems)
        }
    }.catch { 
        emit(emptyList()) 
    }
    
    // Hot + Cold Stream 결합
    val uiState: StateFlow<HotColdUiState> = combine(
        hotEvents.asStateFlow(), // Hot
        createColdItemStream(), // Cold
        temperatureFlow() // 실시간 온도 스트림 (Hot)
    ) { hotEvent, coldItems, temperature ->
        HotColdUiState(
            items = coldItems,
            lastHotEvent = hotEvent,
            temperature = temperature,
            timestamp = System.currentTimeMillis()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = HotColdUiState()
    )
    
    // 실시간 온도 스트림 (Hot)
    private fun temperatureFlow(): Flow<Float> = 
        (0..Int.MAX_VALUE).asFlow()
            .onEach { delay(1000) }
            .map { Random.nextFloat() * 30 + 10 } // 10-40도
            .shareIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                replay = 1
            )
}
```

**핵심 특징:**
- **Hot Stream**: 데이터 소스가 구독자와 독립적으로 동작
- **Cold Stream**: 구독 시점에 데이터 스트림 시작
- **혼합 사용**: Hot과 Cold Stream을 적절히 조합
- **실시간성**: Hot Stream을 통한 실시간 데이터 처리

### Agent04: Compose State 전문

#### Result001: remember + mutableStateOf Pattern
```kotlin
// Agent04Result001Screen.kt - 순수 Compose 상태 관리
@Composable
fun Screen() {
    var screenState by remember { mutableStateOf(ScreenUiState.Initializing) }
    var items by remember { mutableStateOf<List<Item>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    
    // 초기화 효과
    LaunchedEffect(Unit) {
        delay(2000)
        screenState = if (Random.nextDouble() < 0.2) {
            ScreenUiState.Failed("Initial load failed")
        } else {
            items = generateInitialItems()
            ScreenUiState.Succeed
        }
    }
    
    // 비즈니스 로직을 Composable 내에서 처리
    fun addItem() {
        val newItem = Item(
            id = "item_${items.size + 1}",
            title = "New Item ${items.size + 1}",
            description = "Added at ${System.currentTimeMillis()}"
        )
        items = items + newItem
    }
    
    fun removeItem(itemId: String) {
        items = items.filterNot { it.id == itemId }
    }
    
    fun refresh() {
        isRefreshing = true
        // 새로고침 로직...
        isRefreshing = false
    }
}
```

**핵심 특징:**
- **단순성**: ViewModel 없이 순수 Compose 상태 관리
- **직접성**: 상태 변경이 직접적이고 즉시 반영
- **생명주기**: remember는 리컴포지션 간 상태 보존, 구성 변경 시 초기화
- **제한사항**: 복잡한 비즈니스 로직에는 부적합

#### Result002: rememberSaveable + Parcelable Pattern
```kotlin
// ParcelableItem.kt - 상태 저장을 위한 Parcelable 구현
@Parcelize
data class ParcelableItem(
    val id: String,
    val title: String,
    val description: String,
    val timestamp: Long
) : Parcelable {
    fun toItem(): Item = Item(id, title, description, timestamp)
}

@Parcelize
data class ItemListState(
    val items: List<ParcelableItem>,
    val isLoading: Boolean,
    val error: String?
) : Parcelable

// Screen.kt - 구성 변경에서 살아남는 상태
@Composable
fun Screen() {
    var screenState by rememberSaveable { 
        mutableStateOf(ScreenUiState.Initializing) 
    }
    
    var itemListState by rememberSaveable {
        mutableStateOf(
            ItemListState(
                items = emptyList(),
                isLoading = false,
                error = null
            )
        )
    }
    
    // 상태 업데이트 함수들은 Parcelable 객체를 생성
    fun addItem() {
        val newParcelableItem = ParcelableItem(...)
        itemListState = itemListState.copy(
            items = itemListState.items + newParcelableItem
        )
    }
}
```

**핵심 특징:**
- **지속성**: 프로세스 종료와 재시작에서도 상태 보존
- **Parcelable**: Android의 직렬화 메커니즘 활용
- **자동 복원**: 시스템이 자동으로 상태 저장/복원
- **제약사항**: Parcelable 구현 필요, 복잡한 객체에는 부담

#### Result003: CompositionLocal Provider Pattern
```kotlin
// ItemManager.kt - 전역 상태 관리자
class ItemManager {
    private val _items = mutableStateListOf<Item>()
    val items: List<Item> = _items
    
    private val _isLoading = mutableStateOf(false)
    val isLoading: Boolean by _isLoading
    
    private val _error = mutableStateOf<String?>(null)
    val error: String? by _error
    
    fun addItem(item: Item) {
        _items.add(item)
    }
    
    fun removeItem(itemId: String) {
        _items.removeAll { it.id == itemId }
    }
    
    suspend fun loadItems() {
        _isLoading.value = true
        _error.value = null
        
        try {
            delay(2000)
            if (Random.nextDouble() < 0.2) {
                _error.value = "Failed to load items"
            } else {
                _items.clear()
                _items.addAll(generateInitialItems())
            }
        } finally {
            _isLoading.value = false
        }
    }
}

// CompositionLocal 정의
val LocalItemManager = compositionLocalOf<ItemManager> { error("No ItemManager provided") }

// Screen.kt - CompositionLocal 사용
@Composable
fun Screen() {
    val itemManager = LocalItemManager.current
    
    // ItemManager의 상태를 직접 참조
    val items = itemManager.items
    val isLoading = itemManager.isLoading
    val error = itemManager.error
    
    LaunchedEffect(Unit) {
        itemManager.loadItems()
    }
    
    // UI 구성...
}

// App.kt - Provider 설정
@Composable
fun App() {
    val itemManager = remember { ItemManager() }
    
    CompositionLocalProvider(LocalItemManager provides itemManager) {
        Screen()
    }
}
```

**핵심 특징:**
- **전역 접근**: 컴포지션 트리 어디서든 상태 접근 가능
- **의존성 주입**: Provider 패턴을 통한 의존성 제공
- **성능**: 변경된 부분만 리컴포지션
- **확장성**: 여러 레벨의 Provider 중첩 가능

#### Result004: State Hoisting Pattern
```kotlin
// Agent04Result004ViewModel.kt - 전통적 MVVM 패턴
class Agent04Result004ViewModel : BaseViewModel() {
    private val _uiState = MutableStateFlow(
        Agent04UiState(
            screenState = ScreenUiState.Initializing,
            items = emptyList(),
            isLoading = false,
            isRefreshing = false,
            error = null
        )
    )
    val uiState: StateFlow<Agent04UiState> = _uiState.asStateFlow()
    
    // 모든 상태 변경은 ViewModel에서 처리
    fun addItem(title: String, description: String) {
        val newItem = Item(
            id = "item_${_uiState.value.items.size + 1}",
            title = title,
            description = description
        )
        
        _uiState.update { currentState ->
            currentState.copy(items = currentState.items + newItem)
        }
    }
}

// Screen.kt - 완전히 상태가 없는 Composable
@Composable
fun Screen(
    viewModel: Agent04Result004ViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    ScreenContent(
        uiState = uiState,
        onAddItem = { title, description -> 
            viewModel.addItem(title, description) 
        },
        onRemoveItem = viewModel::removeItem,
        onRefresh = viewModel::refresh,
        onClearError = viewModel::clearError
    )
}

@Composable
private fun ScreenContent(
    uiState: Agent04UiState,
    onAddItem: (String, String) -> Unit,
    onRemoveItem: (String) -> Unit,
    onRefresh: () -> Unit,
    onClearError: () -> Unit
) {
    // 순수하게 UI만 렌더링, 상태는 매개변수로 받음
    // 모든 이벤트는 콜백으로 상위로 전달
}
```

**핵심 특징:**
- **명확한 분리**: 상태 관리와 UI 렌더링 완전 분리
- **테스트 용이성**: ViewModel과 Composable을 독립적으로 테스트
- **재사용성**: ScreenContent는 다른 상태 소스와도 사용 가능
- **예측 가능성**: 동일한 상태는 항상 동일한 UI 생성

#### Result005: Snapshot State System Pattern
```kotlin
// Agent04Result005Screen.kt - Snapshot 시스템 직접 활용
@Composable
fun Screen() {
    // SnapshotStateList로 효율적인 리스트 관리
    val items = remember { mutableStateListOf<Item>() }
    val isLoading = remember { mutableStateOf(false) }
    val error = remember { mutableStateOf<String?>(null) }
    
    // 배치 업데이트를 위한 Snapshot 활용
    fun addMultipleItems(newItems: List<Item>) {
        Snapshot.withMutableSnapshot {
            // 여러 상태 변경을 하나의 스냅샷으로 배치
            items.clear()
            items.addAll(newItems)
            isLoading.value = false
            error.value = null
        }
        // 배치 완료 후 한 번에 리컴포지션 발생
    }
    
    // 조건부 업데이트
    fun updateItemsIfNeeded() {
        Snapshot.withMutableSnapshot {
            val hasChanges = items.any { it.needsUpdate }
            
            if (hasChanges) {
                // 변경이 필요한 경우에만 업데이트
                items.replaceAll { item ->
                    if (item.needsUpdate) {
                        item.copy(lastUpdated = System.currentTimeMillis())
                    } else {
                        item
                    }
                }
            }
        }
    }
    
    // Snapshot 상태 모니터링
    LaunchedEffect(items.size) {
        // 아이템 수 변경 시 로그
        println("Items count changed: ${items.size}")
    }
    
    // Snapshot 기반 파생 상태
    val recentItems by remember {
        derivedStateOf {
            items.filter { 
                System.currentTimeMillis() - it.timestamp < 60000 
            }
        }
    }
}
```

**핵심 특징:**
- **배치 업데이트**: 여러 상태 변경을 하나의 리컴포지션으로 처리
- **효율성**: SnapshotStateList의 변경 추적 최적화
- **조건부 업데이트**: 필요한 경우에만 상태 변경
- **파생 상태**: derivedStateOf를 통한 계산된 상태

### Agent05: Hybrid Approach 전문

#### Result001: Repository + UseCase Pattern
```kotlin
// ItemRepository.kt - 데이터 접근 계층
interface ItemRepository {
    suspend fun getItems(): List<Item>
    suspend fun addItem(item: Item)
    suspend fun removeItem(itemId: String)
    suspend fun updateItem(item: Item)
}

class ItemRepositoryImpl : ItemRepository {
    private val items = mutableListOf<Item>()
    private val mutex = Mutex() // 동시성 안전성
    
    override suspend fun getItems(): List<Item> = mutex.withLock {
        delay(2000) // 네트워크 지연 시뮬레이션
        if (Random.nextDouble() < 0.2) {
            throw Exception("Network error")
        }
        items.toList() // 방어적 복사
    }
    
    override suspend fun addItem(item: Item) = mutex.withLock {
        delay(500)
        if (Random.nextDouble() < 0.1) {
            throw Exception("Failed to add item")
        }
        items.add(item)
    }
}

// UseCase들 - 단일 책임 원칙
class GetItemsUseCase(private val repository: ItemRepository) {
    suspend operator fun invoke(): List<Item> {
        return repository.getItems()
    }
}

class AddItemUseCase(private val repository: ItemRepository) {
    suspend operator fun invoke(title: String, description: String): Item {
        val item = Item(
            id = UUID.randomUUID().toString(),
            title = title,
            description = description,
            timestamp = System.currentTimeMillis()
        )
        repository.addItem(item)
        return item
    }
}

// ViewModel - UseCase 오케스트레이션
class Agent05Result001ViewModel(
    private val getItemsUseCase: GetItemsUseCase,
    private val addItemUseCase: AddItemUseCase,
    private val removeItemUseCase: RemoveItemUseCase,
    private val updateItemUseCase: UpdateItemUseCase,
    private val refreshItemsUseCase: RefreshItemsUseCase
) : BaseViewModel() {
    
    // ViewModel은 UseCase만 알고 Repository는 모름
    fun loadItems() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val items = getItemsUseCase()
                _uiState.update { 
                    it.copy(
                        items = items, 
                        isLoading = false,
                        screenState = ScreenUiState.Succeed
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = e.message,
                        screenState = ScreenUiState.Failed(e.message ?: "Unknown error")
                    ) 
                }
            }
        }
    }
}
```

**핵심 특징:**
- **계층 분리**: Repository, UseCase, ViewModel의 명확한 역할
- **단일 책임**: 각 UseCase는 하나의 비즈니스 작업만 수행
- **의존성 역전**: ViewModel이 구체적 구현이 아닌 추상화에 의존
- **테스트 용이성**: 각 계층을 독립적으로 테스트 가능

#### Result002: StateHolder + Delegate Pattern
```kotlin
// ItemStateHolder.kt - 상태 관리 책임 위임
interface ItemStateHolder {
    val items: StateFlow<List<Item>>
    val isLoading: StateFlow<Boolean>
    val error: StateFlow<String?>
    
    fun updateItems(items: List<Item>)
    fun setLoading(loading: Boolean)
    fun setError(error: String?)
}

class ItemStateHolderImpl : ItemStateHolder {
    private val _items = MutableStateFlow<List<Item>>(emptyList())
    override val items: StateFlow<List<Item>> = _items.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    override val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    override val error: StateFlow<String?> = _error.asStateFlow()
    
    override fun updateItems(items: List<Item>) {
        _items.value = items
    }
    
    override fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }
    
    override fun setError(error: String?) {
        _error.value = error
    }
}

// ItemOperationDelegate.kt - 비즈니스 로직 위임
interface ItemOperationDelegate {
    suspend fun loadItems(): List<Item>
    suspend fun addItem(title: String, description: String): Item
    suspend fun removeItem(itemId: String)
    suspend fun updateItem(item: Item)
    suspend fun refreshItems(): List<Item>
}

class ItemOperationDelegateImpl : ItemOperationDelegate {
    override suspend fun loadItems(): List<Item> {
        delay(2000)
        if (Random.nextDouble() < 0.2) {
            throw Exception("Load failed")
        }
        return generateInitialItems()
    }
    
    override suspend fun addItem(title: String, description: String): Item {
        delay(500)
        return Item(
            id = UUID.randomUUID().toString(),
            title = title,
            description = description
        )
    }
}

// ViewModel - StateHolder와 OperationDelegate 조합
class Agent05Result002ViewModel(
    private val stateHolder: ItemStateHolder,
    private val operationDelegate: ItemOperationDelegate
) : BaseViewModel() {
    
    val uiState: StateFlow<Agent05UiState> = combine(
        stateHolder.items,
        stateHolder.isLoading,
        stateHolder.error,
        screenStateFlow
    ) { items, isLoading, error, screenState ->
        Agent05UiState(
            screenState = screenState,
            items = items,
            isLoading = isLoading,
            error = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Agent05UiState()
    )
    
    fun addItem(title: String, description: String) {
        viewModelScope.launch {
            try {
                val newItem = operationDelegate.addItem(title, description)
                val currentItems = stateHolder.items.value
                stateHolder.updateItems(currentItems + newItem)
            } catch (e: Exception) {
                stateHolder.setError(e.message)
            }
        }
    }
}
```

**핵심 특징:**
- **책임 분산**: 상태 관리와 비즈니스 로직을 별도 위임체로 분리
- **조합**: ViewModel은 StateHolder와 OperationDelegate를 조합
- **테스트 용이성**: 각 위임체를 독립적으로 테스트
- **유연성**: StateHolder와 OperationDelegate의 다양한 구현 조합 가능

#### Result003: Singleton State Manager Pattern
```kotlin
// ItemStateManager.kt - 전역 상태 관리자
object ItemStateManager {
    private val _items = MutableStateFlow<List<Item>>(emptyList())
    val items: StateFlow<List<Item>> = _items.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _events = MutableSharedFlow<ItemEvent>()
    val events: SharedFlow<ItemEvent> = _events.asSharedFlow()
    
    private val coroutineScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main.immediate
    )
    
    sealed class ItemEvent {
        data class ItemAdded(val item: Item) : ItemEvent()
        data class ItemRemoved(val itemId: String) : ItemEvent()
        data class Error(val message: String) : ItemEvent()
    }
    
    fun addItem(title: String, description: String) {
        coroutineScope.launch {
            try {
                delay(500) // 시뮬레이션
                val newItem = Item(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    description = description
                )
                
                _items.update { currentItems -> currentItems + newItem }
                _events.emit(ItemEvent.ItemAdded(newItem))
                
            } catch (e: Exception) {
                _events.emit(ItemEvent.Error(e.message ?: "Failed to add item"))
            }
        }
    }
    
    fun initialize() {
        coroutineScope.launch {
            _isLoading.value = true
            delay(2000)
            
            try {
                val items = generateInitialItems()
                _items.value = items
            } catch (e: Exception) {
                _events.emit(ItemEvent.Error("Failed to initialize"))
            } finally {
                _isLoading.value = false
            }
        }
    }
}

// ViewModel - Singleton 관찰자
class Agent05Result003ViewModel : BaseViewModel() {
    val uiState: StateFlow<Agent05UiState> = combine(
        ItemStateManager.items,
        ItemStateManager.isLoading,
        screenStateFlow
    ) { items, isLoading, screenState ->
        Agent05UiState(
            screenState = screenState,
            items = items,
            isLoading = isLoading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Agent05UiState()
    )
    
    // 이벤트 수집
    init {
        viewModelScope.launch {
            ItemStateManager.events.collect { event ->
                when (event) {
                    is ItemStateManager.ItemEvent.ItemAdded -> {
                        // UI 피드백 처리
                    }
                    is ItemStateManager.ItemEvent.Error -> {
                        // 에러 처리
                    }
                }
            }
        }
    }
    
    // ViewModel은 단순히 Singleton에게 위임
    fun addItem(title: String, description: String) {
        ItemStateManager.addItem(title, description)
    }
}
```

**핵심 특징:**
- **전역 상태**: 애플리케이션 전체에서 공유되는 상태
- **이벤트 시스템**: SharedFlow를 통한 전역 이벤트 방출
- **생명주기 독립**: 컴포넌트 생명주기와 독립적인 상태 관리
- **메모리 주의**: Singleton의 메모리 누수 가능성

#### Result004: Event Bus + State Store Pattern
```kotlin
// EventBus.kt - 전역 이벤트 버스
object EventBus {
    private val _events = MutableSharedFlow<Event>(
        replay = 0,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<Event> = _events.asSharedFlow()
    
    fun emit(event: Event) {
        _events.tryEmit(event)
    }
    
    sealed class Event {
        data class LoadItemsRequested(val requestId: String) : Event()
        data class AddItemRequested(val title: String, val description: String) : Event()
        data class RemoveItemRequested(val itemId: String) : Event()
        data class ItemsLoaded(val items: List<Item>, val requestId: String) : Event()
        data class ItemAdded(val item: Item) : Event()
        data class ItemRemoved(val itemId: String) : Event()
        data class ErrorOccurred(val message: String, val requestId: String? = null) : Event()
    }
}

// ItemStateStore.kt - 이벤트 반응형 상태 저장소
class ItemStateStore {
    private val _uiState = MutableStateFlow(ItemStoreState())
    val uiState: StateFlow<ItemStoreState> = _uiState.asStateFlow()
    
    private val coroutineScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main.immediate
    )
    
    init {
        // 이벤트 버스 구독
        coroutineScope.launch {
            EventBus.events.collect { event ->
                handleEvent(event)
            }
        }
    }
    
    private suspend fun handleEvent(event: EventBus.Event) {
        when (event) {
            is EventBus.Event.LoadItemsRequested -> {
                _uiState.update { it.copy(isLoading = true) }
                
                try {
                    delay(2000)
                    val items = generateInitialItems()
                    EventBus.emit(EventBus.Event.ItemsLoaded(items, event.requestId))
                } catch (e: Exception) {
                    EventBus.emit(
                        EventBus.Event.ErrorOccurred(
                            e.message ?: "Load failed", 
                            event.requestId
                        )
                    )
                }
            }
            
            is EventBus.Event.ItemsLoaded -> {
                _uiState.update { 
                    it.copy(
                        items = event.items,
                        isLoading = false
                    ) 
                }
            }
            
            is EventBus.Event.ItemAdded -> {
                _uiState.update { 
                    it.copy(items = it.items + event.item) 
                }
            }
            
            is EventBus.Event.AddItemRequested -> {
                try {
                    delay(500)
                    val newItem = Item(
                        id = UUID.randomUUID().toString(),
                        title = event.title,
                        description = event.description
                    )
                    EventBus.emit(EventBus.Event.ItemAdded(newItem))
                } catch (e: Exception) {
                    EventBus.emit(EventBus.Event.ErrorOccurred(e.message ?: "Add failed"))
                }
            }
        }
    }
}

// ViewModel - 이벤트 발행자
class Agent05Result004ViewModel(
    private val stateStore: ItemStateStore
) : BaseViewModel() {
    
    val uiState: StateFlow<Agent05UiState> = combine(
        stateStore.uiState,
        screenStateFlow
    ) { storeState, screenState ->
        Agent05UiState(
            screenState = screenState,
            items = storeState.items,
            isLoading = storeState.isLoading,
            error = storeState.error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Agent05UiState()
    )
    
    fun addItem(title: String, description: String) {
        EventBus.emit(EventBus.Event.AddItemRequested(title, description))
    }
    
    fun loadItems() {
        EventBus.emit(EventBus.Event.LoadItemsRequested(UUID.randomUUID().toString()))
    }
}
```

**핵심 특징:**
- **완전한 분리**: 컴포넌트 간 직접적인 의존성 제거
- **이벤트 중심**: 모든 상호작용이 이벤트를 통해 이루어짐
- **확장성**: 새로운 컴포넌트가 쉽게 이벤트 버스에 참여
- **디버깅**: 모든 이벤트를 중앙에서 모니터링 가능

#### Result005: Factory Pattern + DI Pattern
```kotlin
// Dependencies.kt - 의존성 정의
interface ItemDataSource {
    suspend fun loadItems(): List<Item>
    suspend fun saveItem(item: Item)
    suspend fun deleteItem(itemId: String)
}

class LocalItemDataSource : ItemDataSource {
    private val items = mutableListOf<Item>()
    
    override suspend fun loadItems(): List<Item> {
        delay(1000)
        return items.toList()
    }
    
    override suspend fun saveItem(item: Item) {
        delay(300)
        items.removeAll { it.id == item.id }
        items.add(item)
    }
}

class RemoteItemDataSource : ItemDataSource {
    override suspend fun loadItems(): List<Item> {
        delay(3000) // 더 긴 네트워크 지연
        if (Random.nextDouble() < 0.3) {
            throw Exception("Network error")
        }
        return generateInitialItems()
    }
}

interface ItemRepository {
    suspend fun getItems(): List<Item>
    suspend fun addItem(item: Item)
    suspend fun removeItem(itemId: String)
}

class ItemRepositoryImpl(
    private val localDataSource: ItemDataSource,
    private val remoteDataSource: ItemDataSource
) : ItemRepository {
    
    override suspend fun getItems(): List<Item> {
        return try {
            // 원격에서 먼저 시도
            val remoteItems = remoteDataSource.loadItems()
            // 로컬에 캐시
            remoteItems.forEach { localDataSource.saveItem(it) }
            remoteItems
        } catch (e: Exception) {
            // 실패 시 로컬 폴백
            localDataSource.loadItems()
        }
    }
}

interface ItemService {
    suspend fun refreshItems(): List<Item>
    suspend fun createItem(title: String, description: String): Item
}

class ItemServiceImpl(private val repository: ItemRepository) : ItemService {
    override suspend fun createItem(title: String, description: String): Item {
        val item = Item(
            id = UUID.randomUUID().toString(),
            title = title,
            description = description,
            timestamp = System.currentTimeMillis()
        )
        repository.addItem(item)
        return item
    }
}

// DependencyFactory.kt - 팩토리 패턴 구현
interface DependencyFactory {
    fun createItemService(): ItemService
    fun createItemRepository(): ItemRepository
    fun createLocalDataSource(): ItemDataSource
    fun createRemoteDataSource(): ItemDataSource
}

class ProdDependencyFactory : DependencyFactory {
    override fun createLocalDataSource(): ItemDataSource = LocalItemDataSource()
    override fun createRemoteDataSource(): ItemDataSource = RemoteItemDataSource()
    
    override fun createItemRepository(): ItemRepository = ItemRepositoryImpl(
        localDataSource = createLocalDataSource(),
        remoteDataSource = createRemoteDataSource()
    )
    
    override fun createItemService(): ItemService = ItemServiceImpl(
        repository = createItemRepository()
    )
}

class TestDependencyFactory : DependencyFactory {
    override fun createLocalDataSource(): ItemDataSource = MockItemDataSource()
    override fun createRemoteDataSource(): ItemDataSource = MockItemDataSource()
    // ... 테스트용 구현들
}

// DI 컨테이너
object DiContainer {
    private var factory: DependencyFactory = ProdDependencyFactory()
    
    fun setFactory(factory: DependencyFactory) {
        this.factory = factory
    }
    
    // Lazy 초기화로 성능 최적화
    val itemService: ItemService by lazy { factory.createItemService() }
    val itemRepository: ItemRepository by lazy { factory.createItemRepository() }
}

// ViewModel - DI 컨테이너 활용
class Agent05Result005ViewModel(
    private val itemService: ItemService = DiContainer.itemService
) : BaseViewModel() {
    
    fun addItem(title: String, description: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val newItem = itemService.createItem(title, description)
                _uiState.update { currentState ->
                    currentState.copy(
                        items = currentState.items + newItem,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message
                    ) 
                }
            }
        }
    }
}
```

**핵심 특징:**
- **추상 팩토리**: 관련된 객체들의 집합을 생성
- **의존성 주입**: 컴파일 타임이 아닌 런타임에 의존성 제공
- **구성 가능**: 다른 환경(개발, 테스트, 프로덕션)에 따른 다른 구현 제공
- **확장 용이**: 새로운 구현체나 의존성 추가가 쉬움

## 📊 비교 분석 매트릭스

### 복잡성 vs 유연성
| Pattern | 복잡성 | 유연성 | 학습 곡선 | 테스트 용이성 |
|---------|--------|--------|-----------|---------------|
| remember + mutableStateOf | ⭐ | ⭐⭐ | ⭐ | ⭐⭐ |
| StateFlow + Sealed | ⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐ |
| MVI | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| Clean Architecture | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| Actor Model | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ |
| Event Bus | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ |

### 성능 특성
| Pattern | 메모리 사용량 | CPU 효율성 | 리컴포지션 효율성 | 동시성 처리 |
|---------|---------------|------------|-------------------|-------------|
| remember + mutableStateOf | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐ |
| StateFlow | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| SharedFlow + Event | ⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| Actor Model | ⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| Singleton Manager | ⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ |

## 🎯 실무 적용 가이드

### 프로젝트 규모별 추천
#### 소규모 프로젝트 (1-2명, 3개월 이내)
- **추천**: remember + mutableStateOf, MutableState + data class
- **이유**: 빠른 개발, 낮은 복잡성, 학습 비용 최소

#### 중간 규모 프로젝트 (3-5명, 6개월-1년)
- **추천**: StateFlow + Sealed Class, MVI, State Hoisting
- **이유**: 적당한 복잡성, 좋은 테스트 용이성, 확장 가능

#### 대규모 프로젝트 (5명 이상, 1년 이상)
- **추천**: Clean Architecture, Redux-like, Factory + DI
- **이유**: 높은 유지보수성, 팀 협업 용이, 확장성

### 도메인별 특성

#### 실시간 데이터가 중요한 앱
- **추천**: Hot/Cold Stream, SharedFlow + Event, Actor Model
- **예시**: 채팅 앱, 주식 거래 앱, 게임

#### 복잡한 비즈니스 로직이 있는 앱
- **추천**: Clean Architecture, Repository + UseCase, MVI
- **예시**: 뱅킹 앱, ERP 시스템, 전자상거래

#### 단순한 CRUD 앱
- **추천**: StateFlow + Sealed, State Hoisting, LiveData + Transformation
- **예시**: 할 일 관리, 메모 앱, 설정 화면

### 마이그레이션 전략

#### 기존 앱에서 Compose 도입
1. **1단계**: LiveData + Transformation 활용하여 기존 MVVM 패턴 유지
2. **2단계**: StateFlow로 점진적 마이그레이션
3. **3단계**: Compose 전용 패턴 도입

#### 레거시 코드베이스 현대화
1. **Repository 패턴 도입**: 데이터 계층 추상화
2. **UseCase 계층 추가**: 비즈니스 로직 분리
3. **DI 시스템 도입**: 의존성 관리 개선

## 🔍 학습 로드맵

### 초급 개발자
1. remember + mutableStateOf로 기본 개념 학습
2. StateFlow + Sealed Class로 반응형 프로그래밍 이해
3. State Hoisting으로 상태 관리 원칙 학습

### 중급 개발자
1. MVI 패턴으로 아키텍처 설계 학습
2. Flow Chain으로 복합 데이터 처리 학습
3. Clean Architecture로 계층 분리 이해

### 고급 개발자
1. Actor Model로 고급 동시성 처리 학습
2. Event Bus로 대규모 시스템 설계 학습
3. Factory + DI로 복잡한 의존성 관리 학습

---

이 문서는 25가지 서로 다른 상태 관리 패턴을 실제 코드와 함께 분석한 포괄적인 가이드입니다. 각 패턴은 특정 상황과 요구사항에 최적화되어 있으며, 실무에서 적절한 선택을 위한 기준을 제공합니다.

# 📢 **추가 구현체: init{} 블록 없는 패턴들**

## 🎯 init{} 블록 없는 10개 추가 구현체

기존 25개 구현체에 추가로 **각 Agent별 2개씩 총 10개**의 init{} 블록을 사용하지 않는 구현체를 생성했습니다. 총 **35개 구현체**가 완성되었습니다.

### Agent01: State Management 전문 (+2개)

#### **Result006: Lazy Initialization Pattern**
```kotlin
// LazyInitViewModel.kt - onStart에서 첫 구독 시 초기화
class LazyInitViewModel : BaseViewModel() {
    val uiState: StateFlow<LazyInitUiState> = _uiState.asStateFlow()
        .onStart { 
            if (!_uiState.value.isInitialized) {
                initializeIfNeeded()
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily, // Lazy 시작
            initialValue = LazyInitUiState()
        )
    
    fun manualInitialize() {
        viewModelScope.launch {
            initializeIfNeeded()
        }
    }
}
```

**핵심 특징:**
- **지연 초기화**: 첫 구독 시점에 onStart로 초기화
- **수동 제어**: manualInitialize() 함수로 명시적 초기화
- **Lazy SharedIn**: SharingStarted.Lazily로 필요시에만 시작
- **상태 추적**: isInitialized 플래그로 중복 초기화 방지

#### **Result007: Factory Method Pattern**
```kotlin
// FactoryViewModel.kt - 팩토리 메서드로 상태 생성
class FactoryViewModel : BaseViewModel() {
    private fun createStateBasedOnCondition(
        items: List<Item>? = null,
        error: String? = null,
        isLoading: Boolean = false
    ): FactoryUiState {
        return when {
            error != null -> FactoryUiState.createErrorState(error)
            isLoading -> FactoryUiState.createLoadingState()
            items != null -> FactoryUiState.createSuccessState(items)
            else -> FactoryUiState.createInitialState()
        }
    }
    
    fun initialize() { // 수동 초기화
        viewModelScope.launch {
            _uiState.value = FactoryUiState.createLoadingState()
            // 초기화 로직...
        }
    }
}
```

**핵심 특징:**
- **팩토리 패턴**: 조건에 따른 상태 팩토리 메서드
- **전략적 생성**: createErrorState, createSuccessState 등 상황별 생성
- **수동 트리거**: initialize() 함수로 명시적 시작
- **유연한 생성**: ItemCreationStrategy enum으로 아이템 생성 전략

### Agent02: Architecture Pattern 전문 (+2개)

#### **Result006: Command Pattern**
```kotlin
// CommandInvoker.kt - 명령 큐와 실행
class CommandInvoker {
    private val commandQueue = mutableListOf<Command>()
    private val undoStack = mutableListOf<Command>()
    
    fun executeCommand(command: Command) {
        commandQueue.add(command)
        command.execute()
        undoStack.add(command)
    }
    
    fun processQueue() { // 수동 큐 처리
        commandQueue.forEach { it.execute() }
        commandQueue.clear()
    }
}
```

**핵심 특징:**
- **명령 큐잉**: 명령을 큐에 저장 후 수동 실행
- **Undo 지원**: 실행된 명령의 취소 기능
- **수동 처리**: processQueue()로 명시적 큐 처리
- **배치 실행**: 여러 명령을 한 번에 처리

#### **Result007: Strategy Pattern**
```kotlin
// DataStrategy.kt - 런타임 전략 교체
interface DataStrategy {
    suspend fun loadItems(): List<Item>
    val name: String
    val description: String
}

class NetworkStrategy : DataStrategy {
    override suspend fun loadItems(): List<Item> {
        delay(3000) // 느린 네트워크
        return generateNetworkItems()
    }
}

class CacheStrategy : DataStrategy {
    override suspend fun loadItems(): List<Item> {
        delay(100) // 빠른 캐시
        return generateCachedItems()
    }
}
```

**핵심 특징:**
- **전략 교체**: 런타임에 데이터 로딩 전략 변경
- **성능 특성**: Fast, Detailed, Network, Cache 등 다양한 전략
- **수동 전환**: switchStrategy()로 명시적 전략 변경
- **설정 가능**: UI에서 직접 전략 선택

### Agent03: Reactive Programming 전문 (+2개)

#### **Result006: Debounce/Throttle Pattern**
```kotlin
// DebounceViewModel.kt - 연산자 기반 초기화
class DebounceViewModel : BaseViewModel() {
    private val searchTrigger = MutableSharedFlow<String>()
    private val refreshTrigger = MutableSharedFlow<Unit>()
    
    val searchResults = searchTrigger
        .debounce(500) // 500ms 디바운스
        .map { query -> performSearch(query) }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    val refreshResults = refreshTrigger
        .sample(2000) // 2초 샘플링
        .onEach { performRefresh() }
    
    // 수동 트리거
    fun triggerSearch(query: String) {
        searchTrigger.tryEmit(query)
    }
}
```

**핵심 특징:**
- **Debounce 검색**: 500ms 지연 후 검색 실행
- **Throttle 새로고침**: 2초 간격으로 새로고침 제한
- **수동 트리거**: triggerSearch(), triggerRefresh() 함수
- **배치 연산**: batchOperations로 여러 작업 묶음 처리

#### **Result007: Switchable Source Pattern**
```kotlin
// DataSource.kt - 동적 소스 전환
interface DataSource {
    val name: String
    fun itemsFlow(): Flow<List<Item>>
}

class NetworkDataSource : DataSource {
    override fun itemsFlow(): Flow<List<Item>> = flow {
        while (true) {
            emit(fetchFromNetwork())
            delay(5000)
        }
    }
}

class WebSocketDataSource : DataSource {
    override fun itemsFlow(): Flow<List<Item>> = callbackFlow {
        // WebSocket 연결 시뮬레이션
        connectWebSocket { items -> trySend(items) }
    }
}
```

**핵심 특징:**
- **동적 전환**: 런타임에 데이터 소스 교체
- **다양한 소스**: Cache, Network, Database, WebSocket, Hybrid
- **수동 전환**: switchToSource()로 명시적 소스 변경
- **실시간성**: WebSocket 시뮬레이션으로 실시간 데이터

### Agent04: Compose State 전문 (+2개)

#### **Result006: LaunchedEffect Pattern**
```kotlin
// LaunchedEffectViewModel.kt - Composable 중심 초기화
class LaunchedEffectViewModel : BaseViewModel() {
    fun initialize(trigger: String) { // 파라미터 기반 초기화
        viewModelScope.launch {
            // 초기화 로직
        }
    }
}

@Composable
fun Screen() {
    val viewModel: LaunchedEffectViewModel = viewModel()
    
    // LaunchedEffect로 초기화
    LaunchedEffect(Unit) {
        viewModel.initialize("default")
    }
    
    // 조건부 초기화
    LaunchedEffect(someCondition) {
        if (shouldReinitialize) {
            viewModel.initialize("conditional")
        }
    }
}
```

**핵심 특징:**
- **Composable 중심**: LaunchedEffect에서 초기화 제어
- **키 기반 트리거**: LaunchedEffect 키로 재초기화 조건 설정
- **파라미터 초기화**: initialize(trigger) 함수로 다양한 초기화
- **조건부 실행**: 상태에 따른 조건부 초기화

#### **Result007: produceState Pattern**
```kotlin
// ProduceStateViewModel.kt - produceState로 상태 생성
@Composable
fun Screen() {
    val config by remember { mutableStateOf(StateConfig()) }
    
    val itemsState by produceState<List<Item>>(
        initialValue = emptyList(),
        key1 = config.source,
        key2 = config.refreshInterval
    ) {
        while (true) {
            value = loadItemsFromSource(config.source)
            delay(config.refreshInterval)
        }
    }
    
    val statusState by produceState<String>(
        initialValue = "Initializing",
        key1 = itemsState.size
    ) {
        value = when {
            itemsState.isEmpty() -> "No items"
            itemsState.size < 5 -> "Few items"
            else -> "Many items"
        }
    }
}
```

**핵심 특징:**
- **Native State**: produceState로 Compose 네이티브 상태 생성
- **키 반응성**: key 변경 시 자동 재생성
- **여러 상태**: 독립적인 여러 produceState 조합
- **설정 기반**: StateConfig로 동적 설정 변경

### Agent05: Hybrid Approach 전문 (+2개)

#### **Result006: Lazy Repository Pattern**
```kotlin
// Repository.kt - 지연 초기화 Repository
class LazyItemRepository : ItemRepository {
    private var isInitialized = false
    
    override suspend fun getItems(): List<Item> {
        if (!isInitialized) {
            initialize() // 첫 호출 시 초기화
            isInitialized = true
        }
        return performGetItems()
    }
    
    private suspend fun initialize() {
        // 실제 초기화 로직
        setupDatabase()
        preloadCache()
    }
}

class RepositorySwitcher {
    fun createRepository(type: RepositoryType): ItemRepository {
        return when (type) {
            RepositoryType.LAZY -> LazyItemRepository()
            RepositoryType.CACHE -> CacheBackedRepository()
            RepositoryType.DATABASE -> DatabaseRepository()
        }
    }
}
```

**핵심 특징:**
- **지연 생성**: 첫 사용 시점에 Repository 초기화
- **동적 전환**: 런타임에 Repository 타입 변경
- **성능 최적화**: 필요한 시점까지 리소스 사용 지연
- **타입별 구현**: Lazy, Cache, Database 등 다양한 Repository

#### **Result007: Provider Pattern**
```kotlin
// Provider.kt - 의존성 제공자 패턴
class DependencyProvider {
    private val services = mutableMapOf<String, Any>()
    
    fun <T> provide(key: String, factory: () -> T): T {
        return services.getOrPut(key) { factory() } as T
    }
    
    fun configure(preset: ProviderPreset) { // 수동 설정
        when (preset) {
            ProviderPreset.DEVELOPMENT -> setupDevelopment()
            ProviderPreset.PRODUCTION -> setupProduction()
            ProviderPreset.TESTING -> setupTesting()
        }
    }
}

class ProviderViewModel : BaseViewModel() {
    private val provider = DependencyProvider()
    
    fun initializeWithPreset(preset: ProviderPreset) { // 수동 초기화
        provider.configure(preset)
        loadDependencies()
    }
}
```

**핵심 특징:**
- **제공자 패턴**: 필요한 의존성을 동적으로 제공
- **프리셋 지원**: Development, Production, Testing 환경별 설정
- **수동 설정**: configure()로 명시적 의존성 설정
- **지연 생성**: 실제 사용 시점에 서비스 생성

## 🔄 init{} 없는 패턴의 장점

### 1. **명시적 제어**
- 초기화 시점을 개발자가 직접 제어
- 조건부 초기화 및 재초기화 가능
- 테스트에서 초기화 과정 mock 용이

### 2. **지연 초기화**
- 필요한 시점까지 리소스 사용 지연
- 앱 시작 시간 단축
- 메모리 사용량 최적화

### 3. **유연한 구성**
- 런타임에 초기화 전략 변경
- 다양한 초기화 파라미터 지원
- 동적 설정 및 환경별 초기화

### 4. **테스트 용이성**
- 초기화 로직을 외부에서 제어
- 단위 테스트에서 초기화 과정 검증
- Mock 및 Fake 구현 용이

## 📊 총 구현체 현황

| Agent | 기존 구현체 | init{} 없는 추가 | 총 구현체 |
|-------|-------------|------------------|-----------|
| Agent01 | 5개 | 2개 | 7개 |
| Agent02 | 5개 | 2개 | 7개 |
| Agent03 | 5개 | 2개 | 7개 |
| Agent04 | 5개 | 2개 | 7개 |
| Agent05 | 5개 | 2개 | 7개 |
| **총계** | **25개** | **10개** | **35개** |

## 🎯 init{} 없는 패턴 사용 가이드

### 언제 사용하면 좋을까?

#### ✅ **사용 권장 상황**
- **테스트 중심 개발**: 초기화 과정을 세밀하게 제어해야 할 때
- **조건부 초기화**: 사용자 입력이나 외부 조건에 따라 초기화할 때
- **성능 최적화**: 앱 시작 시간을 줄이고 싶을 때
- **동적 설정**: 런타임에 초기화 전략을 바꿔야 할 때

#### ⚠️ **주의 사항**
- 초기화를 깜빡할 가능성
- 코드 복잡성 증가
- 개발자의 명시적 관리 필요

### 추천 선택 가이드

| 상황 | 추천 패턴 | 이유 |
|------|-----------|------|
| 단순한 지연 로딩 | Lazy Initialization | 가장 간단하고 직관적 |
| 복잡한 생성 로직 | Factory Method | 조건에 따른 유연한 생성 |
| 명령 기록/취소 | Command Pattern | 작업 이력 관리 용이 |
| 성능 최적화 | Strategy Pattern | 상황별 최적 전략 선택 |
| 실시간 검색 | Debounce/Throttle | 불필요한 API 호출 방지 |
| Compose 중심 | LaunchedEffect | Compose 생명주기와 연동 |
| 설정 기반 앱 | Provider Pattern | 환경별 의존성 관리 |

---

**35개의 다양한 패턴**으로 Android Compose에서 가능한 거의 모든 상태 관리 접근법을 다뤘습니다. 각각의 장단점을 이해하고 프로젝트 상황에 맞는 최적의 패턴을 선택하세요!

더 자세한 분석이나 특정 패턴에 대한 추가 설명이 필요하시면 언제든 말씀해 주세요!