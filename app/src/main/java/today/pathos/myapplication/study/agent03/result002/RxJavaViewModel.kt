package today.pathos.myapplication.study.agent03.result002

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import today.pathos.myapplication.study.common.BaseViewModel
import today.pathos.myapplication.study.common.Item
import today.pathos.myapplication.study.common.ScreenUiState
import java.util.concurrent.TimeUnit

class RxJavaViewModel : BaseViewModel() {
    
    private val compositeDisposable = CompositeDisposable()
    
    // Initial screen state (only used for initialization)
    var screenUiState by mutableStateOf<ScreenUiState>(ScreenUiState.Initializing)
        private set
    
    // RxJava Subjects for reactive state management
    private val itemsSubject = BehaviorSubject.createDefault<List<Item>>(emptyList())
    private val loadingSubject = BehaviorSubject.createDefault(false)
    private val errorSubject = BehaviorSubject.create<String?>()
    
    // Operation triggers
    private val refreshTrigger = PublishSubject.create<Unit>()
    private val addItemTrigger = PublishSubject.create<Unit>()
    private val removeItemTrigger = PublishSubject.create<String>()
    private val updateItemTrigger = PublishSubject.create<Item>()
    
    // Flow adapters for Compose
    private val _itemsFlow = MutableStateFlow<List<Item>>(emptyList())
    val itemsFlow: StateFlow<List<Item>> = _itemsFlow.asStateFlow()
    
    private val _isLoadingFlow = MutableStateFlow(false)
    val isLoadingFlow: StateFlow<Boolean> = _isLoadingFlow.asStateFlow()
    
    private val _errorFlow = MutableStateFlow<String?>(null)
    val errorFlow: StateFlow<String?> = _errorFlow.asStateFlow()
    
    init {
        setupRxStreams()
        performInitialLoad()
    }
    
    private fun setupRxStreams() {
        // Items stream
        compositeDisposable.add(
            itemsSubject
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { items ->
                    _itemsFlow.value = items
                }
        )
        
        // Loading stream
        compositeDisposable.add(
            loadingSubject
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { isLoading ->
                    _isLoadingFlow.value = isLoading
                }
        )
        
        // Error stream
        compositeDisposable.add(
            errorSubject
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { error ->
                    _errorFlow.value = error
                }
        )
        
        // Initial load stream
        compositeDisposable.add(
            Observable.timer(2, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { 
                    loadingSubject.onNext(true)
                    errorSubject.onNext(null)
                }
                .flatMap {
                    if (shouldSimulateError()) {
                        Observable.error<List<Item>>(Exception("Initial load failed"))
                    } else {
                        Observable.just(generateInitialItems())
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { items ->
                        itemsSubject.onNext(items)
                        loadingSubject.onNext(false)
                        updateScreenStateAfterInitialLoad(null)
                    },
                    { error ->
                        errorSubject.onNext(error.message)
                        loadingSubject.onNext(false)
                        updateScreenStateAfterInitialLoad(error.message)
                    }
                )
        )
        
        // Refresh stream
        compositeDisposable.add(
            refreshTrigger
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .doOnNext { 
                    loadingSubject.onNext(true)
                    errorSubject.onNext(null)
                }
                .delay(1, TimeUnit.SECONDS)
                .flatMap {
                    if (shouldSimulateError()) {
                        Observable.error<List<Item>>(Exception("Refresh failed"))
                    } else {
                        Observable.just(generateInitialItems())
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { items ->
                        itemsSubject.onNext(items)
                        loadingSubject.onNext(false)
                    },
                    { error ->
                        errorSubject.onNext(error.message)
                        loadingSubject.onNext(false)
                    }
                )
        )
        
        // Add item stream
        compositeDisposable.add(
            addItemTrigger
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .doOnNext { 
                    loadingSubject.onNext(true)
                    errorSubject.onNext(null)
                }
                .flatMap {
                    if (shouldSimulateError()) {
                        Observable.error<List<Item>>(Exception("Add operation failed"))
                    } else {
                        val currentItems = itemsSubject.value ?: emptyList()
                        val newItem = generateNewItem(currentItems.size)
                        Observable.just(currentItems + newItem)
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { items ->
                        itemsSubject.onNext(items)
                        loadingSubject.onNext(false)
                    },
                    { error ->
                        errorSubject.onNext(error.message)
                        loadingSubject.onNext(false)
                    }
                )
        )
        
        // Remove item stream
        compositeDisposable.add(
            removeItemTrigger
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .doOnNext { 
                    loadingSubject.onNext(true)
                    errorSubject.onNext(null)
                }
                .flatMap { itemId ->
                    if (shouldSimulateError()) {
                        Observable.error<List<Item>>(Exception("Remove operation failed"))
                    } else {
                        val currentItems = itemsSubject.value ?: emptyList()
                        Observable.just(currentItems.filter { it.id != itemId })
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { items ->
                        itemsSubject.onNext(items)
                        loadingSubject.onNext(false)
                    },
                    { error ->
                        errorSubject.onNext(error.message)
                        loadingSubject.onNext(false)
                    }
                )
        )
        
        // Update item stream
        compositeDisposable.add(
            updateItemTrigger
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .doOnNext { 
                    loadingSubject.onNext(true)
                    errorSubject.onNext(null)
                }
                .flatMap { itemToUpdate ->
                    if (shouldSimulateError()) {
                        Observable.error<List<Item>>(Exception("Update operation failed"))
                    } else {
                        val currentItems = itemsSubject.value ?: emptyList()
                        val updatedItem = itemToUpdate.copy(
                            title = "${itemToUpdate.title} (Updated)",
                            timestamp = System.currentTimeMillis()
                        )
                        val updatedList = currentItems.map { item ->
                            if (item.id == updatedItem.id) updatedItem else item
                        }
                        Observable.just(updatedList)
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { items ->
                        itemsSubject.onNext(items)
                        loadingSubject.onNext(false)
                    },
                    { error ->
                        errorSubject.onNext(error.message)
                        loadingSubject.onNext(false)
                    }
                )
        )
    }
    
    private fun performInitialLoad() {
        // Initial load is handled by the timer observable in setupRxStreams()
    }
    
    private fun updateScreenStateAfterInitialLoad(errorMessage: String?) {
        viewModelScope.launch {
            screenUiState = if (errorMessage != null) {
                ScreenUiState.Failed(errorMessage)
            } else {
                ScreenUiState.Succeed
            }
        }
    }
    
    fun refresh() {
        refreshTrigger.onNext(Unit)
    }
    
    fun addItem() {
        addItemTrigger.onNext(Unit)
    }
    
    fun removeItem(itemId: String) {
        removeItemTrigger.onNext(itemId)
    }
    
    fun updateItem(item: Item) {
        updateItemTrigger.onNext(item)
    }
    
    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}