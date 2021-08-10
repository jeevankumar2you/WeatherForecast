package com.nivedhana.weatherapp.Utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.util.*

class ConversionUtils {

    /**
     * Convert 300 Kelvin to degrees Celsius:
     * T(°C) = 300K - 273.15 = 26.85 °C
     */
    companion object {
        fun kelvinToCelsius(temp: Double): Int {
            return (temp - 273.15).toInt()
        }

        /* convert epoch time to hour min format */
        @RequiresApi(Build.VERSION_CODES.O)
        fun getDateTimeFromEpocSeconds(epoc: Long): String? {
            return try {
                val sdf = SimpleDateFormat("hh.mm a")
                val netDate = Date(epoc * 1000)
                sdf.format(netDate)
            } catch (e: Exception) {
                e.toString()
            }
        }

        /* convert epoch time to current date format */
        fun getCurDateTimeFromEpocSeconds(epoc: Long): String? {
            return try {
                val sdf = SimpleDateFormat("dd-MM-yyyy hh.mm a")
                val netDate = Date(epoc * 1000)
                sdf.format(netDate)
            } catch (e: Exception) {
                e.toString()
            }
        }
    }
}