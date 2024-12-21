package com.dicoding.storyapp.di

import androidx.recyclerview.widget.DiffUtil
import com.dicoding.storyapp.data.ListStory

class DiffCallback(
    private val oldList: List<ListStory>,
    private val newList: List<ListStory>
) : DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size
}
