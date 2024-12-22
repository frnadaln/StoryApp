package com.dicoding.storyapp.data.pref

data class UserModel(
    val email: String,
    var password: String,
    val token: String,
    val isLogin: Boolean = false
)
