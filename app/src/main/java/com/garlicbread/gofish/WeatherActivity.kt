package com.garlicbread.gofish

import WeatherResponse
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.garlicbread.gofish.adapter.DailyForecastAdapter
import com.garlicbread.gofish.adapter.HourlyForecastAdapter
import com.garlicbread.gofish.constants.Constants
import com.garlicbread.gofish.data.toCoordinates
import com.garlicbread.gofish.data.toCurrentWeather
import com.garlicbread.gofish.data.toDailyForecast
import com.garlicbread.gofish.data.toHourlyForecast
import com.garlicbread.gofish.data.toTodaySummary
import com.garlicbread.gofish.retrofit.RetrofitInstance
import com.garlicbread.gofish.room.GoFishDatabase
import com.garlicbread.gofish.room.entity.CurrentWeatherEntity
import com.garlicbread.gofish.room.entity.DailyForecastEntity
import com.garlicbread.gofish.room.entity.HourlyForecastEntity
import com.garlicbread.gofish.room.entity.WeatherEntity
import com.garlicbread.gofish.room.repository.WeatherRepository
import com.garlicbread.gofish.util.Utils
import com.garlicbread.gofish.util.Utils.Companion.formatPrecipitation
import com.garlicbread.gofish.util.Utils.Companion.formatTemperature
import com.garlicbread.gofish.util.Utils.Companion.formatTime
import com.garlicbread.gofish.util.Utils.Companion.formatWind
import com.garlicbread.gofish.util.Utils.Companion.logout
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.TimeZone

