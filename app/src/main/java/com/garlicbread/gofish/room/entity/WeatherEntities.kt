package com.garlicbread.gofish.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(tableName = "weather", primaryKeys = ["latitude", "longitude"])
data class WeatherEntity(
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    val location: String?,
    val stormAlert: String,
    val precipitation: Double,
    val sunset: String,
    val sunrise: String
)

@Entity(
    tableName = "current_weather",
    primaryKeys = ["latitude", "longitude"],
    foreignKeys = [
        ForeignKey(
            entity = WeatherEntity::class,
            parentColumns = ["latitude", "longitude"],
            childColumns = ["latitude", "longitude"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CurrentWeatherEntity(
    val latitude: Double,
    val longitude: Double,
    val temperature: Double,
    val precipitation: Double,
    val time: String,
    val weatherCode: Int,
    val windDirection: Int,
    val windSpeed: Double
)

@Entity(
    tableName = "daily_forecast",
    primaryKeys = ["latitude", "longitude", "date"],
    foreignKeys = [
        ForeignKey(
            entity = WeatherEntity::class,
            parentColumns = ["latitude", "longitude"],
            childColumns = ["latitude", "longitude"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DailyForecastEntity(
    val latitude: Double,
    val longitude: Double,
    val date: String,
    val precipitationSum: Double,
    val stormAlert: String,
    val sunrise: String,
    val sunset: String,
    val temperatureMax: Double,
    val temperatureMin: Double,
    val weatherCode: Int,
    val windDirectionDominant: Int,
    val windSpeedMax: Double
)

@Entity(
    tableName = "hourly_forecast",
    primaryKeys = ["latitude", "longitude", "time"],
    foreignKeys = [
        ForeignKey(
            entity = WeatherEntity::class,
            parentColumns = ["latitude", "longitude"],
            childColumns = ["latitude", "longitude"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class HourlyForecastEntity(
    val latitude: Double,
    val longitude: Double,
    val time: String,
    val temperature: Double,
    val precipitation: Double,
    val stormAlert: String,
    val weatherCode: Int,
    val windDirection: Int,
    val windSpeed: Double
)


