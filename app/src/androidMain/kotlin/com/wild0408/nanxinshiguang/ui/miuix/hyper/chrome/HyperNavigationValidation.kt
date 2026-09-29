package com.wild0408.nanxinshiguang.ui.miuix.hyper.chrome

internal fun validateHyperNavigation(keys: List<String>, selectedIndex: Int) {
    require(keys.size in 2..4) { "Hyper navigation requires between 2 and 4 items" }
    require(keys.distinct().size == keys.size) { "Hyper navigation item keys must be unique" }
    require(selectedIndex in keys.indices) { "selectedIndex must point to an existing item" }
}
