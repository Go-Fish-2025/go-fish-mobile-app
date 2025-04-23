import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    @SerializedName("coordinates") val coordinates: Coordinates,
    @SerializedName("current") val current: CurrentWeather,
    @SerializedName("forecast") val forecast: List<DailyForecast>,
    @SerializedName("hourly_forecast") val hourlyForecast: List<HourlyForecast>,
    @SerializedName("location") val location: String?,
    @SerializedName("storm_alert") val stormAlert: String,
    @SerializedName("today") val today: TodaySummary
)

data class Coordinates(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("timezone") val timezone: String
)

data class CurrentWeather(
    @SerializedName("precipitation") val precipitation: Double,
    @SerializedName("temperature") val temperature: Double,
    @SerializedName("time") val time: String,
    @SerializedName("weather_code") val weatherCode: Int,
    @SerializedName("wind_direction") val windDirection: Int,
    @SerializedName("wind_speed") val windSpeed: Double
)

data class DailyForecast(
    @SerializedName("date") val date: String,
    @SerializedName("precipitation_sum") val precipitationSum: Double,
    @SerializedName("storm_alert") val stormAlert: String,
    @SerializedName("sunrise") val sunrise: String,
    @SerializedName("sunset") val sunset: String,
    @SerializedName("temperature_max") val temperatureMax: Double,
    @SerializedName("temperature_min") val temperatureMin: Double,
    @SerializedName("weather_code") val weatherCode: Int,
    @SerializedName("wind_direction_dominant") val windDirectionDominant: Int,
    @SerializedName("wind_speed_max") val windSpeedMax: Double
)

data class HourlyForecast(
    @SerializedName("precipitation") val precipitation: Double,
    @SerializedName("storm_alert") val stormAlert: String,
    @SerializedName("temperature") val temperature: Double,
    @SerializedName("time") val time: String,
    @SerializedName("weather_code") val weatherCode: Int,
    @SerializedName("wind_direction") val windDirection: Int,
    @SerializedName("wind_speed") val windSpeed: Double
)

data class TodaySummary(
    @SerializedName("precipitation_sum") val precipitationSum: Double,
    @SerializedName("sunrise") val sunrise: String,
    @SerializedName("sunset") val sunset: String
)