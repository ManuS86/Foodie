package com.example.foodie.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.foodie.MainViewModel
import com.example.foodie.R
import com.example.foodie.databinding.FragmentLocationDeniedBinding

class LocationDeniedFragment : Fragment() {
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var binding: FragmentLocationDeniedBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel.checkLocationRequest(requireActivity())
        binding = FragmentLocationDeniedBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnGoToSettings.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val uri = Uri.parse("package:com.example.foodie")
            intent.data = uri
            startActivity(intent)
        }

        viewModel.locationPermissionGranted.observe(viewLifecycleOwner) { granted ->
            if (granted) {
                // Permissions granted, start location tracking
                findNavController().navigate(
                    R.id.homeFragment
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkLocationRequest(requireActivity())
    }
}