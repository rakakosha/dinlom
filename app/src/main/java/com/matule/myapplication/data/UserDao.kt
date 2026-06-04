package com.matule.myapplication.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.matule.myapplication.models.AppUser

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: AppUser): Long

    @Query("SELECT * FROM users ORDER BY created_at DESC")
    suspend fun getAllUsers(): List<AppUser>

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE email = :email LIMIT 1)")
    suspend fun isEmailTaken(email: String): Boolean
}
