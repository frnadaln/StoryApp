package com.dicoding.storyapp.view.maps


import androidx.lifecycle.ViewModel
import com.dicoding.storyapp.repository.Repository

class MapsViewModel(
    private val repository: Repository
) : ViewModel() {
    fun getStoriesWithLocation() = repository.getStoriesWithLocation(location = 1)
}