@RequiresApi(Build.VERSION_CODES.O)
class WeatherActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var weatherRepository: WeatherRepository

    private lateinit var dailyRecyclerView: RecyclerView
    private lateinit var hourlyRecyclerView: RecyclerView

    private lateinit var dailyAdapter: DailyForecastAdapter
    private lateinit var hourlyAdapter: HourlyForecastAdapter

    private lateinit var locationPermissionLauncher: ActivityResultLauncher<String>

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)

        val weatherDao = GoFishDatabase.getDatabase(applicationContext).weatherDao()
        weatherRepository = WeatherRepository(weatherDao)

        val sharedPref = getSharedPreferences(Constants.SHARED_PREF_TAG, MODE_PRIVATE)
        val lastLat = sharedPref.getString(Constants.LAST_KNOWN_LAT, null)
        val lastLng = sharedPref.getString(Constants.LAST_KNOWN_LNG, null)

        dailyRecyclerView = findViewById(R.id.daily_forecast)
        dailyRecyclerView.layoutManager = LinearLayoutManager(this)

        hourlyRecyclerView = findViewById<RecyclerView>(R.id.hourly_forecast)
        hourlyRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        dailyAdapter = DailyForecastAdapter(emptyList(), resources)
        dailyRecyclerView.adapter = dailyAdapter

        hourlyAdapter = HourlyForecastAdapter(emptyList(), resources)
        hourlyRecyclerView.adapter = hourlyAdapter

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val cityEditText = findViewById<EditText>(R.id.city)

        cityEditText.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (cityEditText.text.trim().isNotEmpty()) fetchWeather(cityEditText.text.trim().toString())
                cityEditText.setText("")
                cityEditText.clearFocus()
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
                true
            } else {
                false
            }
        }


        findViewById<TextView>(R.id.hourly_forecast_title).setOnClickListener {
            hourlyRecyclerView.isVisible = !hourlyRecyclerView.isVisible
            if (hourlyRecyclerView.isVisible) {
                findViewById<ImageView>(R.id.collapse_hourly).setImageDrawable(resources.getDrawable(R.drawable.collapse_up, null))
            }
            else {
                findViewById<ImageView>(R.id.collapse_hourly).setImageDrawable(resources.getDrawable(R.drawable.collapse_down, null))
            }
        }

        findViewById<TextView>(R.id.daily_forecast_title).setOnClickListener {
            dailyRecyclerView.isVisible = !dailyRecyclerView.isVisible
            if (dailyRecyclerView.isVisible) {
                findViewById<ImageView>(R.id.collapse_daily).setImageDrawable(resources.getDrawable(R.drawable.collapse_up, null))
            }
            else {
                findViewById<ImageView>(R.id.collapse_daily).setImageDrawable(resources.getDrawable(R.drawable.collapse_down, null))
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            getCurrentLocation(sharedPref, lastLat, lastLng)
        }

        locationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                getCurrentLocation(sharedPref, lastLat, lastLng)
            } else {
                Toast.makeText(this, "Location access denied! Allow it in Settings.", Toast.LENGTH_SHORT).show()
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun getCurrentLocation(sharedPref: android.content.SharedPreferences, lastLat: String?, lastLng: String?) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener(this) { location ->
                    if (location != null) {
                        val latitude: Double = location.latitude
                        val longitude: Double = location.longitude

                        with(sharedPref.edit()) {
                            putString(Constants.LAST_KNOWN_LAT, longitude.toString())
                            putString(Constants.LAST_KNOWN_LNG, latitude.toString())
                            apply()
                        }

                        fetchWeather(latitude, longitude)
                    } else {
                        Log.d("Location", "Location is null")
                    }
                }
                .addOnFailureListener(this) { e ->
                    Log.e("Location", "Error getting location", e)
                    if (lastLat != null && lastLng != null) {
                        fetchWeather(lastLat.toDouble(), lastLng.toDouble(), true)
                    } else {
                        Toast.makeText(this@WeatherActivity, "Failed to fetch current location !!", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    private fun fetchWeather(latitude: Double, longitude: Double, lastKnown: Boolean = false) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val rawResponse = RetrofitInstance.api.getWeather(latitude, longitude, TimeZone.getDefault().id)

                if (rawResponse.code() == 401) {
                    GoFishDatabase.getDatabase(this@WeatherActivity).clearAllTables()
                    withContext(Dispatchers.Main) {
                        logout(this@WeatherActivity)
                    }
                    return@launch
                }

                val response = rawResponse.body()
                response?.let {
                    weatherRepository.saveWeatherData(it)
                    withContext(Dispatchers.Main) {
                        populateWeatherDetails(response, lastKnown, false)
                    }
                } ?: fallback(latitude, longitude, null, lastKnown)

            } catch (_: Exception) {
                fallback(latitude, longitude, null, lastKnown)
            }
        }
    }

    private fun fetchWeather(location: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val rawResponse = RetrofitInstance.api.getWeather(location)

                if (rawResponse.code() == 401) {
                    withContext(Dispatchers.Main) {
                        logout(this@WeatherActivity)
                    }
                    return@launch
                }

                val response = rawResponse.body()
                response?.let {
                    weatherRepository.saveWeatherData(it)
                    withContext(Dispatchers.Main) {
                        populateWeatherDetails(response, false, false)
                    }
                } ?: fallback(null, null, location, false)

            } catch (_: Exception) {
                fallback(null, null, location, false)
            }
        }
    }

    private suspend fun fallback(latitude: Double?, longitude: Double?, location: String?, lastKnown: Boolean) {
        var weatherEntity: WeatherEntity? = null
        if (latitude != null && longitude != null) {
            weatherEntity = weatherRepository.getWeather(latitude, longitude)
        }
        else {
            weatherEntity = weatherRepository.getWeather(location!!)
        }

        if (weatherEntity != null) {
            val weatherResponse = buildWeatherResponse(
                weatherEntity,
                weatherRepository.getCurrentWeather(weatherEntity.latitude, weatherEntity.longitude)!!,
                weatherRepository.getDailyEntities(weatherEntity.latitude, weatherEntity.longitude),
                weatherRepository.getHourlyEntities(weatherEntity.latitude, weatherEntity.longitude),
            )
            withContext(Dispatchers.Main) {
                populateWeatherDetails(weatherResponse, lastKnown, true)
            }
        }
        else {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@WeatherActivity, "Failed to fetch current weather !!", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun populateWeatherDetails(
        weatherResponse: WeatherResponse,
        lastKnown: Boolean,
        lastCache: Boolean
    ) {
        findViewById<TextView>(R.id.sunrise).text = formatTime(weatherResponse.today.sunrise)
        findViewById<TextView>(R.id.sunset).text = formatTime(weatherResponse.today.sunset)
        findViewById<TextView>(R.id.temp).text = formatTemperature(weatherResponse.current.temperature)
        findViewById<TextView>(R.id.rain).text = formatPrecipitation(weatherResponse.current.precipitation)
        findViewById<TextView>(R.id.wind).text = formatWind(weatherResponse.current.windSpeed, weatherResponse.current.windDirection)
        findViewById<TextView>(R.id.location).text =
            if (weatherResponse.location.isNullOrEmpty()) {
                if (lastKnown && lastCache) "Last Known Location (Cached${getCacheDate(weatherResponse.forecast[0].date)})"
                else if (lastKnown) "Last Known Location"
                else if (lastCache) "Current Location (Cached${getCacheDate(weatherResponse.forecast[0].date)})"
                else "Current Location"
            }
            else {
                if (lastCache) "${weatherResponse.location} (Cached${getCacheDate(weatherResponse.forecast[0].date)})"
                else weatherResponse.location
            }
        findViewById<TextView>(R.id.title).text =
            if (weatherResponse.stormAlert.startsWith("No")) "${weatherResponse.stormAlert} Happy Fishing!"
            else weatherResponse.stormAlert
        findViewById<ImageView>(R.id.main_icon).setImageDrawable(Utils.getStormIndicator(weatherResponse.stormAlert, resources))

        dailyAdapter.updateData(weatherResponse.forecast.toMutableList())
        hourlyAdapter.updateData(weatherResponse.hourlyForecast.toMutableList())
    }

    private fun getCacheDate(date: String): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val inputDate = LocalDate.parse(date, formatter)
        val today = LocalDate.now()

        return if (inputDate != today) {
            " from ${inputDate.format(DateTimeFormatter.ofPattern("d MMM"))}"
        } else {
            ""
        }
    }

    private fun buildWeatherResponse(
        weatherEntity: WeatherEntity,
        currentWeather: CurrentWeatherEntity,
        dailyForecasts: List<DailyForecastEntity>,
        hourlyForecasts: List<HourlyForecastEntity>
    ): WeatherResponse {
        return WeatherResponse(
            coordinates = weatherEntity.toCoordinates(),
            location = weatherEntity.location,
            stormAlert = weatherEntity.stormAlert,
            current = currentWeather.toCurrentWeather(),
            forecast = dailyForecasts.map { it.toDailyForecast() },
            hourlyForecast = hourlyForecasts.map { it.toHourlyForecast() },
            today = weatherEntity.toTodaySummary()
        )
    }
}