package com.example.foodie.ui.permissions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.foodie.MainViewModel
import com.example.foodie.R
import com.example.foodie.databinding.FragmentNoGpsBinding

class NoGpsFragment : Fragment() {
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var binding: FragmentNoGpsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel.checkLocationPermission(requireContext())
        binding = FragmentNoGpsBinding.inflate(inflater)
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
                    }
                }
            } else {
                findNavController().navigate(R.id.locationPermissionFragment)
            }
        }

        binding.btnTryAgain.setOnClickListener {
            viewModel.isGPSEnabled(requireContext())
            if (viewModel.gpsProvider.value == true) {
                findNavController().navigate(R.id.homeFragment)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.isGPSEnabled(requireContext())
        viewModel.checkLocationPermission(requireContext())
    }
}