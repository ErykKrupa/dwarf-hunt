package com.example.krasnalhunt.model

import androidx.lifecycle.ViewModel

class DwarfViewModel : ViewModel() {
    var dwarfItem: DwarfItem? = null
    var distance: Int = 0

    val isCatchable get() = distance < 50
}