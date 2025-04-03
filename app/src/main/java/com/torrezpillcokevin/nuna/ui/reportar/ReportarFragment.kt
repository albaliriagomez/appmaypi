package com.torrezpillcokevin.nuna.ui.reportar

import android.Manifest
import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.ImageHolder
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.location
import com.torrezpillcokevin.nuna.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ReportarFragment : Fragment() {
    private lateinit var mapView: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var pointAnnotationManager: PointAnnotationManager

    private lateinit var dateField: TextInputEditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_reportar, container, false)

        // Inicializar el cliente de ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Configurar Mapbox
        mapView = view.findViewById(R.id.mapContainer)
        val cameraOptions = CameraOptions.Builder()
            .center(Point.fromLngLat(-66.1667, -17.4089))  // Coordenadas de Cochabamba
            .zoom(10.0)  // Nivel de zoom
            .build()
        mapView.getMapboxMap().setCamera(cameraOptions)

        mapView.getMapboxMap().loadStyleUri(
            Style.MAPBOX_STREETS
        ) {
            // Verificar permisos después de cargar el estilo
           // checkLocationPermission()
            configurarClicEnMapa()
        }


        // Configurar el campo de fecha y hora
        dateField = view.findViewById(R.id.dateField)
        dateField.setOnClickListener {
            showDateTimePicker()
        }


        return view
    }
    private fun showDateTimePicker() {
        // Crear selector de fecha
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Seleccionar fecha")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.addOnPositiveButtonClickListener { selectedDate ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = selectedDate

            // Mostrar el selector de hora con Material Time Picker
            val timePicker = MaterialTimePicker.Builder()
                .setTitleText("Seleccionar hora")
                .setTimeFormat(TimeFormat.CLOCK_24H) // Formato de 24 horas
                .build()

            timePicker.addOnPositiveButtonClickListener {
                // Obtener la hora seleccionada
                val hour = timePicker.hour
                val minute = timePicker.minute
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)

                // Formatear y mostrar fecha + hora
                val formattedDateTime = SimpleDateFormat(
                    "dd/MM/yyyy HH:mm", Locale.getDefault()
                ).format(calendar.time)

                dateField.setText(formattedDateTime)
            }

            timePicker.show(parentFragmentManager, "TIME_PICKER")
        }

        datePicker.show(parentFragmentManager, "DATE_PICKER")
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                obtenerUbicacionYColocarEnMapa()
            } else {
                Toast.makeText(requireContext(), "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun configurarClicEnMapa() {
        // Inicializar el manager de anotaciones una sola vez
        pointAnnotationManager = mapView.annotations.createPointAnnotationManager()

        mapView.gestures.addOnMapClickListener { point ->
            // 1. Eliminar marcadores anteriores
            pointAnnotationManager.deleteAll()

            // 2. Crear nuevo marcador en la posición del clic (sin mover la cámara)
            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.red_marker)
            pointAnnotationManager.create(
                PointAnnotationOptions()
                    .withPoint(Point.fromLngLat(point.longitude(), point.latitude()))
                    .withIconImage(bitmap)
                    .withIconSize(0.5) // Ajusta el tamaño según necesites
            )

            // 3. Mostrar coordenadas (opcional)
            Toast.makeText(
                context,
                "Marcado: ${point.latitude()}, ${point.longitude()}",
                Toast.LENGTH_SHORT
            ).show()

            true // Indica que el evento fue manejado
        }
    }

    //metodo para obtener la ubicaion y marcar
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
                                duration(2000)
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

                        Toast.makeText(
                            requireContext(),
                            "¡Ubicación encontrada! $latitude, $longitude",
                            Toast.LENGTH_LONG
                        ).show()
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

    override fun onStart() {
        super.onStart()
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
}