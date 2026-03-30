package com.torrezpillcokevin.nuna.ui.reportar_desaparecido

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.torrezpillcokevin.nuna.data.ApiService
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class ReportarDesaparecidoViewModel(
    application: Application,
    private val apiService: ApiService
) : AndroidViewModel(application) {

    private val _reporteExitoso = MutableLiveData<Boolean>()
    val reporteExitoso: LiveData<Boolean> = _reporteExitoso

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun reportarDesaparecido(
        context: Context,
        authHeader: String,
        nombre: String, apellido: String, edad: String, genero: String,
        descripcion: String, fechaNac: String, fechaDes: String,
        lugar: String, caracteristicas: String,
        nombreRep: String, telfRep: String,
        uriPerfil: Uri, uriSuceso: Uri
    ) {
        viewModelScope.launch {
            try {
                Log.d("ReporteDesaparecido", "=== INICIANDO ENVÍO ===")

                // Preparar fotos
                val fotoPerfil = prepareFilePart(context, "photo", uriPerfil)
                val fotoSuceso = prepareFilePart(context, "event_photo", uriSuceso)

                if (fotoPerfil == null || fotoSuceso == null) {
                    _errorMessage.postValue("Error al procesar las fotos")
                    _reporteExitoso.postValue(false)
                    return@launch
                }

                Log.d("ReporteDesaparecido", "Enviando a /api/v1/public/missing ...")

                // ✅ Llamada al endpoint correcto: POST /api/v1/public/missing
                // No requiere token ni permisos — es completamente público
                val response = try {
                    apiService.crearMissingPublico(
                        name                  = nombre.toRequestBody("text/plain".toMediaTypeOrNull()),
                        last_name             = apellido.toRequestBody("text/plain".toMediaTypeOrNull()),
                        age                   = edad.toRequestBody("text/plain".toMediaTypeOrNull()),
                        gender                = genero.toRequestBody("text/plain".toMediaTypeOrNull()),
                        description           = descripcion.toRequestBody("text/plain".toMediaTypeOrNull()),
                        birthdate             = fechaNac.toRequestBody("text/plain".toMediaTypeOrNull()),
                        disappearance_date    = fechaDes.toRequestBody("text/plain".toMediaTypeOrNull()),
                        place_of_disappearance = lugar.toRequestBody("text/plain".toMediaTypeOrNull()),
                        characteristics       = caracteristicas.toRequestBody("text/plain".toMediaTypeOrNull()),
                        reporter_name         = nombreRep.toRequestBody("text/plain".toMediaTypeOrNull()),
                        reporter_phone        = telfRep.toRequestBody("text/plain".toMediaTypeOrNull()),
                        photo                 = fotoPerfil,
                        event_photo           = fotoSuceso
                    )
                } catch (e: Exception) {
                    Log.e("ReporteDesaparecido", "❌ Error de red: ${e.message}", e)
                    _errorMessage.postValue("Error de red: ${e.message}")
                    _reporteExitoso.postValue(false)
                    return@launch
                }

                Log.d("ReporteDesaparecido", "Respuesta: ${response.code()}")

                if (response.isSuccessful) {
                    Log.d("ReporteDesaparecido", "✅✅✅ SOLICITUD ENVIADA EXITOSAMENTE")
                    _reporteExitoso.postValue(true)
                } else {
                    val error = response.errorBody()?.string() ?: "Error ${response.code()}"
                    Log.e("ReporteDesaparecido", "❌ Error del servidor: $error")
                    _errorMessage.postValue(error)
                    _reporteExitoso.postValue(false)
                }

            } catch (e: Exception) {
                Log.e("ReporteDesaparecido", "❌ CRASH: ${e.message}", e)
                _errorMessage.postValue("Error inesperado: ${e.message}")
                _reporteExitoso.postValue(false)
            }
        }
    }

    private fun prepareFilePart(context: Context, partName: String, fileUri: Uri): MultipartBody.Part? {
        return try {
            val contentResolver = context.contentResolver
            val type = contentResolver.getType(fileUri) ?: "image/jpeg"
            val inputStream = contentResolver.openInputStream(fileUri) ?: return null
            val bytes = inputStream.readBytes()
            inputStream.close()

            val extension = if (type == "image/png") ".png" else ".jpg"
            val requestFile = bytes.toRequestBody(type.toMediaTypeOrNull())

            MultipartBody.Part.createFormData(
                partName,
                "file_${System.currentTimeMillis()}$extension",
                requestFile
            )
        } catch (e: Exception) {
            Log.e("ReporteDesaparecido", "Error procesando archivo: ${e.message}")
            null
        }
    }

    // ========== PREFERENCIAS ==========

    private fun saveJwtToken(token: String) {
        try {
            getApplication<Application>()
                .getSharedPreferences("nuna_app", Context.MODE_PRIVATE)
                .edit().putString("jwt_token", token).apply()
        } catch (e: Exception) {
            Log.e("ReporteDesaparecido", "Error guardando token: ${e.message}")
        }
    }

    fun getJwtToken(): String? {
        return try {
            getApplication<Application>()
                .getSharedPreferences("nuna_app", Context.MODE_PRIVATE)
                .getString("jwt_token", null)
        } catch (e: Exception) {
            null
        }
    }

    fun getUserId(): Int {
        return try {
            getApplication<Application>()
                .getSharedPreferences("nuna_app", Context.MODE_PRIVATE)
                .getInt("user_id", -1)
        } catch (e: Exception) {
            -1
        }
    }

    fun saveUserId(id: Int) {
        try {
            getApplication<Application>()
                .getSharedPreferences("nuna_app", Context.MODE_PRIVATE)
                .edit().putInt("user_id", id).apply()
        } catch (e: Exception) {
            Log.e("ReporteDesaparecido", "Error guardando user_id: ${e.message}")
        }
    }
}