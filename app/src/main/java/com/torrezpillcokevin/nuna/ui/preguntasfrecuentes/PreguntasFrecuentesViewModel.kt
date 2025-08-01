package com.torrezpillcokevin.nuna.ui.preguntasfrecuentes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.torrezpillcokevin.nuna.data.ApiService
import com.torrezpillcokevin.nuna.data.Faq
import com.torrezpillcokevin.nuna.data.FaqResponse
import kotlinx.coroutines.launch

class PreguntasFrecuentesViewModel(
    application: Application,
    private val apiService: ApiService
) : AndroidViewModel(application) {

    private val _faqs = MutableLiveData<Result<FaqResponse>>()
    val faqs: LiveData<Result<FaqResponse>> = _faqs

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val loadedIds = mutableSetOf<Int>()
    private val accumulatedFaqs = mutableListOf<Faq>()

    fun getFaqs(pagina: Int, itemsPerPage: Int) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                val token = getJwtToken() ?: throw Exception("Token no encontrado")
                val authHeader = "Bearer $token"

                val response = apiService.getFaqs(pagina, itemsPerPage, authHeader)

                if (response.isSuccessful) {
                    response.body()?.let { res ->
                        // Filtrar duplicados y acumular
                        val newItems = res.data.filterNot { loadedIds.contains(it.id) }
                        loadedIds.addAll(newItems.map { it.id })

                        accumulatedFaqs.addAll(newItems)

                        // Postear toda la lista acumulada
                        _faqs.postValue(Result.success(res.copy(data = accumulatedFaqs.toList())))
                    }
                } else {
                    _faqs.postValue(Result.failure(Exception("Error: ${response.code()}")))
                }
            } catch (e: Exception) {
                _faqs.postValue(Result.failure(e))
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    private fun getJwtToken(): String? {
        return try {
            val masterKey = MasterKey.Builder(getApplication())
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                getApplication(),
                "SECURE_APP_PREFS",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            ).getString("JWT_TOKEN", null)
        } catch (e: Exception) {
            null
        }
    }
}
