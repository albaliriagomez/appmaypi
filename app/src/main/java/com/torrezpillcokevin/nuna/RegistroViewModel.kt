package com.torrezpillcokevin.nuna

import androidx.lifecycle.*
import com.torrezpillcokevin.nuna.data.ApiService
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class RegistroViewModel(private val apiService: ApiService) : ViewModel() {

    private val _registroEstado = MutableLiveData<ResultadoRegistro>()
    val registroEstado: LiveData<ResultadoRegistro> = _registroEstado

    fun registrarUsuario(name: String, email: String, phone: String, pass: String, avatarBytes: ByteArray) {
        viewModelScope.launch {
            try {
                val mediaType = "text/plain".toMediaTypeOrNull()
                val nameRB = name.toRequestBody(mediaType)
                val emailRB = email.toRequestBody(mediaType)
                val phoneRB = phone.toRequestBody(mediaType)
                val passRB = pass.toRequestBody(mediaType)
                val lastNameRB = "Pillco".toRequestBody(mediaType)
                val secondSurnameRB = "Kevin".toRequestBody(mediaType)
                val statusRB = "online".toRequestBody(mediaType)
                val codeRB = "USR-${System.currentTimeMillis()}".toRequestBody(mediaType)

                val requestFile = avatarBytes.toRequestBody("image/png".toMediaTypeOrNull())
                val avatarPart = MultipartBody.Part.createFormData("avatar", "avatar.png", requestFile)

                val response = apiService.createUser(codeRB, nameRB, lastNameRB, secondSurnameRB, emailRB, passRB, phoneRB, statusRB, null, avatarPart)

                if (response.isSuccessful) {
                    _registroEstado.postValue(ResultadoRegistro.Exito("Éxito"))
                } else {
                    _registroEstado.postValue(ResultadoRegistro.Error("Fallo en el servidor"))
                }
            } catch (e: Exception) {
                _registroEstado.postValue(ResultadoRegistro.Error(e.message ?: "Error"))
            }
        }
    }

    sealed class ResultadoRegistro {
        object Cargando : ResultadoRegistro()
        data class Exito(val mensaje: String) : ResultadoRegistro()
        data class Error(val error: String) : ResultadoRegistro()
    }
}