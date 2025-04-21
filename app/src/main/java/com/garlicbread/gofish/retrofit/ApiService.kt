package com.garlicbread.gofish.retrofit

import com.garlicbread.gofish.data.FishDetails
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {

    @Multipart
    @POST("/fish/identify")
    fun identifyFish(@Part file: MultipartBody.Part): Call<FishDetails>
}