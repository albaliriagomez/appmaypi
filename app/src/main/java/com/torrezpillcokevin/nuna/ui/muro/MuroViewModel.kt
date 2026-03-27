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

    // Control para no recargar innecesariamente
    var yaSeCargaronInicialmente = false

    fun recargarDesdePrimeraPagina(porPagina: Int = 10) {
        paginaActual = 1
        totalPaginas = 1
        // No limpiamos la lista inmediatamente para evitar saltos visuales bruscos,
        // la respuesta de la API la reemplazará.
        obtenerDesaparecidos(porPagina, isRefresh = true)
    }

    fun obtenerDesaparecidos(porPagina: Int = 10, isRefresh: Boolean = false) {
        // Si ya estamos cargando o llegamos al final, ignorar
        if (cargando || (!isRefresh && paginaActual > totalPaginas)) return

        cargando = true

        viewModelScope.launch {
            try {
                val token = getJwtToken()
                if (token == null) {
                    _status.postValue(Result.failure(Exception("Sesión expirada o token no encontrado")))
                    cargando = false
                    return@launch
                }

                val response = apiService.obtenerDesaparecidos(
                    pagina = if (isRefresh) 1 else paginaActual,
                    porPagina = porPagina,
                    token = "Bearer $token"
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    val nuevosItems = body?.data ?: emptyList()

                    val listaActualizada = if (isRefresh) {
                        nuevosItems
                    } else {
                        val anteriores = _desaparecidos.value ?: emptyList()
                        anteriores + nuevosItems
                    }

                    _desaparecidos.postValue(listaActualizada)

                    // Actualizar punteros de paginación
                    totalPaginas = body?.total_paginas ?: 1
                    paginaActual = (body?.pagina_actual ?: 1) + 1

                    _status.postValue(Result.success("Datos cargados correctamente"))
                } else {
                    _status.postValue(Result.failure(Exception("Error de servidor: ${response.code()}")))
                }
            } catch (e: Exception) {
                _status.postValue(Result.failure(e))
            } finally {
                cargando = false
            }
        }
    }

    // --- Helpers de Preferencias ---
    private fun getEncryptedPrefs() = EncryptedSharedPreferences.create(
        getApplication(),
        "SECURE_APP_PREFS",
        MasterKey.Builder(getApplication()).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun getJwtToken(): String? = getEncryptedPrefs().getString("JWT_TOKEN", null)
}