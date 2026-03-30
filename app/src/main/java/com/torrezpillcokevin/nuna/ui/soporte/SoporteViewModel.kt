package com.torrezpillcokevin.nuna.ui.soporte

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.torrezpillcokevin.nuna.data.ApiService
import com.torrezpillcokevin.nuna.data.ContactoSupportRequest
import kotlinx.coroutines.launch

class SoporteViewModel(
    application: Application,
    private val apiService: ApiService
) : AndroidViewModel(application) {

    private val _status = MutableLiveData<Result<String>>()
    val status: LiveData<Result<String>> = _status

    // ✅ Usa el endpoint PÚBLICO /api/v1/public/contact-support
    // No requiere token ni permisos
    fun sendSupportRequest(
        nombre: String,
        email: String,
        titulo: String,
        mensaje: String,
        telefono: String? = null
    ) {
        viewModelScope.launch {
            try {
                val request = ContactoSupportRequest(
                    name    = nombre,
                    email   = email,
                    title   = titulo,
                    message = mensaje,
                    phone   = telefono,
                    user_id = null  // público, sin usuario
                )

                val response = apiService.enviarContactoPublico(request)

                if (response.isSuccessful) {
                    _status.postValue(Result.success("Mensaje enviado con éxito"))
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Error ${response.code()}"
                    _status.postValue(Result.failure(Exception(errorMsg)))
                }
            } catch (e: Exception) {
                _status.postValue(Result.failure(e))
            }
        }
    }
}