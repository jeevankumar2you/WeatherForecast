package com.nivedhana.weatherapp.config

class Config {
    companion object {
        var weatherURL =
            "http://api.openweathermap.org/data/2.5/"
        var WEATHERDETAILS = "weatherDetails"
        val API_ID: String = "5ad7218f2e11df834b0eaf3a33a39d2a"

        const val time = "time"
        const val tempeature = "temperature"
        const val mintemp = "mintemp"
        const val maxtemp = "maxtemp"
        const val humidity = "humidity"
        const val pressure = "pressure"
        const val sunrise = "sunrise"
        const val sunset = "sunset"

        const val lattitude = "lattitude"
        const val longitude = "longitude"

        const val TASK_ALREADY_EXECUTED = "taskalreadyexecuted"
    }
}