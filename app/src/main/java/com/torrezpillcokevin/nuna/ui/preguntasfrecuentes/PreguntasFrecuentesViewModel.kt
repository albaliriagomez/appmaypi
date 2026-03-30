package com.torrezpillcokevin.nuna.ui.preguntasfrecuentes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.torrezpillcokevin.nuna.data.ApiService
import com.torrezpillcokevin.nuna.data.CategoryResponse
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

    // DATOS MANUALES DE LA WEB
    private val faqsManuales = listOf(
        Faq(1, "¿Cuánto tiempo debo esperar para reportar una desaparición?", "No es necesario esperar ningún tiempo. Puedes reportar una desaparición inmediatamente si consideras que la persona está en riesgo o su ausencia es inusual.", CategoryResponse(1, "Sobre Reportes")),
        Faq(2, "¿Qué información necesito para reportar una desaparición?", "Necesitarás proporcionar datos personales de la persona desaparecida (nombre completo, edad, características físicas), fotografías recientes, información sobre la última vez que fue vista.", CategoryResponse(1, "Sobre Reportes")),
        Faq(3, "¿Cómo funciona Maypi?", "Maypi es una plataforma que conecta a familias, autoridades y comunidades para maximizar las posibilidades de encontrar personas desaparecidas.", CategoryResponse(2, "Sobre la Plataforma")),
        Faq(4, "¿Es gratuito el uso de Maypi?", "Sí, Maypi es completamente gratuito para reportar desapariciones, buscar personas y colaborar.", CategoryResponse(2, "Sobre la Plataforma")),
        Faq(5, "¿Cómo puedo ayudar en la búsqueda?", "Puedes ayudar compartiendo los casos en redes sociales, uniéndote como voluntario a grupos de búsqueda, o reportando cualquier avistamiento.", CategoryResponse(3, "Sobre Colaboración"))
    )

    fun getFaqs(pagina: Int, itemsPerPage: Int) {
        viewModelScope.launch {
            _isLoading.postValue(true)

            // CARGA MANUAL: Esto asegura que siempre haya contenido como en la Web
            val responseManual = FaqListResponse(
                message = "Éxito",
                data = faqsManuales,
                total = faqsManuales.size,
                page = 1,
                size = 10
            )
            _faqsResult.postValue(Result.success(responseManual))
            _isLoading.postValue(false)

            // Intento de carga de backend (opcional, si quieres que se actualice si hay red)
            /*
            try {
                val response = apiService.getFaqs(pagina, itemsPerPage, "Bearer ...")
                if (response.isSuccessful) {
                    response.body()?.let { _faqsResult.postValue(Result.success(it)) }
                }
            } catch (e: Exception) {
                // Si falla el backend, ya tenemos los manuales cargados arriba
            }
            */
        }
    }
}