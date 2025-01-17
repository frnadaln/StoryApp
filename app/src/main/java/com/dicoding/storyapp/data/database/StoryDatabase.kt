package com.dicoding.storyapp.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dicoding.storyapp.data.ListStory

@Database(
    entities = [ListStory::class, RemoteKeys::class],
    version = 2,
    exportSchema = false
)
abstract class StoryDatabase : RoomDatabase() {
    abstract fun storyDao() : StoryDao
    abstract fun remoteDao() : RemoteDao

    companion object {
        @Volatile
        private var INSTANCE : StoryDatabase? = null

        @JvmStatic
        fun getInstance(context : Context) : StoryDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    StoryDatabase::class.java, "story_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
