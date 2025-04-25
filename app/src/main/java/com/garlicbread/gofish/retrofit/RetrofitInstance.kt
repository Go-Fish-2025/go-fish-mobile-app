package com.garlicbread.gofish.retrofit

import android.content.Context
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

object RetrofitInstance {
    private const val BASE_URL = "http://192.168.1.105:5001"

    private lateinit var client: OkHttpClient
    private lateinit var retrofit: Retrofit

    fun init(context: Context) {
        client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(context))
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    val api: ApiService by lazy {
        if (!::retrofit.isInitialized) {
            throw IllegalStateException("RetrofitInstance not initialized. Call init(context) first.")
        }
        retrofit.create(ApiService::class.java)
    }
}