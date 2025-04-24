package com.garlicbread.gofish.util

import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import com.garlicbread.gofish.R
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class Utils {

    companion object {
        fun getConfidenceColor(confidence: Double, resources: Resources): Int {
            return if (confidence > 80) resources.getColor(R.color.very_high_confidence, null)
            else if (confidence > 60) resources.getColor(R.color.high_confidence, null)
            else if (confidence > 40) resources.getColor(R.color.low_confidence, null)
            else resources.getColor(R.color.very_low_confidence, null)
        }

        fun String.toTitleCase(): String {
            return split(" ").joinToString(" ") {
                it.lowercase().replaceFirstChar { char -> char.titlecase() }
            }
        }

        fun getStormIndicator(stormAlert: String, resources: Resources): Drawable {
            return if (stormAlert.startsWith("Sever")) return ResourcesCompat.getDrawable(resources, R.drawable.heavy_storm, null)
            else if (stormAlert.startsWith("Moderate")) return ResourcesCompat.getDrawable(resources, R.drawable.moderate_storm, null)
            else if (stormAlert.startsWith("Minor")) return ResourcesCompat.getDrawable(resources, R.drawable.light_storm, null)
            else return ResourcesCompat.getDrawable(resources, R.drawable.no_storm, null)
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun formatTime(timeString: String): String {
            return try {
                val time = LocalTime.parse(timeString)
                val formatter = DateTimeFormatter.ofPattern("hh:mm\na")
                time.format(formatter)
            } catch (_: Exception) {
                timeString
            }
        }

        fun formatPrecipitation(precipitation: Double, newLine: Boolean = true): String {
            return if (newLine) String.format(Locale.getDefault(), "%.1f\nmm", precipitation)
            else String.format(Locale.getDefault(), "%.1f mm", precipitation)
        }

        fun formatTemperature(tempCelsius: Double): String {
            val tempFahrenheit = tempCelsius * 9 / 5 + 32
            return String.format(Locale.getDefault(), "%d\u00B0", tempFahrenheit.toInt())
        }

        fun formatWind(windSpeed: Double, windDirection: Int, newLine: Boolean = true): String {
            val windSpeedMph = windSpeed * 0.621371
            val direction = when ((windDirection + 22) / 45 % 8) {
                0 -> "N"
                1 -> "NE"
                2 -> "E"
                3 -> "SE"
                4 -> "S"
                5 -> "SW"
                6 -> "W"
                7 -> "NW"
                else -> "N"
            }
            return if (newLine) String.format(Locale.getDefault(), "%.1f\nmph %s", windSpeedMph, direction)
            else String.format(Locale.getDefault(), "%.1f mph %s", windSpeedMph, direction)
        }

        fun formatTimestamp(timestamp: Long): String {
            val date = Date(timestamp)
            val day = SimpleDateFormat("d", Locale.US).format(date).toInt()
            val suffix = when {
                day in 11..13 -> "th"
                day % 10 == 1 -> "st"
                day % 10 == 2 -> "nd"
                day % 10 == 3 -> "rd"
                else -> "th"
            }

            val raw = SimpleDateFormat("d'$suffix' MMMM, yyyy 'at' h:mm a", Locale.US).format(date)
            return raw.replace("AM", "am").replace("PM", "pm")
        }
    }
}