package com.torrezpillcokevin.nuna.ui.muro

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.torrezpillcokevin.nuna.data.ApiService
import com.torrezpillcokevin.nuna.data.ReporteDesaparecido
import kotlinx.coroutines.launch

class MuroViewModel(
    application: Application,
    private val apiService: ApiService
) : AndroidViewModel(application) {

    private val _desaparecidos = MutableLiveData<List<ReporteDesaparecido>>(emptyList())
    val desaparecidos: LiveData<List<ReporteDesaparecido>> = _desaparecidos

    private val _status = MutableLiveData<Result<String>>()
    val status: LiveData<Result<String>> = _status

    private var paginaActual = 1
    private var totalPaginas = 1
    private var cargando = false
    var yaSeCargaronInicialmente = false

    fun recargarDesdePrimeraPagina(porPagina: Int = 5) {
        paginaActual = 1
        totalPaginas = 1
        _desaparecidos.value = emptyList()
        obtenerDesaparecidos(porPagina)
    }
    fun obtenerDesaparecidos(porPagina: Int = 5) {
        if (cargando || paginaActual > totalPaginas) return


        cargando = true

        viewModelScope.launch {
            try {
                val token = getJwtToken()
                if (token == null) {
                    _status.postValue(Result.failure(Exception("Token no encontrado")))
                    cargando = false
                    return@launch
                }

                val response = apiService.obtenerDesaparecidos(
                    pagina = paginaActual,
                    porPagina = porPagina,
                    token = "Bearer $token"
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    val nuevos = body?.data ?: emptyList()
                    val anteriores = _desaparecidos.value ?: emptyList()

                    _desaparecidos.postValue(anteriores + nuevos)

                    paginaActual = body?.pagina_actual?.plus(1) ?: paginaActual + 1
                    totalPaginas = body?.total_paginas ?: totalPaginas

                    _status.postValue(Result.success("Página $paginaActual cargada"))
                } else {
                    val error = response.errorBody()?.string()
                    _status.postValue(Result.failure(Exception("Error al obtener datos: $error")))
                }

            } catch (e: Exception) {
                _status.postValue(Result.failure(e))
            } finally {
                cargando = false
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
