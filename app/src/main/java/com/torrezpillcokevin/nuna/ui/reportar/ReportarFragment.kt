package com.torrezpillcokevin.nuna.ui.reportar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.torrezpillcokevin.nuna.R

class ReportarFragment : Fragment() {

    private lateinit var mapView: MapView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_reportar, container, false)

        // Configurar Mapbox
        mapView = view.findViewById(R.id.mapContainer)
        mapView.getMapboxMap().loadStyleUri(
            Style.MAPBOX_STREETS
        ) {
            // Callback después de cargar el estilo
        }

        // Configurar botones (opcional)
        val sendButton: View = view.findViewById(R.id.sendButton)
        sendButton.setOnClickListener {
            // Acción del botón enviar
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDestroy() // Asegúrate de liberar recursos al destruir la vista
    }
}
