package com.torrezpillcokevin.nuna.ui.opciones

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel

import androidx.lifecycle.ViewModel

class OpcionesViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPreferences = application.getSharedPreferences("mi_prefs", Context.MODE_PRIVATE)

    fun guardarDatos(numeroCelular: String) {
        with(sharedPreferences.edit()) {
            putString("numero_celular", numeroCelular)
            apply()
        }
    }
    fun getNumeroCelular(): String? {
        return sharedPreferences.getString("numero_celular", null)
    }
}