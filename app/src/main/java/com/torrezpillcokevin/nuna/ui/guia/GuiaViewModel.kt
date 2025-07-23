package com.torrezpillcokevin.nuna.ui.guia

import android.app.Application
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.torrezpillcokevin.nuna.R
import com.torrezpillcokevin.nuna.data.ApiService
import com.torrezpillcokevin.nuna.data.Guide
import androidx.lifecycle.viewModelScope
import com.torrezpillcokevin.nuna.data.GuideCategory
import com.torrezpillcokevin.nuna.data.GuideCategoryResponse
import kotlinx.coroutines.launch

class GuiaViewModel(application: Application, private val apiService: ApiService) :
    AndroidViewModel(application) {

    private val _categories = MutableLiveData<Result<GuideCategoryResponse>>()
    val categories: LiveData<Result<GuideCategoryResponse>> = _categories

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val loadedIds = mutableSetOf<Int>() // Para trackear IDs ya cargados

    fun getGuideCategories(pagina: Int, itemsPerPage: Int) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                val token = getJwtToken() ?: throw Exception("Token no encontrado")

                val response = apiService.getGuideCategories(
                    pagina = pagina,
                    porPagina = itemsPerPage,
                    token = "Bearer $token"
                )

                if (response.isSuccessful) {
                    response.body()?.let { apiResponse ->
                        // Filtrar duplicados usando los IDs
                        val newItems = apiResponse.data.filterNot { loadedIds.contains(it.id) }
                        loadedIds.addAll(newItems.map { it.id })

                        // Ordenar por número de tema (extraído del título)
                        val sortedData = newItems.sortedBy {
                            it.title.replace("TEMA", "").trim().toIntOrNull() ?: 0
                        }

                        _categories.postValue(Result.success(
                            apiResponse.copy(data = sortedData)
                        ))
                    }
                } else {
                    // Manejo de errores
                }
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
