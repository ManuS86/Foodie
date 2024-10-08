package com.example.foodie.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.foodie.data.model.Top5Item
import com.example.foodie.databinding.ItemStatsDetailBinding

class StatsDetailAdapter(
    private val dataset: List<Top5Item>
) : RecyclerView.Adapter<StatsDetailAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(val binding: ItemStatsDetailBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding =
            ItemStatsDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val top5Item = dataset[position]

    }

    override fun getItemCount(): Int {
        return dataset.size
    }
}