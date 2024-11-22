package com.torrezpillcokevin.nuna.ui.asistente

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Asistente "
    }
    val text: LiveData<String> = _text
}