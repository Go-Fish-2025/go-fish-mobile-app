package com.garlicbread.gofish.data

import Coordinates
import CurrentWeather
import DailyForecast
import HourlyForecast
import TodaySummary
import WeatherResponse
import com.garlicbread.gofish.room.entity.CurrentWeatherEntity
import com.garlicbread.gofish.room.entity.DailyForecastEntity
import com.garlicbread.gofish.room.entity.FishEntity
import com.garlicbread.gofish.room.entity.HourlyForecastEntity
import com.garlicbread.gofish.room.entity.WeatherEntity

fun FishDetails.asFishEntity(): FishEntity {
    return FishEntity(
        confidence = confidence,
        name = details.name,
        scientificName = details.scientificName,
        appearanceAndAnatomy = details.appearanceAndAnatomy,
        coloration = details.color.coloration,
        dominantColour = details.color.dominantColor,
        commonNames = details.commonNames.joinToString(","),
        diet = details.feedingAndFoodValue.diet,
        foodValue = details.feedingAndFoodValue.foodValue,
        healthWarnings = details.feedingAndFoodValue.healthWarnings,
        depthRange = details.habitatAndRange.depthRange,
        distribution = details.habitatAndRange.distribution,
        habitat = details.habitatAndRange.habitat,
        conservationStatus = details.handlingAndConservation.conservationStatus,
        handlingTip = details.handlingAndConservation.handlingTip,
        imageUrl = details.imageUrl,
        recordCatchAngler = details.recordCatch.angler,
        recordCatchDate = details.recordCatch.date,
        recordCatchLocation = details.recordCatch.location,
        recordCatchType = details.recordCatch.type,
        recordCatchWeight = details.recordCatch.weight,
        commonLength = details.sizeAndLifespan.commonLength,
        lifespan = details.sizeAndLifespan.lifespan,
        maximumLength = details.sizeAndLifespan.maximumLength,
        reproduction = details.sizeAndLifespan.reproduction,
        weightRecord = details.sizeAndLifespan.weightRecord,
        taxonomyClass = details.taxonomy.className,
        taxonomyFamily = details.taxonomy.family,
        taxonomyGenus = details.taxonomy.genus,
        taxonomyKingdom = details.taxonomy.kingdom,
        taxonomyOrder = details.taxonomy.order,
        taxonomyPhylum = details.taxonomy.phylum
    )
}

fun WeatherResponse.asWeatherEntity(): WeatherEntity {
    return WeatherEntity(
        latitude = coordinates.latitude,
        longitude = coordinates.longitude,
        timezone = coordinates.timezone,
        location = location,
        stormAlert = stormAlert,
        precipitation = today.precipitationSum,
        sunset = today.sunset,
        sunrise = today.sunrise
    )
}

fun WeatherResponse.asCurrentWeatherEntity(): CurrentWeatherEntity {
    current.let {
        return CurrentWeatherEntity(
            latitude = coordinates.latitude,
            longitude = coordinates.longitude,
            temperature = it.temperature,
            precipitation = it.precipitation,
            time = it.time,
            weatherCode = it.weatherCode,
            windDirection = it.windDirection,
            windSpeed = it.windSpeed
        )
    }
}

fun WeatherResponse.asDailyForecastEntities(): List<DailyForecastEntity> {
    return forecast.map {
        DailyForecastEntity(
            latitude = coordinates.latitude,
            longitude = coordinates.longitude,
            date = it.date,
            precipitationSum = it.precipitationSum,
            stormAlert = it.stormAlert,
            sunrise = it.sunrise,
            sunset = it.sunset,
            temperatureMax = it.temperatureMax,
            temperatureMin = it.temperatureMin,
            weatherCode = it.weatherCode,
            windDirectionDominant = it.windDirectionDominant,
            windSpeedMax = it.windSpeedMax
        )
    }
}

fun WeatherResponse.asHourlyForecastEntities(): List<HourlyForecastEntity> {
    return hourlyForecast.map {
        HourlyForecastEntity(
            latitude = coordinates.latitude,
            longitude = coordinates.longitude,
            time = it.time,
            temperature = it.temperature,
            precipitation = it.precipitation,
            stormAlert = it.stormAlert,
            weatherCode = it.weatherCode,
            windDirection = it.windDirection,
            windSpeed = it.windSpeed
        )
    }
}

fun WeatherEntity.toCoordinates(): Coordinates {
    return Coordinates(
        latitude = latitude,
        longitude = longitude,
        timezone = timezone
    )
}

fun WeatherEntity.toTodaySummary(): TodaySummary {
    return TodaySummary(
        precipitationSum = precipitation,
        sunrise = sunrise,
        sunset = sunset
    )
}

fun CurrentWeatherEntity.toCurrentWeather(): CurrentWeather {
    return CurrentWeather(
        precipitation = precipitation,
        temperature = temperature,
        time = time,
        weatherCode = weatherCode,
        windDirection = windDirection,
        windSpeed = windSpeed
    )
}

fun DailyForecastEntity.toDailyForecast(): DailyForecast {
    return DailyForecast(
        date = date,
        precipitationSum = precipitationSum,
        stormAlert = stormAlert,
        sunrise = sunrise,
        sunset = sunset,
        temperatureMax = temperatureMax,
        temperatureMin = temperatureMin,
        weatherCode = weatherCode,
        windDirectionDominant = windDirectionDominant,
        windSpeedMax = windSpeedMax
    )
}

fun HourlyForecastEntity.toHourlyForecast(): HourlyForecast {
    return HourlyForecast(
        precipitation = precipitation,
        stormAlert = stormAlert,
        temperature = temperature,
        time = time,
        weatherCode = weatherCode,
        windDirection = windDirection,
        windSpeed = windSpeed
    )
}