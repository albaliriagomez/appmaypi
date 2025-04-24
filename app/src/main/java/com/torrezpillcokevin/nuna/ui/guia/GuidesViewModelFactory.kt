package com.torrezpillcokevin.nuna.ui.guia

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.torrezpillcokevin.nuna.data.ApiService

class GuidesViewModelFactory(
    private val application: Application,
    private val apiService: ApiService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GuiaViewModel::class.java)) {
            return GuiaViewModel(application, apiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
