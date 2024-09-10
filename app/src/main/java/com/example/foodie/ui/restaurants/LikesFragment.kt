package com.example.foodie.ui.restaurants

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.foodie.R
import com.example.foodie.adapter.LikesAdapter
import com.example.foodie.databinding.FragmentLikesBinding
import com.example.foodie.ui.viewmodels.LocationViewModel
import com.example.foodie.ui.viewmodels.PlacesViewModel
import com.example.foodie.ui.viewmodels.UserViewModel

class LikesFragment : Fragment() {
    private val locationViewModel: LocationViewModel by activityViewModels()
    private val placesViewModel: PlacesViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()
    private lateinit var binding: FragmentLikesBinding
    private lateinit var likesAdapter: LikesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLikesBinding.inflate(inflater)

        locationViewModel.isGPSEnabled()
        locationViewModel.checkLocationPermission()

        if (userViewModel.likesIds.value != null) {
            placesViewModel.loadRestaurantById("likes", userViewModel.likesIds.value!!)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        likesAdapter = LikesAdapter(
            requireContext(),
            mutableListOf(),
            locationViewModel,
            placesViewModel,
            userViewModel,
            requireView()
        )

        binding.rvLikes.adapter = likesAdapter
        binding.rvLikes.hasFixedSize()

        addLocationPermissionObserver()
        addLikeObserverWithAdapter()

        binding.etSearch.addTextChangedListener { s ->
            likesAdapter.filterLikes(s.toString())
            if (s.isNullOrBlank()) {
                binding.ivCloseSearch.visibility = View.INVISIBLE
            } else {
                binding.ivCloseSearch.visibility = View.VISIBLE
                binding.ivCloseSearch.setOnClickListener {
                    binding.etSearch.setText("")
                }
            }
        }
    }

    private fun addLikeObserverWithAdapter() {
        placesViewModel.likes.observe(viewLifecycleOwner) { likes ->
            likesAdapter.addLikes(likes)
        }
    }

    private fun addLocationPermissionObserver() {
        locationViewModel.locationPermission.observe(viewLifecycleOwner) { granted ->
            if (granted == true) {
                // Permissions granted, start location tracking
                addGPSObserver()
            } else {
                findNavController().navigate(R.id.locationPermissionFragment)
            }
        }
    }

    private fun addGPSObserver() {
        locationViewModel.gpsProvider.observe(viewLifecycleOwner) { enabled ->
            if (enabled) {
                // GPS enabled
            } else {
                findNavController().navigate(R.id.noGpsFragment)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        locationViewModel.isGPSEnabled()
    }
}