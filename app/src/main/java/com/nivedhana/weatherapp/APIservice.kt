package com.nivedhana.weatherapp

import com.nivedhana.weatherapp.model.Base
import com.nivedhana.weatherapp.config.Config
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface APIservice {
    companion object {
        private var BASEURL = Config.weatherURL
        fun create(): APIservice {
            val retrofit =
                Retrofit.Builder().addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(BASEURL)
                    .build()
            return retrofit.create(APIservice::class.java)
        }
    }

    @GET("weather")
    fun getWeather(
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("appid") appid: String
    ): Call<Base>
}