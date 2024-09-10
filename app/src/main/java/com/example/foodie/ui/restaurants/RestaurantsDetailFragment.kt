package com.example.foodie.ui.restaurants

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.foodie.LocationViewModel
import com.example.foodie.PlacesViewModel
import com.example.foodie.R
import com.example.foodie.UserViewModel
import com.example.foodie.adapter.ImageGalleryAdapter
import com.example.foodie.addIndicatorChip
import com.example.foodie.data.model.AppSettings
import com.example.foodie.data.model.DiscoverySettings
import com.example.foodie.data.model.Id
import com.example.foodie.databinding.FragmentRestaurantsDetailBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.model.Place
import kotlin.math.roundToInt

class RestaurantsDetailFragment : Fragment() {
    private val locationViewModel: LocationViewModel by activityViewModels()
    private val placesViewModel: PlacesViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()
    private lateinit var appSettings: AppSettings
    private lateinit var binding: FragmentRestaurantsDetailBinding
    private lateinit var discoverySettings: DiscoverySettings
    private lateinit var mapView: MapView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRestaurantsDetailBinding.inflate(inflater)
        appSettings = userViewModel.currentAppSettings.value ?: AppSettings()
        discoverySettings = userViewModel.currentDiscoverySettings.value ?: DiscoverySettings()
        mapView = binding.ivMapPreview
        mapView.onCreate(savedInstanceState)

        locationViewModel.isGPSEnabled()
        locationViewModel.checkLocationPermission()

