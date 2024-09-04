package com.example.foodie.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.foodie.NearbyRestaurantsViewModel
import com.example.foodie.R
import com.example.foodie.UserViewModel
import com.example.foodie.addIndicatorChip
import com.example.foodie.data.model.DiscoverySettings
import com.example.foodie.databinding.ItemLikesBinding
import com.google.android.libraries.places.api.model.Place

class LikesAdapter(
    private val context: Context,
    private val dataset: List<Place>?,
    private val lifecycleOwner: LifecycleOwner,
    private val nearbyRestaurantsViewModel: NearbyRestaurantsViewModel,
    private val userViewModel: UserViewModel
) : RecyclerView.Adapter<LikesAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(val binding: ItemLikesBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemLikesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val restaurant = dataset?.get(position)
        val discoverySettings = userViewModel.currentDiscoverySettings.value
        val matchingCategories = userViewModel.repository.foodCategories.filter { category ->
            restaurant?.placeTypes!!.any { categoryString -> category.type == categoryString }
        }
        val photoMetadata = restaurant?.photoMetadatas?.get(0)

        if (restaurant != null) {
            holder.binding.let { binding ->
                if (photoMetadata != null) {
                    nearbyRestaurantsViewModel.getPhoto(photoMetadata)
                        .observe(lifecycleOwner) { bitmap ->
                            if (bitmap != null) {
                                binding.ivRestaurantPicLikes.setImageBitmap(bitmap)
                            } else {
                                binding.ivRestaurantPicLikes.setImageResource(R.drawable.placeholder_image)
                            }
                        }
                }
                binding.tvRestaurantNameLikes.text = restaurant.name
                val chipGroup = binding.cpgCategoriesLikes
                matchingCategories.forEach { category ->
                    addIndicatorChip(category.name, chipGroup, context, discoverySettings ?: DiscoverySettings())
                }

                binding.cvLikes.setOnClickListener {
                    nearbyRestaurantsViewModel.setCurrentRestaurant(position)
                    holder.itemView.findNavController().navigate(
                        R.id.restaurantDetailFragment
                    )
                }

                binding.ivApproveLikes.setOnClickListener {
                    userViewModel.likes.value?.remove(restaurant)
                    userViewModel.history.value?.add(restaurant)
                    userViewModel.addNewRestaurant("history", restaurant.name ?: "", restaurant)
                }

                binding.ivDismissLikes.setOnClickListener {
                    userViewModel.likes.value?.remove(restaurant)
                    userViewModel.deleteLikedRestaurant(restaurant.name ?: "")
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return dataset?.size ?: 0
    }
}