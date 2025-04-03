package com.torrezpillcokevin.nuna.ui.muro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.torrezpillcokevin.nuna.R
import com.torrezpillcokevin.nuna.models.Persona
import com.torrezpillcokevin.nuna.ui.muro.adapter.PersonaAdapter

class MuroFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PersonaAdapter
    private lateinit var searchView: SearchView

    private val listaPersonas = mutableListOf(
        Persona("Juan", "Pérez", 30, "Masculino", "Desapareció en la zona centro.",
            "15/05/1994", "01/04/2025", "Ciudad de México", "En investigación",
            R.drawable.adultos, "1.75m, cabello corto negro, lunar en la mejilla.", "2 días desaparecido"),

        Persona("María", "López", 25, "Femenino", "Última vez vista en un parque.",
            "20/08/1999", "25/03/2025", "Guadalajara", "Caso abierto",
            R.drawable.adultos, "1.60m, tatuaje en el brazo derecho.", "1 semana desaparecida"),

        Persona("Carlos", "Gómez", 40, "Masculino", "Salió de casa y no regresó.",
            "10/10/1984", "30/03/2025", "Monterrey", "Buscado activamente",
            R.drawable.adultos, "1.80m, barba y cicatriz en la ceja.", "3 días desaparecido"),

        Persona("Ana", "Torres", 28, "Femenino", "Viajaba a su trabajo y desapareció.",
            "05/07/1996", "02/04/2025", "Puebla", "Testigos afirman verla en autobús",
            R.drawable.adultos, "1.65m, cabello castaño, vestía blusa blanca.", "5 horas desaparecida"),

        Persona("Luis", "Ramírez", 35, "Masculino", "Se le vio por última vez en un bar.",
            "12/06/1989", "22/03/2025", "Querétaro", "Investigación preliminar",
            R.drawable.adultos, "1.72m, usa gafas, cojea de la pierna derecha.", "10 días desaparecido"),

        Persona("Sofía", "Castro", 22, "Femenino", "Desapareció después de una fiesta.",
            "03/04/2002", "27/03/2025", "Tijuana", "Investigación en progreso",
            R.drawable.adultos, "1.70m, cabello largo y oscuro.", "4 semanas desaparecida"),

        Persona("Fernando", "Ruiz", 45, "Masculino", "No se sabe nada desde que salió de su trabajo.",
            "30/08/1979", "01/04/2025", "León", "Caso activo",
            R.drawable.adultos, "1.80m, calvo, usa gafas.", "6 horas desaparecido"),

        Persona("Gabriela", "Mendoza", 29, "Femenino", "Desapareció mientras viajaba a la ciudad.",
            "11/12/1995", "15/03/2025", "Aguascalientes", "Caso bajo investigación",
            R.drawable.adultos, "1.65m, cabello castaño claro, ojos verdes.", "2 semanas desaparecida"),

        Persona("Diego", "Fernández", 37, "Masculino", "Desapareció cerca de su casa.",
            "22/03/1987", "10/03/2025", "Cancún", "Buscado activamente",
            R.drawable.adultos, "1.78m, barba corta, tatuaje en el brazo izquierdo.", "5 días desaparecido")
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_muro, container, false)

        recyclerView = view.findViewById(R.id.recyclerPersonas)
        searchView = view.findViewById(R.id.searchView)


        recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        adapter = PersonaAdapter(listaPersonas) { persona ->
            abrirDetalle(persona)
        }
        recyclerView.adapter = adapter


        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filtrar(newText.orEmpty())
                return true
            }
        })

        return view
    }

    private fun abrirDetalle(persona: Persona) {
        val dialog = DetallePersonaDialog.newInstance(persona)
        dialog.show(parentFragmentManager, "DetallePersonaDialog")
    }
}
