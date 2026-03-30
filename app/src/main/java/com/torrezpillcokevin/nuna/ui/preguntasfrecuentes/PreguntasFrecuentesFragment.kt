package com.torrezpillcokevin.nuna.ui.preguntasfrecuentes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.torrezpillcokevin.nuna.data.RetrofitInstance
import com.torrezpillcokevin.nuna.databinding.FragmentPreguntasFrecuentesBinding

class PreguntasFrecuentesFragment : Fragment() {

    private var _binding: FragmentPreguntasFrecuentesBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: PreguntasFrecuentesViewModel
    private lateinit var adapter: FaqAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPreguntasFrecuentesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val factory = PreguntasFrecuentesViewModelFactory(requireActivity().application, RetrofitInstance.api)
        viewModel = ViewModelProvider(this, factory)[PreguntasFrecuentesViewModel::class.java]

        setupRecyclerView()
        observeViewModel()

        // Pedir los datos
        viewModel.getFaqs(1, 20)
    }

    private fun setupRecyclerView() {
        adapter = FaqAdapter()
        binding.recyclerViewFaqs.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewFaqs.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.faqsResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { response ->
                adapter.submitList(response.data)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}