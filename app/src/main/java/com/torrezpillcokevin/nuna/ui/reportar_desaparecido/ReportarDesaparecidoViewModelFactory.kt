package com.torrezpillcokevin.nuna.ui.reportar_desaparecido

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.torrezpillcokevin.nuna.data.ApiService

class ReportarDesaparecidoViewModelFactory(
    private val application: Application,
    private val apiService: ApiService
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReportarDesaparecidoViewModel::class.java)) {
            return ReportarDesaparecidoViewModel(application, apiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
