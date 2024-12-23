package com.dicoding.storyapp.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dicoding.storyapp.R
import com.dicoding.storyapp.data.ListStory
import com.dicoding.storyapp.databinding.ItemStoryBinding
import com.dicoding.storyapp.utils.formatDate
import com.dicoding.storyapp.view.story.StoryDetailActivity

class StoryAdapter : PagingDataAdapter<ListStory, StoryAdapter.StoryViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val binding = ItemStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val story = getItem(position)
        if (story != null) {
            holder.bind(story)
        }
    }

    inner class StoryViewHolder(private val binding: ItemStoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(story: ListStory) {
            val context = binding.root.context
            binding.usernameTextView.text = story.name
            binding.description.text = story.description
            binding.timeCreated.text = context.getString(R.string.created_at,
                story.createdAt?.let { formatDate(it) })
            Glide.with(itemView)
                .load(story.photoUrl)
                .placeholder(R.drawable.ic_placeholder)
                .into(binding.previewImageView)
            binding.story.setOnClickListener {
                val intent = Intent(it.context, StoryDetailActivity::class.java)
                intent.putExtra(StoryDetailActivity.EXTRA_ID, story.id)
                itemView.context.startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(itemView.context as Activity).toBundle())
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ListStory>() {
            override fun areItemsTheSame(oldItem: ListStory, newItem: ListStory): Boolean {
                return oldItem == newItem
            }
            override fun areContentsTheSame(
                oldItem: ListStory,
                newItem: ListStory
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}
