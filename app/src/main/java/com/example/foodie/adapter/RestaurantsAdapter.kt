package com.example.foodie.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.foodie.databinding.ItemRestaurantBinding
import com.example.foodie.ui.restaurants.RestaurantsFragmentDirections
import com.google.android.libraries.places.api.model.Place

class RestaurantsAdapter(
    private val dataset: List<Place>?
) : RecyclerView.Adapter<RestaurantsAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(val binding: ItemRestaurantBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding =
            ItemRestaurantBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val restaurant = dataset?.get(position)

        holder.binding.let {
            if (restaurant != null) {
                it.tvRestaurantName.text = restaurant.name
                it.tvPriceRangeRestaurant.text = restaurant.priceLevel?.toString() ?: "N/A"
                it.tvRating.text = restaurant.rating?.toString() ?: "N/A"
                if (restaurant.rating != null) {
                    it.rbRating.rating = restaurant.rating?.toFloat()!!
                }
                it.tvRatingTotal.text = "(${restaurant.userRatingsTotal?.toString() ?: "0"})"
                it.ivDecollapseButton.setOnClickListener {
                    holder.itemView.findNavController().navigate(
                        RestaurantsFragmentDirections.actionRestaurantsFragmentToRestaurantDetailFragment()
                    )
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return dataset?.size ?: 0
    }
}