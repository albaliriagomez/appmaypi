package com.torrezpillcokevin.nuna.ui.reportar

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.ar.core.Config
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.ImageHolder
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.MapAnimationOptions.Companion.mapAnimationOptions
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.location
import com.torrezpillcokevin.nuna.R



class MapBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var mapView: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var pointAnnotationManager: PointAnnotationManager

    interface OnLocationSelectedListener {
        fun onLocationSelected(point: Point)
    }

    private var locationListener: OnLocationSelectedListener? = null

    // Método para asignar el listener
    fun setLocationListener(listener: OnLocationSelectedListener) {
        this.locationListener = listener
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflar el layout del BottomSheet
        return inflater.inflate(R.layout.fragment_map_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        mapView = view.findViewById(R.id.mapView4)
           val cameraOptions = CameraOptions.Builder()
               .center(Point.fromLngLat(-66.1667, -17.4089))  // Coordenadas de Cochabamba
               .zoom(10.0)  // Nivel de zoom
               .build()
           mapView.getMapboxMap().setCamera(cameraOptions)

           mapView.getMapboxMap().loadStyleUri(
               Style.MAPBOX_STREETS
           ) {
               configurarClicEnMapa()
           }


        // Configura el botón de cierre
        val closeButton = view.findViewById<ImageButton>(R.id.btnClose2)
        closeButton.setOnClickListener {
            dismiss() // Cerrar el BottomSheet
        }

        // Configura el botón de cierre
        val MarcarUbicacion = view.findViewById<ImageButton>(R.id.btnCurrentLocation)
        MarcarUbicacion.setOnClickListener {
            checkLocationPermission()
        }

        //regresar posision inciial
        val ResetCamera = view.findViewById<ImageButton>(R.id.btnResetCamera)
        ResetCamera.setOnClickListener {
            regresarPosicionInicial()
        }

    }

    override fun onStart() {
        super.onStart()
        val behavior = BottomSheetBehavior.from(requireView().parent as View)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.skipCollapsed = true
        behavior.isDraggable = false
        behavior.isHideable = false
        mapView.onStart()

    }


    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        pointAnnotationManager.deleteAll()
        mapView.onDestroy()
    }

    companion object {
        private const val PERMISSION_REQUEST_LOCATION = 1
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            obtenerUbicacionYColocarEnMapa()
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_LOCATION
            )
        }
    }


    private fun regresarPosicionInicial() {
        // Crear un punto a partir de las coordenadas de ubicación
        val userLocationPoint = Point.fromLngLat(-66.1667, -17.4089)
        // Animar la cámara a la ubicación del usuario
        mapView.getMapboxMap().flyTo(
            CameraOptions.Builder()
                .center(userLocationPoint) // Colocar la cámara en la ubicación actual
                .zoom(10.0) // Zoom adecuado para ver la ubicación en detalle
                .bearing(0.0) // Rotación de la cámara
                .pitch(0.0) // Inclinación de la cámara
                .build(),
            mapAnimationOptions {
                duration(3000) // Duración de la animación en milisegundos
            }
        )
    }

    private fun obtenerUbicacionYColocarEnMapa() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val locationManager =
                requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val isLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            if (isLocationEnabled) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        val latitude = location.latitude
                        val longitude = location.longitude
                        val userLocationPoint = Point.fromLngLat(longitude, latitude)

                        mapView.getMapboxMap().flyTo(
                            CameraOptions.Builder()
                                .center(userLocationPoint)
                                .zoom(18.0)
                                .bearing(0.0)
                                .pitch(0.0)
                                .build(),
                            MapAnimationOptions.mapAnimationOptions {
                                duration(3000)
                            }
                        )

                        // Configurar el componente de ubicación
                        mapView.location.apply {
                            enabled = true
                            locationPuck = LocationPuck2D(
                                topImage = ImageHolder.from(R.drawable.mapbox_user_icon),
                                shadowImage = ImageHolder.from(R.drawable.mapbox_user_stroke_icon)
                            )
                            puckBearingEnabled = true
                            puckBearing = PuckBearing.HEADING
                            showAccuracyRing = true
                        }


                    }
                }
            } else {
                AlertDialog.Builder(requireContext())
                    .setTitle("Ubicación desactivada")
                    .setMessage("La ubicación está desactivada. Por favor, actívala para obtener la ubicación.")
                    .setPositiveButton("Ir a Ajustes") { _, _ ->
                        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        startActivity(intent)
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }
    }

    private fun configurarClicEnMapa() {
        pointAnnotationManager = mapView.annotations.createPointAnnotationManager()

        // Configurar botón de confirmación
        val btnConfirm = requireView().findViewById<Button>(R.id.btnConfirmLocation)
        var selectedPoint: Point? = null

        mapView.gestures.addOnMapClickListener { point ->
            // 1. Eliminar marcadores anteriores
            pointAnnotationManager.deleteAll()

            // 2. Crear nuevo marcador
            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.red_marker)
            pointAnnotationManager.create(
                PointAnnotationOptions()
                    .withPoint(Point.fromLngLat(point.longitude(), point.latitude()))
                    .withIconImage(bitmap)
                    .withIconSize(0.5)
            )

            // 3. Guardar punto seleccionado
            selectedPoint = Point.fromLngLat(point.longitude(), point.latitude())

            // 4. Mostrar coordenadas
            Toast.makeText(
                context,
                "Marcado: ${"%.6f".format(point.latitude())}, ${"%.6f".format(point.longitude())}",
                Toast.LENGTH_SHORT
            ).show()

            true
        }

        // Configurar el botón de confirmación
        btnConfirm.setOnClickListener {
            selectedPoint?.let { point ->
                locationListener?.onLocationSelected(point)
                dismiss()
            } ?: run {
                Toast.makeText(context, "Selecciona una ubicación primero", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
