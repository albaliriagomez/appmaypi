package com.torrezpillcokevin.nuna

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.torrezpillcokevin.nuna.data.ApiService
import com.torrezpillcokevin.nuna.data.UserRequest
import kotlinx.coroutines.launch

class RegistroViewModel(private val apiService: ApiService) : ViewModel() {

    private val _registroEstado = MutableLiveData<ResultadoRegistro>()
    val registroEstado: LiveData<ResultadoRegistro> = _registroEstado

    fun registrarUsuario(userRequest: UserRequest) {
        viewModelScope.launch {
            try {
                val response = apiService.createUser(userRequest)
                if (response.isSuccessful && response.body() != null) {
                    _registroEstado.postValue(ResultadoRegistro.Exito("Registro exitoso"))
                } else {
                    val error = response.errorBody()?.string() ?: "Error desconocido"
                    _registroEstado.postValue(ResultadoRegistro.Error("Error: $error"))
                }
            } catch (e: Exception) {
                _registroEstado.postValue(ResultadoRegistro.Error("Excepción: ${e.message}"))
            }
        }
    }


    sealed class ResultadoRegistro {
        data class Exito(val mensaje: String) : ResultadoRegistro()
        data class Error(val error: String) : ResultadoRegistro()
    }
}
