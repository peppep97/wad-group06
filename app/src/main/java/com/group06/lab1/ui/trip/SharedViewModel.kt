package com.group06.lab1.ui.trip

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    val data = MutableLiveData<Int>()

    fun data(item: Int) {
        data.value = item
    }
}