package com.matule.myapplication.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.matule.myapplication.models.AppUser

@Database(
    entities = [AppUser::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(DateConverters::class)
abstract class AstronomyGuideDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var instance: AstronomyGuideDatabase? = null

        fun getInstance(context: Context): AstronomyGuideDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AstronomyGuideDatabase::class.java,
                    DATABASE_NAME
                ).build().also { instance = it }
            }
        }

        private const val DATABASE_NAME = "astronomy_guide.db"
    }
}
