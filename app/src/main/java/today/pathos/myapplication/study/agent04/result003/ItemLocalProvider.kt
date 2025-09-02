package today.pathos.myapplication.study.agent04.result003

import androidx.compose.runtime.compositionLocalOf

// CompositionLocal for providing ItemManager throughout the composition tree
val LocalItemManager = compositionLocalOf<ItemManager> {
    error("ItemManager not provided")
}