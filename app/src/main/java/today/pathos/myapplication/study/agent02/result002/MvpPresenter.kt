package today.pathos.myapplication.study.agent02.result002

import kotlinx.coroutines.*
import today.pathos.myapplication.study.common.BaseViewModel
import today.pathos.myapplication.study.common.Item
import today.pathos.myapplication.study.common.ScreenUiState

class MvpPresenter : BaseViewModel(), MvpContract.Presenter {
    
    private var view: MvpContract.View? = null
    private val presenterScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var items = mutableListOf<Item>()
    
    override fun attachView(view: MvpContract.View) {
        this.view = view
        loadItems()
    }
    
    override fun detachView() {
        this.view = null
        presenterScope.cancel()
    }
    
    override fun loadItems() {
        presenterScope.launch {
            view?.showLoading(true)
            view?.clearError()
            
            try {
                delay(2000) // Simulate network delay
                
                if (shouldSimulateError()) {
                    throw Exception("Network error occurred")
                }
                
                items.clear()
                items.addAll(generateInitialItems())
                
                view?.showItems(items)
                view?.showLoading(false)
                view?.showScreenState(ScreenUiState.Succeed)
                
            } catch (e: Exception) {
                view?.showLoading(false)
                view?.showError(e.message)
                view?.showScreenState(ScreenUiState.Failed(e.message ?: "Unknown error"))
            }
        }
    }
    
    override fun refreshItems() {
        presenterScope.launch {
            view?.showRefreshing(true)
            view?.clearError()
            
            try {
                delay(1000) // Simulate refresh delay
                
                if (shouldSimulateError()) {
                    throw Exception("Refresh failed")
                }
                
                items.clear()
                items.addAll(generateInitialItems())
                
                view?.showItems(items)
                view?.showRefreshing(false)
                
            } catch (e: Exception) {
                view?.showRefreshing(false)
                view?.showError(e.message)
            }
        }
    }
    
    override fun addItem(item: Item) {
        items.add(item)
        view?.showItems(items.toList())
    }
    
    override fun removeItem(itemId: String) {
        items.removeAll { it.id == itemId }
        view?.showItems(items.toList())
    }
    
    override fun updateItem(item: Item) {
        val index = items.indexOfFirst { it.id == item.id }
        if (index != -1) {
            items[index] = item
            view?.showItems(items.toList())
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        detachView()
    }
}