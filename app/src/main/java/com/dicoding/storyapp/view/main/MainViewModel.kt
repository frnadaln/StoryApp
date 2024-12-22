package com.dicoding.storyapp.view.main

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dicoding.storyapp.data.ListStory
import com.dicoding.storyapp.data.pref.UserModel
import com.dicoding.storyapp.repository.Repository
import kotlinx.coroutines.launch
import java.io.File

class MainViewModel(private val repository: Repository) : ViewModel() {
    val story : LiveData<PagingData<ListStory>> =
        repository.getAllStory().cachedIn(viewModelScope)
    val listStory = repository.listStory
    val detail = repository.detail

    private var _curImage = MutableLiveData<Uri?>()
    val curImage : MutableLiveData<Uri?> = _curImage

    fun setCurImage(uri: Uri?) {
        _curImage.value = uri
    }

    fun signup(name: String, email: String, password: String) = repository.signup(name, email, password)
    fun login(email: String, password: String) = repository.login(email, password)
    fun getStories(token: String) = repository.getAllStories(token)
    fun getDetailStory(token: String, id: String) = repository.getDetailStory(token, id)
    fun uploadImage(token: String, file: File, description: String, lat: Float?, lon: Float?) = repository.uploadImage(token, file, description, lat, lon)

    fun saveSession(user: UserModel) {
        viewModelScope.launch {
            repository.saveSession(user)
        }
    }

    fun getSession(): LiveData<UserModel> {
        return repository.getSession().asLiveData()
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }
}
