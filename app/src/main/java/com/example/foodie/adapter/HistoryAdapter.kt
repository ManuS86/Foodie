package com.example.foodie.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.foodie.data.model.Restaurant
import com.example.foodie.databinding.ItemHistoryBinding

class HistoryAdapter(
    private val dataset: List<Restaurant>
) : RecyclerView.Adapter<HistoryAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(val binding: ItemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val restaurant = dataset[position]

    }

    override fun getItemCount(): Int {
        return dataset.size
    }
}