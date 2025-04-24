package com.torrezpillcokevin.nuna.ui.guia

import android.app.Application
import android.os.Bundle
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
import kotlinx.coroutines.launch

class GuiaViewModel(application: Application, private val apiService: ApiService) :
    AndroidViewModel(application) {

    private val _guides = MutableLiveData<Result<Map<String, List<Guide>>>>()
    val guides: LiveData<Result<Map<String, List<Guide>>>> = _guides

    fun getGuides(pagina: Int, porPagina: Int) {
        viewModelScope.launch {
            try {
                val token = getJwtToken()
                if (token == null) {
                    _guides.postValue(Result.failure(Exception("Token no encontrado")))
                    return@launch
                }

                val response = apiService.getGuides(pagina, porPagina, "Bearer $token")
                if (response.isSuccessful) {
                    response.body()?.let {
                        val guidesByCategory = it.data.groupBy { guide -> guide.title } // Agrupamos por subtítulo (o categoría)
                        _guides.postValue(Result.success(guidesByCategory))
                    } ?: run {
                        _guides.postValue(Result.failure(Exception("Respuesta vacía")))
                    }
                } else {
                    _guides.postValue(Result.failure(Exception("Error: ${response.code()}")))
                }
            } catch (e: Exception) {
                _guides.postValue(Result.failure(e))
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
}
