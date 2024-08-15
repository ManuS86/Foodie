package com.example.foodie.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.foodie.data.model.Restaurant
import com.example.foodie.databinding.ItemLikesBinding

class LikesAdapter(
    private val dataset: List<Restaurant>
) : RecyclerView.Adapter<LikesAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(val binding: ItemLikesBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemLikesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val restaurant = dataset[position]

    }

    override fun getItemCount(): Int {
        return dataset.size
    }
}