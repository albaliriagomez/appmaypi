package com.torrezpillcokevin.nuna.ui.muro

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.torrezpillcokevin.nuna.data.ApiService

class MuroViewModelFactory(
    private val application: Application,
    private val apiService: ApiService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MuroViewModel::class.java)) {
            return MuroViewModel(application, apiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
