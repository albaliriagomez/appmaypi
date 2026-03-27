package com.torrezpillcokevin.nuna.ui.preguntasfrecuentes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.torrezpillcokevin.nuna.data.ApiService
import com.torrezpillcokevin.nuna.data.Faq
import com.torrezpillcokevin.nuna.data.FaqListResponse
import kotlinx.coroutines.launch

class PreguntasFrecuentesViewModel(
    application: Application,
    private val apiService: ApiService
) : AndroidViewModel(application) {

    private val _faqsResult = MutableLiveData<Result<FaqListResponse>>()
    val faqsResult: LiveData<Result<FaqListResponse>> = _faqsResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val accumulatedFaqs = mutableListOf<Faq>()

    fun getFaqs(pagina: Int, itemsPerPage: Int) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                val token = getJwtToken() ?: throw Exception("Sesión expirada")
                val response = apiService.getFaqs(pagina, itemsPerPage, "Bearer $token")

                if (response.isSuccessful) {
                    val body: FaqListResponse? = response.body() // Especificamos el tipo explícitamente
                    body?.let { res ->
                        val newItems = res.data.filter { newItem ->
                            accumulatedFaqs.none { it.id == newItem.id }
                        }
                        accumulatedFaqs.addAll(newItems)

                        // Aquí forzamos el tipo FaqListResponse para el Result
                        _faqsResult.postValue(Result.success(res.copy(data = accumulatedFaqs.toList())))
                    }
                } else {
                    _faqsResult.postValue(Result.failure(Exception("Error servidor: ${response.code()}")))
                }
            } catch (e: Exception) {
                _faqsResult.postValue(Result.failure(e))
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    private fun getJwtToken(): String? {
        val masterKey = MasterKey.Builder(getApplication()).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
        return EncryptedSharedPreferences.create(
            getApplication(), "SECURE_APP_PREFS", masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ).getString("JWT_TOKEN", null)
    }
}