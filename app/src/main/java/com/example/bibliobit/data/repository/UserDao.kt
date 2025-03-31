package com.example.bibliobit.data.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.bibliobit.data.model.LocalUser

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: LocalUser)

    @Query("SELECT * FROM users WHERE uid = :uid")
    suspend fun getUser(uid: String): LocalUser?
}