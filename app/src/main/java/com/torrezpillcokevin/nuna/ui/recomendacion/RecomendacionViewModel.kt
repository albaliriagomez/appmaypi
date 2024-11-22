package com.torrezpillcokevin.nuna.ui.recomendacion

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.torrezpillcokevin.nuna.data.RetrofitInstance
import com.torrezpillcokevin.nuna.data.User
import kotlinx.coroutines.launch

class RecomendacionViewModel : ViewModel() {
    // TODO: Implement the ViewModel
    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> get() = _users

    // Instancia de Gson para convertir objetos a JSON
    private val gson = Gson()

    fun fetchUsers(page: Int, itemsPerPage: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getUsers(page, itemsPerPage)
                if (response.isSuccessful) {
                    // Imprimir la respuesta en formato JSON
                    response.body()?.let { apiResponse ->
                        Log.d("OpcionesViewModel", "Response: ${gson.toJson(apiResponse)}")
                        _users.value = apiResponse.data
                    }
                } else {
                    // Manejo de error: puedes lanzar una excepción o registrar el error
                    Log.e("OpcionesViewModel", "Error fetching users: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("OpcionesViewModel", "Exception: ${e.message}")
                // Manejo de excepciones
            }
        }
    }
}