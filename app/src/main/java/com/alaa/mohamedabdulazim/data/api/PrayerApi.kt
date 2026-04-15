package com.alaa.mohamedabdulazim.data.api

import com.alaa.mohamedabdulazim.data.models.PrayerApiResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PrayerApi {
    @GET("timings/{date}")
    suspend fun getPrayerTimes(
        @Path("date") date: String,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("method") method: Int = 5
    ): PrayerApiResponse
}
