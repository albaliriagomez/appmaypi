package com.torrezpillcokevin.nuna.ui.soporte

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.torrezpillcokevin.nuna.data.ApiService
import com.torrezpillcokevin.nuna.data.SupportRequest
import com.torrezpillcokevin.nuna.data.UserData
import kotlinx.coroutines.launch

class SoporteViewModel(application: Application, private val apiService: ApiService) :
    AndroidViewModel(application) {

    private val _status = MutableLiveData<Result<String>>()
    val status: LiveData<Result<String>> = _status

    private val _user = MutableLiveData<UserData>()
    val user: LiveData<UserData> = _user

    fun sendSupportRequest(request: SupportRequest) {
        viewModelScope.launch {
            try {
                val token = getJwtToken()
                if (token == null) {
                    _status.postValue(Result.failure(Exception("Token no encontrado")))
                    return@launch
                }

                val response = apiService.createSupportRequest(request, "Bearer $token")
                if (response.isSuccessful) {
                    _status.postValue(Result.success("Solicitud enviada con éxito"))
                } else {
                    _status.postValue(Result.failure(Exception("Error: ${response.errorBody()?.string()}")))
                }
            } catch (e: Exception) {
                _status.postValue(Result.failure(e))
            }
        }
    }

    fun fetchUserInfo() {
        viewModelScope.launch {
            try {
                val token = getJwtToken()
                val userId = getUserId()
                if (token == null || userId == -1) {
                    _status.postValue(Result.failure(Exception("Token o ID no encontrados")))
                    return@launch
                }

                val response = apiService.getUserById(userId, "Bearer $token")
                if (response.isSuccessful) {
                    response.body()?.let {
                        _user.postValue(it.data)
                    } ?: run {
                        _status.postValue(Result.failure(Exception("Respuesta vacía")))
                    }
                } else {
                    _status.postValue(Result.failure(Exception("Error al obtener usuario: ${response.code()}")))
                }
            } catch (e: Exception) {
                _status.postValue(Result.failure(e))
            }
        }
    }

    private fun getJwtToken(): String? {
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

    private fun getUserId(): Int {
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
