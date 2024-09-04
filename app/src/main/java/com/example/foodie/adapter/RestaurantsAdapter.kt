package com.example.foodie.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.foodie.NearbyRestaurantsViewModel
import com.example.foodie.R
import com.example.foodie.UserViewModel
import com.example.foodie.addIndicatorChip
import com.example.foodie.data.model.DiscoverySettings
import com.example.foodie.databinding.ItemRestaurantBinding
import com.google.android.libraries.places.api.model.Place

class RestaurantsAdapter(
    private val context: Context,
    private val dataset: List<Place>?,
    private val lifecycleOwner: LifecycleOwner,
    private val nearbyRestaurantsViewModel: NearbyRestaurantsViewModel,
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
        val restaurant = dataset?.get(position)
        val discoverySettings = userViewModel.currentDiscoverySettings.value
        val matchingCategories = userViewModel.repository.foodCategories.filter { category ->
            restaurant?.placeTypes!!.any { categoryString -> category.type == categoryString }
        }

        holder.binding.let { binding ->
            restaurant?.photoMetadatas?.take(1)?.forEach { photoMetadata ->
                nearbyRestaurantsViewModel.getPhoto(photoMetadata)
                    .observe(lifecycleOwner) { bitmap ->
                        // Update UI with the fetched photos
                        if (bitmap != null) {
                            binding.ivRestaurant.setImageBitmap(bitmap)
                        } else {
                            binding.ivRestaurant.setImageResource(R.drawable.placeholder_image)
                        }
                    }
            }
//            binding.tvAttributions.text = restaurant?.photoMetadatas?.get(0)?.authorAttributions.toString()
            if (restaurant != null) {
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
                matchingCategories.forEach { category ->
                    addIndicatorChip(category.name, chipGroup, context, discoverySettings ?: DiscoverySettings())
                }
                binding.tvRating.text = restaurant.rating?.toString() ?: "n/a"
                binding.rbRating.rating = restaurant.rating?.toFloat() ?: 0f
                binding.tvRatingTotal.text = "(${restaurant.userRatingsTotal?.toString() ?: "0"})"
                binding.tvOpenNow.text =
                    if (nearbyRestaurantsViewModel.isPlaceOpenNow(restaurant)) {
                        binding.tvOpenNow.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.open_green_dark
                            )
                        )
                        "Open"
                    } else {
                        binding.tvOpenNow.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.dismiss_red
                            )
                        )
                        "Closed"
                    }
                binding.ivDecollapseButton.setOnClickListener {
                    nearbyRestaurantsViewModel.setCurrentRestaurant(position)
                    holder.itemView.findNavController().navigate(
                        R.id.restaurantDetailFragment
                    )
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return dataset?.size ?: 0
    }
}