package com.example.bibliobit.data.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.bibliobit.data.model.LocalUser
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: LocalUser)

    @Query("SELECT * FROM users WHERE uid = :uid")
    suspend fun getUser(uid: String): LocalUser?

    @Query("SELECT * FROM users WHERE uid = :uid")
    fun getUserById(uid: String): Flow<LocalUser?>

    @Query("SELECT * FROM users WHERE isSynced = 0")
    suspend fun getUnsyncedUsers(): List<LocalUser>
}