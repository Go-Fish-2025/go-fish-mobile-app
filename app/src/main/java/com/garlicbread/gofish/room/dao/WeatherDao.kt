package com.garlicbread.gofish.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.garlicbread.gofish.room.entity.CurrentWeatherEntity
import com.garlicbread.gofish.room.entity.DailyForecastEntity
import com.garlicbread.gofish.room.entity.HourlyForecastEntity
import com.garlicbread.gofish.room.entity.WeatherEntity

@Dao
interface WeatherDao {

    @Transaction
    suspend fun insertWeatherData(
        weather: WeatherEntity,
        current: CurrentWeatherEntity,
        daily: List<DailyForecastEntity>,
        hourly: List<HourlyForecastEntity>
    ) {
        insertWeather(weather)
        insertCurrent(current)
        insertDaily(daily)
        insertHourly(hourly)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(location: WeatherEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCurrent(current: CurrentWeatherEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDaily(daily: List<DailyForecastEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHourly(hourly: List<HourlyForecastEntity>)

    @Query("""
        SELECT * FROM weather
        WHERE latitude BETWEEN :lat - 0.15 AND :lat + 0.15
          AND longitude BETWEEN :lon - 0.15 AND :lon + 0.15
        ORDER BY 
          CASE WHEN latitude = :lat AND longitude = :lon THEN 0 ELSE 1 END,
          ABS(latitude - :lat) + ABS(longitude - :lon)
        LIMIT 1
    """)
    suspend fun getWeather(lat: Double, lon: Double): WeatherEntity?

    @Query("SELECT * FROM current_weather WHERE latitude = :lat AND longitude = :lon")
    suspend fun getCurrentWeather(lat: Double, lon: Double): CurrentWeatherEntity?

    @Query("SELECT * FROM daily_forecast WHERE latitude = :lat AND longitude = :lon")
    suspend fun getDailyForecast(lat: Double, lon: Double): List<DailyForecastEntity>

    @Query("SELECT * FROM hourly_forecast WHERE latitude = :lat AND longitude = :lon")
    suspend fun getHourlyForecast(lat: Double, lon: Double): List<HourlyForecastEntity>

    @Query("SELECT * FROM weather WHERE LOWER(location) = LOWER(:location)")
    suspend fun getWeather(location: String): WeatherEntity?

}
