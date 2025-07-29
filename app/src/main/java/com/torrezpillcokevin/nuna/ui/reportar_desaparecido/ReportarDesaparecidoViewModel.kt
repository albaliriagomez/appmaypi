package com.torrezpillcokevin.nuna.ui.reportar_desaparecido

import android.app.Application
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

class ReportarDesaparecidoViewModel(
    application: Application,
    private val apiService: ApiService
) : AndroidViewModel(application) {

    private val _status = MutableLiveData<Result<String>>()
    private val _reporteExitoso = MutableLiveData<Boolean>()
    val reporteExitoso: LiveData<Boolean> = _reporteExitoso


    fun reportarDesaparecido(reporte: ReporteDesaparecido) {
        viewModelScope.launch {
            try {
                val token = getJwtToken()
                if (token == null) {
                    _status.postValue(Result.failure(Exception("Token no encontrado")))
                    return@launch
                }

                val response = apiService.reportarDesaparecido("Bearer $token", reporte)
                if (response.isSuccessful) {
                    _status.postValue(Result.success("Reporte enviado con éxito"))
                    _reporteExitoso.postValue(true) // Indicar que fue exitoso
                } else {
                    _status.postValue(Result.failure(Exception("Error: ${response.errorBody()?.string()}")))
                    _reporteExitoso.postValue(false)
                }
            } catch (e: Exception) {
                _status.postValue(Result.failure(e))
                _reporteExitoso.postValue(false)
            }
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
