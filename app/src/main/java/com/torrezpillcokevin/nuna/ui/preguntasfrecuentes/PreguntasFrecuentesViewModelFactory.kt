package com.torrezpillcokevin.nuna.ui.preguntasfrecuentes

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.torrezpillcokevin.nuna.data.ApiService

class PreguntasFrecuentesViewModelFactory(
    private val application: Application,
    private val apiService: ApiService
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PreguntasFrecuentesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PreguntasFrecuentesViewModel(application, apiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
