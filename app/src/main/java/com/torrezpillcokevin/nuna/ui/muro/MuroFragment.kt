package com.torrezpillcokevin.nuna.ui.muro

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.torrezpillcokevin.nuna.R
import com.torrezpillcokevin.nuna.models.Persona
import com.torrezpillcokevin.nuna.ui.muro.adapter.PersonaAdapter

class MuroFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PersonaAdapter
    private lateinit var searchView: SearchView
    private lateinit var txtResultados: TextView
    private lateinit var personas: List<Persona>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_muro, container, false)

        // Inicializar vistas
        recyclerView = view.findViewById(R.id.recyclerPersonas)
        searchView = view.findViewById(R.id.searchView)
        txtResultados = view.findViewById(R.id.txtResultados)

        // Configurar RecyclerView
        recyclerView.layoutManager = GridLayoutManager(context, 2)

        // Inicializar datos
        inicializarDatos()

        // Configurar SearchView
        configurarBuscador()

        // Configurar FAB para navegar al fragmento de reportar
        val fabAgregar = view.findViewById<FloatingActionButton>(R.id.fabAgregarPersona)
        fabAgregar.setOnClickListener {
            try {
                findNavController().navigate(R.id.reportarFragment)
            } catch (e: Exception) {
                Log.e("MuroFragment", "Navigation failed", e)
                Toast.makeText(context, "Error al navegar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        return view


    }

    private fun inicializarDatos() {
        // Aquí puedes cargar los datos de tu API o fuente de datos
        personas = listOf(
            Persona(
                nombre = "Juan",
                apellido = "Pérez",
                edad = 30,
                genero = "Masculino",
                descripcion = "Desapareció en la zona centro mientras caminaba hacia su trabajo",
                fechaNacimiento = "15/05/1994",
                fechaDesaparicion = "01/04/2025",
                lugarDesaparicion = "Ciudad de México",
                estadoInvestigacion = "En investigación",
                imagen = R.drawable.adultos, // Asegúrate de tener esta imagen en tus recursos
                caracteristicas = "1.75m, cabello corto negro, lunar en la mejilla izquierda",
                tiempoDesaparecido = "2 días desaparecido",
                nombreContacto = "María Pérez",
                telefonoContacto = "555-123-4567",
                emailContacto = "maria.perez@example.com",
                ubicacionDesaparicion = "-16.5000,-68.1500" // Coordenadas en formato compatible con ReportarFragment
            )
            // Añade más ejemplos según sea necesario
        )

        // Configurar adaptador con listener para mostrar detalles
        adapter = PersonaAdapter(personas) { persona ->
            mostrarDetallePersona(persona)
        }
        recyclerView.adapter = adapter

        // Actualizar texto de resultados
        txtResultados.text = "Mostrando ${personas.size} registros"
    }

    private fun configurarBuscador() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { filtrarLista(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { filtrarLista(it) }
                return true
            }
        })
    }

    private fun filtrarLista(texto: String) {
        adapter.filtrar(texto)
        actualizarResultadosTexto(texto)
    }

    private fun actualizarResultadosTexto(texto: String) {
        val cantidadResultados = adapter.itemCount
        txtResultados.text = if (texto.isEmpty()) {
            "Mostrando todos los registros"
        } else {
            "Mostrando $cantidadResultados resultados para \"$texto\""
        }
    }

    private fun mostrarDetallePersona(persona: Persona) {
        val dialogFragment = DetallePersonaDialog.newInstance(persona)
        dialogFragment.show(parentFragmentManager, "DetallePersonaDialog")
    }
}