package com.nivedhana.weatherapp.Jobservices

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.nivedhana.weatherapp.APIservice
import com.nivedhana.weatherapp.MainActivity
import com.nivedhana.weatherapp.Utils.ConversionUtils
import com.nivedhana.weatherapp.config.Config
import com.nivedhana.weatherapp.model.Base
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WeatherInfo : Service() {
    lateinit var mainHandler: Handler

    private lateinit var editor: SharedPreferences.Editor
    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        val REQUEST_INTERVAL: Long = 30000
    }

    private lateinit var mWifi: NetworkInfo

    override fun onCreate() {
        super.onCreate()
        // Toast.makeText(applicationContext, "service onCreate()", Toast.LENGTH_SHORT).show()
        sharedPreferences = this.getSharedPreferences(Config.WEATHERDETAILS, MODE_PRIVATE)

        mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post(updateTextTask)

        val connManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)!!
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Toast.makeText(applicationContext, "service started", Toast.LENGTH_SHORT).show()
        return START_STICKY
    }

    /* run task every 30sec in background */
    private val updateTextTask = object : Runnable {
        override fun run() {
            try {
                mainHandler.postDelayed(this, REQUEST_INTERVAL)
                Log.i(
                    "Request done ",
                    "weather requested in background every 10sec once for testing purpose not for 2hrs"
                )
                // Toast.makeText(applicationContext, "weather requested in background every 10sec once for testing purpose not for 2hrs", Toast.LENGTH_SHORT).show()

                /*
                * latest lattitude and longitude stored in shared preferences will be used for Request
                */
                val lattitude: String =
                    sharedPreferences.getString(Config.lattitude, null).toString()
                val longitude: String =
                    sharedPreferences.getString(Config.longitude, null).toString()
                getweatherData(lattitude, longitude)

                /*if (mWifi.isConnected) {
                    getweatherData(MainActivity.lattitude, MainActivity.longitude)
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Sorry! can't fetch weather deatails",
                        Toast.LENGTH_SHORT
                    ).show()
                }*/
            } catch (e: Exception) {
                Log.e("WeatherInfo ", "problem! in Job scheduler")
                Log.e("problem!", e.printStackTrace().toString())
            }
        }
    }

    /*
    * request weather data
    */
    fun getweatherData(lat: String, lon: String) {
        Log.i("lat - lon", "$lat - $lon")
        val apiInterface =
            APIservice.create().getWeather(lat, lon, Config.API_ID)

        apiInterface.enqueue(object : Callback<Base> {
            override fun onFailure(call: Call<Base>?, t: Throwable?) {
                Log.d("onFailure ", t?.message.toString())
                Toast.makeText(applicationContext, "request failed", Toast.LENGTH_SHORT)
                    .show()
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<Base>, response: Response<Base>) {
                if (response.body() != null) {
                    Log.d("success response", response.body().toString())
                    storeDataToCache(response)
                    // Toast.makeText(applicationContext, "Background response successful and stored to cache", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    /*
    * on succcess response the necessary data is stored to cache
    */
    @RequiresApi(Build.VERSION_CODES.O)
    fun storeDataToCache(response: Response<Base>) {
        val time =
            response.body()?.dt?.let(ConversionUtils.Companion::getCurDateTimeFromEpocSeconds)
        val tempeature = ConversionUtils.kelvinToCelsius(response.body()?.main?.temp!!)
        val mintemp = ConversionUtils.kelvinToCelsius(response.body()?.main?.temp_min!!)
        val maxtemp = ConversionUtils.kelvinToCelsius(response.body()?.main?.temp_max!!)
        val humidity = response.body()?.main?.humidity.toString()
        val pressure = response.body()?.main?.pressure.toString()
        val sunrise =
            response.body()?.sys?.sunrise?.let { ConversionUtils.getDateTimeFromEpocSeconds(it) }
        val sunset =
            response.body()?.sys?.sunset?.let { ConversionUtils.getDateTimeFromEpocSeconds(it) }

        /* storing data to shared preferences */
        editor = sharedPreferences.edit()
        editor.putString(Config.time, time)
        editor.putString(Config.tempeature, tempeature.toString())
        editor.putString(Config.mintemp, mintemp.toString())
        editor.putString(Config.maxtemp, maxtemp.toString())
        editor.putString(Config.humidity, humidity)
        editor.putString(Config.pressure, pressure)
        editor.putString(Config.sunrise, sunrise)
        editor.putString(Config.sunset, sunset)
        editor.commit()
        editor.apply()

        Log.i("shared-time ", sharedPreferences.getString(Config.time, null).toString())
        Log.i("shared-temp ", sharedPreferences.getString(Config.tempeature, null).toString())
        Log.i("shared-mintemp ", sharedPreferences.getString(Config.mintemp, null).toString())
        Log.i("shared-maxtemp ", sharedPreferences.getString(Config.maxtemp, null).toString())
        Log.i("shared-humidity ", sharedPreferences.getString(Config.humidity, null).toString())
        Log.i("shared-pressure ", sharedPreferences.getString(Config.pressure, null).toString())
        Log.i("shared-sunrise ", sharedPreferences.getString(Config.sunrise, null).toString())
        Log.i("shared-sunset ", sharedPreferences.getString(Config.sunset, null).toString())
    }
}