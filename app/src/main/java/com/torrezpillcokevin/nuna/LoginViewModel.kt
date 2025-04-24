package com.torrezpillcokevin.nuna

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.torrezpillcokevin.nuna.data.ApiService
import com.torrezpillcokevin.nuna.data.Login
import kotlinx.coroutines.launch

class LoginViewModel(application: Application, private val apiService: ApiService) : AndroidViewModel(application) {

    private val _loginState = MutableLiveData<ResultadoLogin>()
    val loginState: LiveData<ResultadoLogin> = _loginState

    // Accede al contexto de la aplicación
    @SuppressLint("StaticFieldLeak")
    private val context: Context = getApplication<Application>().applicationContext

    fun login(login: Login) {
        viewModelScope.launch {
            try {
                val response = apiService.postLogin(login.username, login.password)
                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()
                    // Guardar datos de autenticación de forma segura
                    saveSecureData(authResponse?.access_token, authResponse?.user_id, authResponse?.email)
                    _loginState.postValue(ResultadoLogin.Exito("Inicio de sesión exitoso"))
                } else {
                    val error = response.errorBody()?.string() ?: "Error desconocido"
                    _loginState.postValue(ResultadoLogin.Error("Error: $error"))
                }
            } catch (e: Exception) {
                _loginState.postValue(ResultadoLogin.Error("Excepción: ${e.message}"))
            }
        }
    }

    private fun saveSecureData(token: String?, userId: Int?, email: String?) {
        try {
            // 1. Crear una instancia de EncryptedSharedPreferences
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            val sharedPreferences = EncryptedSharedPreferences.create(
                context,
                "SECURE_APP_PREFS", // Nombre del archivo de preferencias
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, // Cifrado de claves
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM // Cifrado de valores
            )

            // 2. Guardar los datos cifrados
            sharedPreferences.edit()
                .putString("JWT_TOKEN", token) // Guardar el token JWT
                .putInt("USER_ID", userId ?: -1) // Guardar el ID del usuario
                .putString("EMAIL", email) // Guardar el correo electrónico
                .apply()

            Log.d("SECURE_STORAGE", "Datos guardados de forma segura.")
        } catch (e: Exception) {
            Log.e("SECURE_STORAGE", "Error al guardar datos cifrados: ${e.message}")
        }
    }

    sealed class ResultadoLogin {
        data class Exito(val message: String) : ResultadoLogin()
        data class Error(val error: String) : ResultadoLogin()
    }
}
