package com.example.bibliobit.data.repository

import com.example.bibliobit.data.remote.RemoteDataSource
import com.example.bibliobit.data.remote.response.StatisticResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatisticRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource
) {
    suspend fun getStatistics(filter: String): Result<StatisticResponse> {
        return try {
            val response = remoteDataSource.getStatistics(filter)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
