package com.torrezpillcokevin.nuna.ui.muro

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
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
    private var hayMasPaginas = true
    private var cargando = false

    var yaSeCargaronInicialmente = false

    fun recargarDesdePrimeraPagina(porPagina: Int = 10) {
        paginaActual = 1
        hayMasPaginas = true
        _desaparecidos.value = emptyList()
        obtenerDesaparecidos(porPagina, isRefresh = true)
    }

    fun obtenerDesaparecidos(porPagina: Int = 10, isRefresh: Boolean = false) {
        if (cargando || (!isRefresh && !hayMasPaginas)) return
        cargando = true

        val paginaACargar = if (isRefresh) 1 else paginaActual

        viewModelScope.launch {
            try {
                Log.d("MuroViewModel", "Cargando página $paginaACargar...")

                // ✅ Sin token — igual que la web Angular que llama listMissing sin auth
                val response = apiService.obtenerDesaparecidos(
                    pagina    = paginaACargar,
                    porPagina = porPagina
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    val nuevosItems = body?.data ?: emptyList()

                    Log.d("MuroViewModel", "✅ ${nuevosItems.size} registros en página $paginaACargar")

                    val listaActualizada = if (isRefresh) {
                        nuevosItems
                    } else {
                        (_desaparecidos.value ?: emptyList()) + nuevosItems
                    }
                    _desaparecidos.postValue(listaActualizada)

                    // ✅ Igual que la web: usa links.next para saber si hay más páginas
                    hayMasPaginas = body?.links?.next != null
                    paginaActual = paginaACargar + 1

                    Log.d("MuroViewModel", "Total: ${body?.total}, hayMás: $hayMasPaginas")
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Error ${response.code()}"
                    Log.e("MuroViewModel", "❌ $errorMsg")
                    _status.postValue(Result.failure(Exception(errorMsg)))
                }
            } catch (e: Exception) {
                Log.e("MuroViewModel", "❌ Error: ${e.message}", e)
                _status.postValue(Result.failure(e))
            } finally {
                cargando = false
            }
        }
    }
}