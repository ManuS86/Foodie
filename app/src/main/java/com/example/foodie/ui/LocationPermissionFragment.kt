package com.example.foodie.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.foodie.MainViewModel
import com.example.foodie.R
import com.example.foodie.databinding.FragmentLocationPermissionBinding

class LocationPermissionFragment : Fragment() {
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var binding: FragmentLocationPermissionBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel.isGPSEnabled(requireContext())
        viewModel.checkLocationPermission(requireContext())
        binding = FragmentLocationPermissionBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.locationPermission.observe(viewLifecycleOwner) { granted ->
            if (granted == true) {
                // Permissions granted, start location tracking
                viewModel.gpsProvider.observe(viewLifecycleOwner) { enabled ->
                    if (enabled) {
                        // GPS enabled
                        findNavController().navigate(R.id.homeFragment)
                    } else {
                        findNavController().navigate(R.id.noGpsFragment)
                    }
                }
            }
        }

        binding.btnAllow.setOnClickListener {
            viewModel.nullLocationPermission()
            viewModel.handleLocationRequest(requireActivity())
            viewModel.locationPermission.observe(viewLifecycleOwner) { hasPermission ->
                if (hasPermission != null) {
                    if (hasPermission) {
                        findNavController().navigate(R.id.homeFragment)
                    } else {
                        findNavController().navigate(R.id.locationDeniedFragment)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.isGPSEnabled(requireContext())
        viewModel.checkLocationPermission(requireContext())
    }
}