        return binding.root
    }

    @SuppressLint("MissingPermission", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val restaurant = placesViewModel.currentRestaurant.value

        if (restaurant != null) {
            val chipGroup = binding.cpgRestaurantCategories
            val matchingCategories =
                userViewModel.firestoreRepository.foodCategories.filter { category ->
                    restaurant.placeTypes?.any { categoryString -> category.type == categoryString }
                        ?: false
                }
            val formattedOpeningWeekdays =
                restaurant.currentOpeningHours?.periods?.joinToString("\n") { period ->
                    period.open?.day.toString().let { day ->
                        day.substring(0, 1) + day.substring(1, 3).lowercase()
                    }
                } ?: ""
            val formattedOpeningHours =
                restaurant.currentOpeningHours?.periods?.joinToString("\n") { period ->
                    val openTime =
                        "%02d:%02d".format(period.open?.time?.hours, period.open?.time?.minutes)
                    val closeTime =
                        "%02d:%02d".format(period.close?.time?.hours, period.close?.time?.minutes)
                    "$openTime - $closeTime"
                } ?: "N/a"
            val restaurantLocation = Location("").apply {
                latitude = restaurant.latLng?.latitude!!
                longitude = restaurant.latLng?.longitude!!
            }
            val userLocation = locationViewModel.currentLocation.value
            val distanceInMeters = userLocation?.distanceTo(restaurantLocation)

            addLocationPermissionObserver()
            initializeMap(restaurant)
            if (restaurant.photoMetadatas != null) {
                val adapter = ImageGalleryAdapter(mutableListOf())
                val restaurantPhotoMetadata = restaurant.photoMetadatas?.toMutableList()

                binding.vpGalleryProfile.adapter = adapter
                binding.btnImageLeft.setOnClickListener {
                    val currentItem = binding.vpGalleryProfile.currentItem
                    binding.vpGalleryProfile.currentItem = currentItem - 1
                }
                binding.btnImageRight.setOnClickListener {
                    if (adapter.itemCount > 0 && binding.vpGalleryProfile.currentItem != adapter.itemCount - 1) {
                        val currentItem = binding.vpGalleryProfile.currentItem
                        val nextItem = (currentItem + 1) % adapter.itemCount
                        binding.vpGalleryProfile.currentItem = nextItem
                    }
                }

                restaurantPhotoMetadata?.removeAt(0)?.let { photoMetadata ->
                    placesViewModel.loadPhoto(photoMetadata) { photo ->
                        adapter.addFirstPhoto(photo)
                    }
                }

                restaurantPhotoMetadata?.take(8)?.let { photoMetadatas ->
                    placesViewModel.loadPhotoList(photoMetadatas) { photos ->
                        photos.forEach { photo ->
                            adapter.addPhoto(photo)
                        }
                    }
                }
            } else {
                binding.ivPlaceholder.setImageResource(R.drawable.placeholder_image)
            }
            binding.tvNameTitle.text = restaurant.name
            matchingCategories.forEach { category ->
                addIndicatorChip(
                    category.name,
                    chipGroup,
                    requireContext(),
                    discoverySettings
                )
            }
            binding.tvDistanceProfile.text =
                if (appSettings.distanceUnit == "Km") {
                    val distanceInKm = (distanceInMeters?.div(1000.0f))?.roundToInt()
                    if (distanceInKm!! < 1) {
                        "Less than 1 Km away"
                    } else {
                        "$distanceInKm Km away"
                    }
                } else {
                    val distanceInMi = (distanceInMeters?.div(621.371f))?.roundToInt()
                    if (distanceInMi!! < 1) {
                        "Less than 1 Mi away"
                    } else {
                        "$distanceInMi Mi away"
                    }
                }
            binding.tvTodaysHours.text =
                when (placesViewModel.isPlaceOpenNow(restaurant)) {
                    true -> {
                        binding.tvTodaysHours.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.open_green
                            )
                        )
                        "Open"
                    }

                    false -> {
                        binding.tvTodaysHours.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.open_red
                            )
                        )
                        "Closed"
                    }

                    null -> {
                        binding.tvTodaysHours.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.off_black
                            )
                        )
                        "Uknown"
                    }
                }

            binding.tvWeekdays.text = formattedOpeningWeekdays
            binding.tvTimes.text = formattedOpeningHours
            binding.tvRating.text = restaurant.rating?.toString() ?: "n/a"
            binding.rbRating.rating = restaurant.rating?.toFloat() ?: 0f
            binding.tvRatingsTotal.text = "(${restaurant.userRatingsTotal?.toString() ?: "0"})"
            binding.tvPriceLevelProfile.text =
                when (restaurant.priceLevel?.toString()) {
                    "1" -> "€"
                    "2" -> "€€"
                    "3" -> "€€€"
                    "4" -> "€€€€"
                    else -> "N/A"
                }

            binding.tvAddress.text = restaurant.address
            if (restaurant.websiteUri != null) {
                binding.cvWebsite.visibility = View.VISIBLE
                binding.btnWebsite.setOnClickListener {
                    val uri = Uri.parse(restaurant.websiteUri?.toString())
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                }
            } else {
                binding.cvWebsite.visibility = View.GONE
            }

            binding.ivDecollapse.setOnClickListener {
                findNavController().navigateUp()
            }

            binding.fabNavigate.setOnClickListener {
                findNavController().navigate(R.id.navigationDetailFragment)
            }

            binding.fabNope.setOnClickListener {
                userViewModel.saveRestaurant(
                    "nopes",
                    restaurant.name!!,
                    Id(restaurant.name!!, restaurant.id!!, null)
                )
            }

            binding.fabLike.setOnClickListener {
                userViewModel.saveRestaurant(
                    "likes",
                    restaurant.name!!,
                    Id(restaurant.name!!, restaurant.id!!, null)
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun initializeMap(restaurant: Place) {
        mapView.getMapAsync { googleMap ->
            googleMap.isMyLocationEnabled = true
            googleMap.uiSettings.isCompassEnabled = true
            googleMap.uiSettings.isMyLocationButtonEnabled = false
            googleMap.uiSettings.setAllGesturesEnabled(true)
            googleMap.addMarker(
                MarkerOptions()
                    .position(restaurant.latLng!!)
                    .title(restaurant.name)
                    .icon(BitmapDescriptorFactory.defaultMarker(HUE_RED))
            )
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(restaurant.latLng!!, 15f))
        }
        mapView.setOnClickListener {
            findNavController().navigate(R.id.navigationDetailFragment)
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

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        locationViewModel.isGPSEnabled()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}