package com.torrezpillcokevin.nuna.ui.soporte

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.torrezpillcokevin.nuna.R

class SoporteFragment : Fragment() {

    companion object {
        fun newInstance() = SoporteFragment()
    }

    private val viewModel: SoporteViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.fragment_soporte, container, false)
    }
}