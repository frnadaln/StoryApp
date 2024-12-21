package com.dicoding.storyapp.di

import android.content.Context
import com.dicoding.storyapp.api.ApiConfig
import com.dicoding.storyapp.data.database.StoryDatabase
import com.dicoding.storyapp.data.pref.UserPreference
import com.dicoding.storyapp.data.pref.dataStore
import com.dicoding.storyapp.repository.Repository

object Injection {
    fun provideRepository(context: Context): Repository {
        val pref = UserPreference.getInstance(context.dataStore)
        val apiService = ApiConfig.getApiService()
        val storyDatabase = StoryDatabase.getInstance(context)
        return Repository.getInstance(storyDatabase, apiService, pref)
    }
}
