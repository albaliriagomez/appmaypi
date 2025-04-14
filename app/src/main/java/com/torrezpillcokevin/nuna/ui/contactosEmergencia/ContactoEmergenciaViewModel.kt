package com.torrezpillcokevin.nuna.ui.contactosEmergencia

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ContactosViewModel : ViewModel() {
    private val _contactosActualizados = MutableLiveData<Boolean>()
    val contactosActualizados: LiveData<Boolean> = _contactosActualizados

    fun notificarActualizacion() {
        _contactosActualizados.postValue(true)
    }
}