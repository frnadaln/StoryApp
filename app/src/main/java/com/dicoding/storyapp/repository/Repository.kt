package com.dicoding.storyapp.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.dicoding.storyapp.api.ApiService
import com.dicoding.storyapp.data.DetailResponse
import com.dicoding.storyapp.data.ListStory
import com.dicoding.storyapp.data.LoginResponse
import com.dicoding.storyapp.data.Story
import com.dicoding.storyapp.data.StoryResponse
import com.dicoding.storyapp.data.UpResponse
import com.dicoding.storyapp.data.Error
import com.dicoding.storyapp.data.RemoteMediator
import com.dicoding.storyapp.data.database.StoryDatabase
import com.dicoding.storyapp.data.pref.UserModel
import com.dicoding.storyapp.data.pref.UserPreference
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import java.io.File
import com.dicoding.storyapp.di.Result
import java.io.IOException

class Repository(
    private val storyDatabase: StoryDatabase,
    private val apiService : ApiService,
    private val preference: UserPreference
) {
    private val _listStories = MutableLiveData<List<ListStory>>()
    val listStory: LiveData<List<ListStory>> = _listStories

    private val _detail = MutableLiveData<Story>()
    val detail: LiveData<Story> = _detail

    fun getSession(): Flow<UserModel> {
        return preference.getSession()
    }

    suspend fun saveSession(user: UserModel) {
        preference.saveSession(user.copy(isLogin = true))
    }

    fun signup(name: String, email: String, password: String) = liveData {
        emit(Result.Loading)
        try {
            val successResponse = apiService.signup(name, email, password)
            val message = successResponse.message
            emit(Result.Success(message))
        } catch (e: HttpException) {
            val errorMessage: String
            if (e.code() == 400) {
                errorMessage = "Email has been entered"
                emit(Result.Error(errorMessage))
            } else {
                val jsonInString = e.response()?.errorBody()?.string()
                val errorBody = Gson().fromJson(jsonInString, Error::class.java)
                errorMessage = errorBody.message.toString()
                emit(Result.Error(errorMessage))
            }
        }
    }

    fun login(email: String, password: String) = liveData {
        emit(Result.Loading)
        try {
            val successResponse = apiService.login(email, password)
            val data = successResponse.loginData.token
            emit(Result.Success(data))
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorBody, LoginResponse::class.java)
            emit(Result.Error(errorResponse.message))
        }
    }

    fun uploadImage(token: String, imageFile: File, description: String, lat: Float? = null, lon: Float? = null) : LiveData<Result<UpResponse>> = liveData {
        emit(Result.Loading)
        val requestBody = description.toRequestBody("text/plain".toMediaType())
        val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())
        val latReqData = lat?.toString()?.toRequestBody("text/plain".toMediaType())
        val lonReqData = lon?.toString()?.toRequestBody("text/plain".toMediaType())
        val multipartBody = MultipartBody.Part.createFormData("photo", imageFile.name, requestImageFile)
        try {
            val successResponse = apiService.upStory("Bearer $token", multipartBody, requestBody, latReqData, lonReqData)
            if (!successResponse.error!!) {
                emit(Result.Success(successResponse))
            } else {
                emit(Result.Error(successResponse.message ?: "Unknown Error"))
            }
        } catch (e: HttpException) {
            try {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorBody, UpResponse::class.java)
                errorResponse.message?.let { Result.Error(it) }?.let { emit(it) }
        } catch (e: Exception) {
            emit(Result.Error(e.message.toString()))
        }
            Log.e("Fail to add story", e.message().toString())
        } catch (e: IOException) {
            emit(Result.Error(e.message.toString()))
            Log.e("IOException", e.message.toString())
        }
    }

    suspend fun logout() {
        preference.logout()
        instance = null
    }

    fun getAllStories(token: String) {
        val client = apiService.getStory("Bearer $token")
        client.enqueue(object : Callback<StoryResponse> {
            override fun onResponse(call: Call<StoryResponse>, response: Response<StoryResponse>
            ) {
                if (response.isSuccessful) {
                    _listStories.value = response.body()?.listStory
                } else {
                    Log.e(TAG, "onFailure: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<StoryResponse>, t: Throwable) {
                Log.e(TAG, "onFailure: ${t.message.toString()}")
            }
        })
    }

    @OptIn(ExperimentalPagingApi::class)
    fun getAllStory() : LiveData<PagingData<ListStory>> {
        return Pager(
            config = PagingConfig(
                pageSize = 3,
            ),
            remoteMediator = RemoteMediator(storyDatabase, apiService),
            pagingSourceFactory = {
                storyDatabase.storyDao().getAllStory()
            }
        ).liveData
    }

    suspend fun getListStory() : List<ListStory> = apiService.getStories().listStory

    fun getStoriesWithLocation(location : Int = 1) : LiveData<Result<StoryResponse>> = liveData {
        emit(Result.Loading)
        try {
            val res = apiService.getStoriesWithLocation(location)

            emit(Result.Success(res))
        } catch (e : HttpException) {
            Log.e(StoryResponse::class.java.simpleName, e.message.toString())
            try {
                val errorRes = e.response()?.errorBody()?.string()
                val parseError = Gson().fromJson(errorRes, StoryResponse::class.java)
                emit(Result.Success(parseError))
            } catch (e : Exception) {
                emit(Result.Error("Error : ${e.message}"))
            }
        }
    }

    fun getDetailStory(token: String, id: String) {
        val client = apiService.detailStory("Bearer $token", id)
        client.enqueue(object : Callback<DetailResponse> {
            override fun onResponse(call: Call<DetailResponse>, response: Response<DetailResponse>
            ) {
                if (response.isSuccessful) {
                    _detail.value = response.body()?.story!!
                } else {
                    Log.e(TAG, "onFailure: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<DetailResponse>, t: Throwable) {
                Log.e(TAG, "onFailure: ${t.message.toString()}")
            }
        })
    }

    companion object {
        private const val TAG = "MainViewModel"
        @Volatile
        private var instance: Repository? = null
        fun getInstance(
            storyDatabase: StoryDatabase,
            apiService: ApiService,
            preference: UserPreference
        ) : Repository =
            instance ?: synchronized(this) {
                instance ?: Repository(storyDatabase, apiService, preference)
            }.also { instance = it }
    }
}
