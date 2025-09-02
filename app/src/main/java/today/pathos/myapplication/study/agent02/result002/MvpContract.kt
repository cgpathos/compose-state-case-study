package today.pathos.myapplication.study.agent02.result002

import today.pathos.myapplication.study.common.Item
import today.pathos.myapplication.study.common.ScreenUiState

interface MvpContract {
    
    interface View {
        fun showScreenState(state: ScreenUiState)
        fun showItems(items: List<Item>)
        fun showLoading(isLoading: Boolean)
        fun showRefreshing(isRefreshing: Boolean)
        fun showError(error: String?)
        fun clearError()
    }
    
    interface Presenter {
        fun attachView(view: View)
        fun detachView()
        fun loadItems()
        fun refreshItems()
        fun addItem(item: Item)
        fun removeItem(itemId: String)
        fun updateItem(item: Item)
    }
}