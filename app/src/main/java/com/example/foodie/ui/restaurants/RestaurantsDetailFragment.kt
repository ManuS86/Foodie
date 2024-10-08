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
import com.example.foodie.R
import com.example.foodie.adapter.ImageGalleryAdapter
import com.example.foodie.data.model.AppSettings
import com.example.foodie.data.model.DiscoverySettings
import com.example.foodie.data.model.Id
import com.example.foodie.databinding.FragmentRestaurantsDetailBinding
import com.example.foodie.ui.viewmodels.LocationViewModel
import com.example.foodie.ui.viewmodels.PlacesViewModel
import com.example.foodie.ui.viewmodels.UserViewModel
import com.example.foodie.utils.ListType
import com.example.foodie.utils.addIndicatorChip
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.model.Place
import java.text.DecimalFormat

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
            val restaurantId = Id(
                restaurant.name!!,
                restaurant.id!!,
                null
            )

            addLocationPermissionObserver()
            initializeMap(restaurant)
            if (restaurant.photoMetadatas != null) {
                val adapter = ImageGalleryAdapter(mutableListOf())
                val galleryChipGroup = binding.cpgGalleryPips
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

//                restaurantPhotoMetadata?.take(9).let { photoMetadatas ->
//                    photoMetadatas?.forEach {
//                        if (it != null) {
//                            addGalleryPip(galleryChipGroup, requireContext())
//                        }
//                    }
//                }

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
                    val decimalFormat = DecimalFormat("#.#")
                    val distanceInKm = distanceInMeters?.div(1000.0f)?.toDouble()
                    val formattedDistanceInKm = decimalFormat.format(distanceInKm)
                    "$formattedDistanceInKm Km away"
                } else {
                    val decimalFormat = DecimalFormat("#.#")
                    val distanceInMi = distanceInMeters?.let { it / 1609.34 }
                    val formattedDistanceInMi = decimalFormat.format(distanceInMi)
                    "$formattedDistanceInMi Mi away"
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

            binding.ivShareBtn.setOnClickListener {
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "text/plain"
                shareIntent.putExtra(Intent.EXTRA_TEXT, restaurant.websiteUri?.toString())
                startActivity(
                    Intent.createChooser(
                        shareIntent,
                        "Check out this restaurant: $restaurant."
                    )
                )
            }

            binding.fabNavigate.setOnClickListener {
                findNavController().navigate(R.id.navigationDetailFragment)
            }

            binding.fabNope.setOnClickListener {
                placesViewModel.nopes.value?.let {
                    if (!it.contains(restaurant)) {
                        placesViewModel.addRestaurantToLiveData(
                            ListType.NOPES,
                            restaurant
                        )
                    }
                }

                userViewModel.nopesIds.value?.let {
                    if (!it.contains(restaurantId)) {
                        userViewModel.nopesIds.value?.add(restaurantId)
                    }
                }

                restaurant.name?.let { restaurantName ->
                    userViewModel.saveRestaurant(
                        "nopes",
                        restaurantName,
                        restaurantId
                    )
                }

                findNavController().navigateUp()
            }

            binding.fabLike.setOnClickListener {
                placesViewModel.likes.value?.let {
                    if (!it.contains(restaurant)) {
                        placesViewModel.addRestaurantToLiveData(
                            ListType.LIKES,
                            restaurant
                        )
                    }
                }

                userViewModel.likesIds.value?.let {
                    if (!it.contains(restaurantId)) {
                        userViewModel.likesIds.value?.add(restaurantId)
                    }
                }

                restaurant.name?.let { restaurantName ->
                    userViewModel.saveRestaurant(
                        "likes",
                        restaurantName,
                        restaurantId
                    )
                }

                findNavController().navigateUp()
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

            restaurant.latLng?.let { latLng ->
                googleMap.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title(restaurant.name)
                        .icon(BitmapDescriptorFactory.defaultMarker(HUE_RED))
                )
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
            }
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