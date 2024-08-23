package com.example.foodie.ui

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.foodie.MainViewModel
import com.example.foodie.R
import com.example.foodie.databinding.FragmentHistoryBinding

class HistoryFragment : Fragment() {
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var binding: FragmentHistoryBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel.checkLocationRequest(requireActivity())
        binding = FragmentHistoryBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.locationPermissionGranted.observe(viewLifecycleOwner) { granted ->
            if (granted) {
                // Permissions granted, start location tracking
            } else {
                findNavController().navigate(
                    R.id.locationPermissionFragment
                )
            }
        }
    }
}