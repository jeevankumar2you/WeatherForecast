package com.nivedhana.weatherapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.nivedhana.weatherapp.Jobservices.WeatherInfo
import com.nivedhana.weatherapp.Utils.ConversionUtils
import com.nivedhana.weatherapp.Utils.PermissionUtils
import com.nivedhana.weatherapp.config.Config
import com.nivedhana.weatherapp.model.Base
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class MainActivity : AppCompatActivity() {
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 999
        lateinit var lattitude: String
        lateinit var longitude: String
    }

    lateinit var progressDialog: ProgressDialog

    private lateinit var textviewCurrenttime: TextView
    private lateinit var textviewSunrise: TextView
    private lateinit var textViewSunset: TextView

    private lateinit var textviewTemp: TextView
    private lateinit var textViewMintemp: TextView
    private lateinit var textViewMaxtemp: TextView

    private lateinit var textViewHumidity: TextView
    private lateinit var textViewPressure: TextView

    private lateinit var editor: SharedPreferences.Editor
    private lateinit var sharedPreferences: SharedPreferences

    private var executed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        try {
            progressDialog = ProgressDialog(this@MainActivity)
            progressDialog.setTitle("Loading")
            progressDialog.setMessage("Please wait....")

            textviewCurrenttime = findViewById(R.id.textview_currenttime)
            textviewSunrise = findViewById(R.id.textview_sunrise_data)
            textViewSunset = findViewById(R.id.textview_sunset_data)

            textviewTemp = findViewById(R.id.textview_temp)
            textViewMintemp = findViewById(R.id.textview_mintemp)
            textViewMaxtemp = findViewById(R.id.textview_maxtemp)

            textViewHumidity = findViewById(R.id.textview_humidity)
            textViewPressure = findViewById(R.id.textview_pressure)

            sharedPreferences = this.getSharedPreferences(Config.WEATHERDETAILS, MODE_PRIVATE)
            editor = sharedPreferences.edit()

        } catch (e: java.lang.Exception) {
            Log.e("mainactivity ", "problem! in onCreate")
            Log.e("problem!", e.printStackTrace().toString())
        }
    }

    override fun onStart() {
        super.onStart()
        checkPermission()
    }

    /*
    * check weather location permission has been granted
    */
    private fun checkPermission() {
        when {
            PermissionUtils.isAccessFineLocationGranted(this) -> {
                when {
                    PermissionUtils.isLocationEnabled(this) -> {
                        setUpLocationListener()
                        progressDialog.show()
                    }
                    else -> {
                        PermissionUtils.showGPSNotEnabledDialog(this)
                    }
                }
            }
            else -> {
                PermissionUtils.requestAccessFineLocationPermission(
                    this,
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    /*
    * Requesting runtime location permission
    */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    when {
                        PermissionUtils.isLocationEnabled(this) -> {
                            setUpLocationListener()
                            progressDialog.show()
                        }
                        else -> {
                            PermissionUtils.showGPSNotEnabledDialog(this)
                        }
                    }
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.location_permission_not_granted),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    /*
    *   Requesting location updates
    */
    private fun setUpLocationListener() {
        try {
            val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
            // for getting the current location update after every 2 seconds with high accuracy
            val locationRequest = LocationRequest().setInterval(2000).setFastestInterval(2000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }

            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        super.onLocationResult(locationResult)
                        for (location in locationResult.locations) {
                            Log.i("lat ", location.latitude.toString())
                            Log.i("lon ", location.longitude.toString())

                            lattitude = location.latitude.toString()
                            longitude = location.longitude.toString()

                            editor.putString(Config.lattitude, lattitude)
                            editor.putString(Config.longitude, longitude)
                            editor.commit()

                            if (!executed) {
                                getweatherData(lattitude, longitude)
                                executed = true

                                val intent = Intent(applicationContext, WeatherInfo::class.java)
                                startService(intent)
                            } else {
                                progressDialog.dismiss()
                            }
                        }
                    }
                },
                Looper.myLooper()
            )
        } catch (e: java.lang.Exception) {
            Log.e("mainactivity ", "problem! in location listerner updates")
            Log.e("problem!", e.printStackTrace().toString())
        }
    }

    /*
    * request weather data
    */
    fun getweatherData(lat: String, lon: String) {
        val apiInterface =
            APIservice.create().getWeather(lat, lon, "5ad7218f2e11df834b0eaf3a33a39d2a")
        apiInterface.enqueue(object : Callback<Base> {
            override fun onFailure(call: Call<Base>?, t: Throwable?) {
                Log.d("onFailure ", "error $t")
                Toast.makeText(applicationContext, "Error message", Toast.LENGTH_LONG).show()
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<Base>, response: Response<Base>) {
                if (response.body() != null) {
                    progressDialog.dismiss()
                    Log.d("response ", response.body().toString())
                    updateUI(response)
                }
            }
        })
    }

    @SuppressLint("SetTextI18n", "CommitPrefEdits")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateUI(response: Response<Base>) {
        try {
            textviewCurrenttime.text =
                "Time : ${response.body()?.dt?.let(ConversionUtils.Companion::getCurDateTimeFromEpocSeconds)}"

            val tempeature = ConversionUtils.kelvinToCelsius(response.body()?.main?.temp!!)
            val mintemp = ConversionUtils.kelvinToCelsius(response.body()?.main?.temp_min!!)
            val maxtemp = ConversionUtils.kelvinToCelsius(response.body()?.main?.temp_max!!)
            val humidity = response.body()?.main?.humidity.toString()
            val pressure = response.body()?.main?.pressure.toString()
            val sunrise =
                response.body()?.sys?.sunrise?.let { ConversionUtils.getDateTimeFromEpocSeconds(it) }
            val sunset =
                response.body()?.sys?.sunset?.let { ConversionUtils.getDateTimeFromEpocSeconds(it) }

            editor = sharedPreferences.edit()
            editor.putString(Config.tempeature, tempeature.toString())
            editor.putString(Config.mintemp, mintemp.toString())
            editor.putString(Config.maxtemp, maxtemp.toString())
            editor.putString(Config.humidity, humidity)
            editor.putString(Config.pressure, pressure)
            editor.putString(Config.sunrise, sunrise)
            editor.putString(Config.sunset, sunset)
            editor.commit()
            editor.apply()

            textviewTemp.text = "${tempeature}℃"
            textViewMintemp.text = "Min ${mintemp}℃"
            textViewMaxtemp.text = "Max ${maxtemp}℃"

            textViewHumidity.text = "Humidity : ${humidity}%"
            textViewPressure.text = "Pressure : $pressure mBar"

            textviewSunrise.text =
                response.body()?.sys?.sunrise?.let { ConversionUtils.getDateTimeFromEpocSeconds(it) }
            textViewSunset.text =
                response.body()?.sys?.sunset?.let { ConversionUtils.getDateTimeFromEpocSeconds(it) }
        } catch (e: Exception) {
            Log.e("mainactivity ", "problem! in response processing")
            Log.e("problem!", e.printStackTrace().toString())
        }
    }
}