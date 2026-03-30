package com.torrezpillcokevin.nuna

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.torrezpillcokevin.nuna.data.ApiService
import com.torrezpillcokevin.nuna.data.Login
import kotlinx.coroutines.launch

class LoginViewModel(application: Application, private val apiService: ApiService) : AndroidViewModel(application) {

    private val _loginState = MutableLiveData<ResultadoLogin>()
    val loginState: LiveData<ResultadoLogin> = _loginState
    private val context: Context = getApplication<Application>().applicationContext

    fun login(login: Login) {
        // DISPARAR ESTADO CARGANDO
        _loginState.value = ResultadoLogin.Cargando

        viewModelScope.launch {
            try {
                val response = apiService.postLogin(login.username, login.password)
                if (response.isSuccessful && response.body() != null) {
                    val auth = response.body()!!
                    saveSecureData(auth.token, auth.user.id, auth.user.email)
                    _loginState.postValue(ResultadoLogin.Exito("Bienvenido ${auth.user.name}"))
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Credenciales incorrectas"
                    _loginState.postValue(ResultadoLogin.Error(errorBody))
                }
            } catch (e: Exception) {
                _loginState.postValue(ResultadoLogin.Error("Error de conexión: ${e.message}"))
            }
        }
    }

    private fun saveSecureData(token: String?, userId: Int, email: String?) {
        try {
            val masterKey = MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
            val sharedPrefs = EncryptedSharedPreferences.create(
                context, "SECURE_APP_PREFS", masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            sharedPrefs.edit().apply {
                putString("JWT_TOKEN", token)
                putInt("USER_ID", userId)
                putString("EMAIL", email)
                apply()
            }
        } catch (e: Exception) { Log.e("AUTH", "Error al guardar sesión") }
    }

    fun isUserLoggedIn(): Boolean {
        return try {
            val masterKey = MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
            val sharedPrefs = EncryptedSharedPreferences.create(
                context, "SECURE_APP_PREFS", masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            !sharedPrefs.getString("JWT_TOKEN", null).isNullOrEmpty()
        } catch (e: Exception) { false }
    }


    // SEALED CLASS ACTUALIZADA
    sealed class ResultadoLogin {
        object Cargando : ResultadoLogin() // Agregado
        data class Exito(val message: String) : ResultadoLogin()
        data class Error(val error: String) : ResultadoLogin()
    }
}