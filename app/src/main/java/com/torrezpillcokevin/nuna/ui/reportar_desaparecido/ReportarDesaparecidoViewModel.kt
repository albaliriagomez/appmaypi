package com.torrezpillcokevin.nuna.ui.reportar_desaparecido

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.torrezpillcokevin.nuna.data.ApiService
import com.torrezpillcokevin.nuna.data.ReporteDesaparecido
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody


class ReportarDesaparecidoViewModel(
    application: Application,
    private val apiService: ApiService
) : AndroidViewModel(application) {

    private val _reporteExitoso = MutableLiveData<Boolean>()
    val reporteExitoso: LiveData<Boolean> = _reporteExitoso

    // ... (tus otros imports)

    fun reportarDesaparecido(
        context: Context,
        nombre: String, apellido: String, edad: String, genero: String,
        descripcion: String, fechaNac: String, fechaDes: String,
        lugar: String, caracteristicas: String,
        nombreRep: String, telfRep: String,
        uriPerfil: Uri, uriSuceso: Uri
    ) {
        viewModelScope.launch {
            try {
                val userId = getUserId().toString()

                // Mapeo exacto a lo que pide registrarDesaparecidoPublico en ApiService
                val map = mutableMapOf<String, RequestBody>()
                map["name"] = nombre.toRequestBody("text/plain".toMediaTypeOrNull())
                map["last_name"] = apellido.toRequestBody("text/plain".toMediaTypeOrNull())
                map["age"] = edad.toRequestBody("text/plain".toMediaTypeOrNull())
                map["gender"] = genero.toRequestBody("text/plain".toMediaTypeOrNull())
                map["description"] = descripcion.toRequestBody("text/plain".toMediaTypeOrNull())
                map["birthdate"] = fechaNac.toRequestBody("text/plain".toMediaTypeOrNull())
                map["disappearance_date"] = fechaDes.toRequestBody("text/plain".toMediaTypeOrNull())
                map["place_of_disappearance"] = lugar.toRequestBody("text/plain".toMediaTypeOrNull())
                map["characteristics"] = caracteristicas.toRequestBody("text/plain".toMediaTypeOrNull())
                map["reporter_name"] = nombreRep.toRequestBody("text/plain".toMediaTypeOrNull())
                map["reporter_phone"] = telfRep.toRequestBody("text/plain".toMediaTypeOrNull())
                map["id_usuario"] = userId.toRequestBody("text/plain".toMediaTypeOrNull())

                val photoPart = prepareFilePart(context, "photo", uriPerfil)
                val eventPart = prepareFilePart(context, "event_photo", uriSuceso)

                if (photoPart != null && eventPart != null) {
                    val response = apiService.registrarDesaparecidoPublico(
                        map["name"]!!, map["last_name"]!!, map["age"]!!, map["gender"]!!,
                        map["description"]!!, map["birthdate"]!!, map["disappearance_date"]!!,
                        map["place_of_disappearance"]!!, map["characteristics"]!!,
                        map["reporter_name"]!!, map["reporter_phone"]!!, map["id_usuario"]!!,
                        photoPart, eventPart
                    )
                    _reporteExitoso.postValue(response.isSuccessful)
                }
            } catch (e: Exception) {
                _reporteExitoso.postValue(false)
            }
        }
    }

    private fun prepareFilePart(context: Context, partName: String, fileUri: Uri): MultipartBody.Part? {
        return try {
            val inputStream = context.contentResolver.openInputStream(fileUri)
            val bytes = inputStream?.readBytes() ?: return null
            inputStream.close()
            val requestFile = bytes.toRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData(partName, "image_${partName}.jpg", requestFile)
        } catch (e: Exception) {
            null
        }
    }

    fun getJwtToken(): String? {
        val masterKey = MasterKey.Builder(getApplication())
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        val sharedPreferences = EncryptedSharedPreferences.create(
            getApplication(),
            "SECURE_APP_PREFS",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        return sharedPreferences.getString("JWT_TOKEN", null)
    }

    fun getUserId(): Int {
        val masterKey = MasterKey.Builder(getApplication())
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        val sharedPreferences = EncryptedSharedPreferences.create(
            getApplication(),
            "SECURE_APP_PREFS",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        return sharedPreferences.getInt("USER_ID", -1)
    }
}
