package com.rehelp.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.rehelp.app.databinding.FragmentPlaceholderBinding

// Temporary screen for tabs we build in later steps
class PlaceholderFragment : Fragment() {

    private var _binding: FragmentPlaceholderBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance(title: String, message: String) = PlaceholderFragment().apply {
            arguments = bundleOf("title" to title, "message" to message)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPlaceholderBinding.inflate(inflater, container, false)
        binding.tvTitle.text = arguments?.getString("title").orEmpty()
        binding.tvMessage.text = arguments?.getString("message").orEmpty()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
