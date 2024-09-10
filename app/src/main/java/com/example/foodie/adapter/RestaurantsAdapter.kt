package com.example.foodie.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.foodie.LocationViewModel
import com.example.foodie.PlacesViewModel
import com.example.foodie.R
import com.example.foodie.UserViewModel
import com.example.foodie.addIndicatorChip
import com.example.foodie.data.model.DiscoverySettings
import com.example.foodie.databinding.ItemRestaurantBinding
import com.google.android.libraries.places.api.model.Place
import kotlin.math.roundToInt

class RestaurantsAdapter(
    private val context: Context,
    private var dataset: MutableList<Place>,
    private val locationViewModel: LocationViewModel,
    private val placesViewModel: PlacesViewModel,
    private val userViewModel: UserViewModel
) : RecyclerView.Adapter<RestaurantsAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(val binding: ItemRestaurantBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding =
            ItemRestaurantBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val restaurant = dataset[position]
        val appSettings = userViewModel.currentAppSettings.value
        val discoverySettings = userViewModel.currentDiscoverySettings.value

        val matchingCategories =
            userViewModel.firestoreRepository.foodCategories.filter { category ->
                restaurant.placeTypes!!.any { categoryString -> category.type == categoryString }
            }
        val restaurantLocation = Location("").apply {
            latitude = restaurant.latLng?.latitude!!
            longitude = restaurant.latLng?.longitude!!
        }
        val userLocation = locationViewModel.currentLocation.value
        val distanceInMeters = userLocation?.distanceTo(restaurantLocation)

        holder.binding.let { binding ->
            if (restaurant.photoMetadatas != null) {
                val adapter = ImageGalleryAdapter(mutableListOf())
                val restaurantPhotoMetadata = restaurant.photoMetadatas?.toMutableList()

                binding.vpGalleryRestaurant.adapter = adapter
                binding.btnImageLeft.setOnClickListener {
                    val currentItem = binding.vpGalleryRestaurant.currentItem
                    binding.vpGalleryRestaurant.currentItem = currentItem - 1
                }
                binding.btnImageRight.setOnClickListener {
                    if (adapter.itemCount > 0 && binding.vpGalleryRestaurant.currentItem != adapter.itemCount - 1) {
                        val currentItem = binding.vpGalleryRestaurant.currentItem
                        val nextItem = (currentItem + 1) % adapter.itemCount
                        binding.vpGalleryRestaurant.currentItem = nextItem
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
            binding.tvRestaurantName.text =
                restaurant.name!! +
                        when (restaurant.priceLevel?.toString()) {
                            "1" -> ", €"
                            "2" -> ", €€"
                            "3" -> ", €€€"
                            "4" -> ", €€€€"
                            else -> ""
                        }
            val chipGroup = binding.cpgCategoriesRestaurant
            chipGroup.removeAllViews()
            matchingCategories.forEach { category ->
                addIndicatorChip(
                    category.name,
                    chipGroup,
                    context,
                    discoverySettings ?: DiscoverySettings()
                )
            }
            binding.tvRating.text = restaurant.rating?.toString() ?: "n/a"
            binding.rbRating.rating = restaurant.rating?.toFloat() ?: 0f
            binding.tvRatingTotal.text = "(${restaurant.userRatingsTotal?.toString() ?: "0"})"
            binding.ivDecollapseButton.setOnClickListener {
                placesViewModel.setCurrentRestaurant(position)
                holder.itemView.findNavController().navigate(
                    R.id.restaurantDetailFragment
                )
            }

            binding.tvDistanceRestaurant.text =
                if (appSettings?.distanceUnit == "Km") {
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
        }
    }

    fun addRestaurants(restaurants: MutableList<Place>) {
        dataset = restaurants
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return dataset.size ?: 0
    }
}