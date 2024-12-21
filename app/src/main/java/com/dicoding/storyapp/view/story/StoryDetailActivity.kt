package com.dicoding.storyapp.view.story

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.dicoding.storyapp.R
import com.dicoding.storyapp.databinding.ActivityStoryDetailBinding
import com.dicoding.storyapp.utils.formatDate
import com.dicoding.storyapp.view.ViewModelFactory
import com.dicoding.storyapp.view.main.MainViewModel

class StoryDetailActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels { ViewModelFactory.getInstance(this) }
    private lateinit var binding: ActivityStoryDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStoryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = resources.getString(R.string.detail)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val id = intent.getStringExtra(EXTRA_ID).toString()

        showLoading(true)
        viewModel.getSession().observe(this) { story ->
            val token = story.token
            viewModel.getDetailStory(token, id)
        }
        viewModel.detail.observe(this) {story->
            Glide.with(this@StoryDetailActivity)
                .load(story.photoUrl)
                .placeholder(R.drawable.ic_placeholder)
                .into(binding.ivItemPhoto)
            binding.tvDetailName.text = story.name
            binding.tvDetailDescription.text = story.description
            binding.createdAt.text = story.createdAt?.let { formatDate(it) }
            showLoading(false)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    companion object {
        const val EXTRA_ID = "extra_id"
    }
}
