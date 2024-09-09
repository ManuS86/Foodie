package com.example.foodie.adapter

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.foodie.databinding.ItemImageGalleryBinding

class ImageGalleryAdapter(
    private val dataset: List<Bitmap>?
) : RecyclerView.Adapter<ImageGalleryAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(val binding: ItemImageGalleryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding =
            ItemImageGalleryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val image = dataset?.get(position)

        holder.binding.ivGallery.setImageBitmap(image)
    }

    override fun getItemCount(): Int {
        return dataset?.size ?: 0
    }
}