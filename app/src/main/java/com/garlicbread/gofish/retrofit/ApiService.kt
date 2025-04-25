package com.garlicbread.gofish.retrofit

import WeatherResponse
import com.garlicbread.gofish.data.FirebaseTokenRequest
import com.garlicbread.gofish.data.FishDetails
import com.garlicbread.gofish.data.JwtResponse
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiService {

    @Multipart
    @POST("/fish/identify")
    fun identifyFish(@Part file: MultipartBody.Part): Call<FishDetails>

    @GET("weather")
    suspend fun getWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("timezone") timezone: String
    ): Response<WeatherResponse?>

    @GET("weather")
    suspend fun getWeather(
        @Query("location") location: String
    ): Response<WeatherResponse?>

    @POST("auth/login")
    suspend fun loginWithFirebaseToken(
        @Body tokenRequest: FirebaseTokenRequest
    ): Response<JwtResponse>
}