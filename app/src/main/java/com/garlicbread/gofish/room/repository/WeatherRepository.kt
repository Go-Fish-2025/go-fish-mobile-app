package com.garlicbread.gofish.room.repository

import WeatherResponse
import com.garlicbread.gofish.data.asCurrentWeatherEntity
import com.garlicbread.gofish.data.asDailyForecastEntities
import com.garlicbread.gofish.data.asHourlyForecastEntities
import com.garlicbread.gofish.data.asWeatherEntity
import com.garlicbread.gofish.room.dao.WeatherDao
import com.garlicbread.gofish.room.entity.CurrentWeatherEntity
import com.garlicbread.gofish.room.entity.DailyForecastEntity
import com.garlicbread.gofish.room.entity.HourlyForecastEntity
import com.garlicbread.gofish.room.entity.WeatherEntity

class WeatherRepository(private val weatherDao: WeatherDao) {

    suspend fun saveWeatherData(
        weather: WeatherResponse
    ) {
        val weatherEntity = weather.asWeatherEntity()
        val currentEntity = weather.asCurrentWeatherEntity()
        val dailyEntities = weather.asDailyForecastEntities()
        val hourlyEntities = weather.asHourlyForecastEntities()

        weatherDao.insertWeatherData(weatherEntity, currentEntity, dailyEntities, hourlyEntities)
    }

    suspend fun getWeather(loc: String): WeatherEntity? {
        return weatherDao.getWeather(loc)
    }

    suspend fun getWeather(lat: Double, lon: Double): WeatherEntity? {
        return weatherDao.getWeather(lat, lon)
    }

    suspend fun getCurrentWeather(lat: Double, lon: Double): CurrentWeatherEntity? {
        return weatherDao.getCurrentWeather(lat, lon)
    }

    suspend fun getDailyEntities(lat: Double, lon: Double): List<DailyForecastEntity> {
        return weatherDao.getDailyForecast(lat, lon)
    }

    suspend fun getHourlyEntities(lat: Double, lon: Double): List<HourlyForecastEntity> {
        return weatherDao.getHourlyForecast(lat, lon)
    }
}
