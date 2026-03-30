package com.torrezpillcokevin.nuna

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.torrezpillcokevin.nuna.data.ApiService
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class RegistroViewModel(
    application: Application,
    private val apiService: ApiService
) : AndroidViewModel(application) {

    private val _registroEstado = MutableLiveData<ResultadoRegistro>()
    val registroEstado: LiveData<ResultadoRegistro> = _registroEstado

    // ✅ Ahora recibe nombre, apellido, segundoApellido por separado
    fun registrarUsuario(
        nombre: String,
        apellido: String,
        segundoApellido: String,
        email: String,
        phone: String,
        pass: String,
        avatarBytes: ByteArray
    ) {
        _registroEstado.value = ResultadoRegistro.Cargando

        viewModelScope.launch {
            try {
                val mediaType = "text/plain".toMediaTypeOrNull()

                val requestFile = avatarBytes.toRequestBody("image/png".toMediaTypeOrNull())
                val avatarPart = MultipartBody.Part.createFormData("avatar", "avatar.png", requestFile)

                // ✅ Usa el endpoint PÚBLICO /api/v1/public/register-user
                // No requiere token ni permisos
                val response = apiService.registrarUsuarioPublico(
                    name           = nombre.toRequestBody(mediaType),
                    last_name      = apellido.toRequestBody(mediaType),
                    second_surname = segundoApellido.toRequestBody(mediaType),
                    email          = email.toRequestBody(mediaType),
                    password       = pass.toRequestBody(mediaType),
                    phone          = phone.toRequestBody(mediaType),
                    avatar         = avatarPart
                )

                if (response.isSuccessful) {
                    _registroEstado.postValue(ResultadoRegistro.Exito("¡Registro exitoso!"))
                } else {
                    val errorBody = response.errorBody()?.string()
                    val mensaje = try {
                        JSONObject(errorBody!!).getString("detail")
                    } catch (e: Exception) {
                        "Error del servidor (${response.code()})"
                    }
                    _registroEstado.postValue(ResultadoRegistro.Error(mensaje))
                }
            } catch (e: Exception) {
                _registroEstado.postValue(ResultadoRegistro.Error("Sin conexión: ${e.message}"))
            }
        }
    }

    sealed class ResultadoRegistro {
        object Cargando : ResultadoRegistro()
        data class Exito(val mensaje: String) : ResultadoRegistro()
        data class Error(val error: String) : ResultadoRegistro()
    }
}