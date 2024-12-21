package com.dicoding.storyapp.data.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dicoding.storyapp.data.ListStory

@Dao
interface StoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story : List<ListStory>)

    @Query("SELECT * FROM story")
    fun getAllStory() : PagingSource<Int, ListStory>

    @Query("DELETE FROM story")
    suspend fun deleteAllStory()
}