package com.torrezpillcokevin.nuna.ui.preguntasfrecuentes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.torrezpillcokevin.nuna.data.RetrofitInstance
import com.torrezpillcokevin.nuna.databinding.FragmentPreguntasFrecuentesBinding

class PreguntasFrecuentesFragment : Fragment() {

    private var _binding: FragmentPreguntasFrecuentesBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: PreguntasFrecuentesViewModel
    private lateinit var adapter: FaqAdapter

    private var currentPage = 1 // El backend de FastAPI suele empezar en 1
    private var isLastPage = false
    private var isLoading = false

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

        if (currentPage == 1) viewModel.getFaqs(currentPage, 10)
    }

    private fun setupRecyclerView() {
        adapter = FaqAdapter()
        binding.recyclerViewFaqs.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewFaqs.adapter = adapter

        binding.recyclerViewFaqs.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!isLoading && !isLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition >= 0) {
                        isLoading = true
                        currentPage++
                        viewModel.getFaqs(currentPage, 10)
                    }
                }
            }
        })
    }

    private fun observeViewModel() {
        viewModel.faqsResult.observe(viewLifecycleOwner) { result ->
            isLoading = false
            result.onSuccess { response ->
                adapter.submitList(response.data)
                // Si recibimos menos de 10, es que ya no hay más
                if (response.data.size < (currentPage * 10)) {
                    isLastPage = true
                }
            }.onFailure {
                Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
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