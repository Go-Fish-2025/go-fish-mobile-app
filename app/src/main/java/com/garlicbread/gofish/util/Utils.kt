package com.garlicbread.gofish.util

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import com.garlicbread.gofish.R
import org.osmdroid.tileprovider.cachemanager.CacheManager
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.MapView
import java.security.KeyStore
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import kotlin.math.cos
import androidx.core.content.edit
import com.garlicbread.gofish.LoginActivity
import com.garlicbread.gofish.constants.Constants
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.TimeUnit
import javax.crypto.spec.GCMParameterSpec
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class Utils {

    companion object {

        fun haversineMiles(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
            val R = 3958.8 // Radius of Earth in miles
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)
            val a = sin(dLat / 2).pow(2.0) +
                    cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                    sin(dLon / 2).pow(2.0)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            return R * c
        }

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


        fun generateKeyIfNeeded() {
            val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

            if (!keyStore.containsAlias("jwt_key")) {
                val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
                val keySpec = KeyGenParameterSpec.Builder(
                    "jwt_key",
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()

                keyGenerator.init(keySpec)
                keyGenerator.generateKey()
            }
        }

        fun encryptAndStoreJwt(context: Context, jwt: String) {
            val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
            val secretKey = (keyStore.getEntry("jwt_key", null) as KeyStore.SecretKeyEntry).secretKey

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(jwt.toByteArray())

            val prefs = context.getSharedPreferences(Constants.SHARED_PREF_TAG, Context.MODE_PRIVATE)
            prefs.edit {
                putString(Constants.JWT_TOKEN, Base64.encodeToString(encryptedBytes, Base64.DEFAULT))
                    .putString(Constants.JWT_IV, Base64.encodeToString(iv, Base64.DEFAULT))
            }
        }

        fun decryptJwt(context: Context): String? {
            val prefs = context.getSharedPreferences(Constants.SHARED_PREF_TAG, Context.MODE_PRIVATE)
            val encryptedBase64 = prefs.getString(Constants.JWT_TOKEN, null) ?: return null
            val ivBase64 = prefs.getString(Constants.JWT_IV, null) ?: return null

            val encryptedBytes = Base64.decode(encryptedBase64, Base64.DEFAULT)
            val iv = Base64.decode(ivBase64, Base64.DEFAULT)

            val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
            val secretKey = (keyStore.getEntry("jwt_key", null) as KeyStore.SecretKeyEntry).secretKey

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

            return String(cipher.doFinal(encryptedBytes))
        }

        fun saveJwtTimestamp(context: Context) {
            val sharedPrefs = context.getSharedPreferences(Constants.SHARED_PREF_TAG, Context.MODE_PRIVATE)
            sharedPrefs.edit {
                putLong(Constants.JWT_SAVED_AT, System.currentTimeMillis())
            }
        }

        fun logout(context: Context, relaunchApp: Boolean = true, forceLogout: Boolean = false) {
            FirebaseAuth.getInstance().signOut()

            val prefs = context.getSharedPreferences(Constants.SHARED_PREF_TAG, Context.MODE_PRIVATE)
            prefs.edit { clear() }

            try {
                val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
                if (keyStore.containsAlias("jwt_key")) {
                    keyStore.deleteEntry("jwt_key")
                }
            } catch (_: Exception) { }

            if (relaunchApp) {
                val intent = Intent(context, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(intent)
            }

            val message = if (forceLogout) "Logged Out!" else "Session expired. Please sign back in."
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }


        fun isJwtExpired(context: Context): Boolean {
            val sharedPrefs = context.getSharedPreferences(Constants.SHARED_PREF_TAG, Context.MODE_PRIVATE)
            val issuedAt = sharedPrefs.getLong(Constants.JWT_SAVED_AT, -1L)

            if (issuedAt == -1L) return true

            val ninetyDaysMillis = TimeUnit.DAYS.toMillis(90)
            val buffer = TimeUnit.DAYS.toMillis(1)
            val now = System.currentTimeMillis()

            return now - issuedAt > (ninetyDaysMillis - buffer)
        }


        fun downloadTilesAroundLocation(context: Context, lat: Double, lon: Double) {
            val usgsTopo = object : org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase(
                "USGSTopo",
                1,
                18,
                256,
                "",
                arrayOf("https://basemap.nationalmap.gov/arcgis/rest/services/USGSTopo/MapServer/tile/")
            ) {
                override fun getTileURLString(pMapTileIndex: Long): String {
                    return baseUrl + "${MapTileIndex.getZoom(pMapTileIndex)}/${MapTileIndex.getY(pMapTileIndex)}/${MapTileIndex.getX(pMapTileIndex)}"
                }
            }

            val mapView = MapView(context)
            mapView.setTileSource(usgsTopo)
            mapView.setUseDataConnection(true)
            mapView.setMultiTouchControls(false)

            val milesToDeg = 1.0 / 69.0
            val latOffset = milesToDeg
            val lonOffset = milesToDeg / cos(Math.toRadians(lat))

            val box = BoundingBox(
                lat + latOffset,
                lon + lonOffset,
                lat - latOffset,
                lon - lonOffset
            )

            val cacheManager = CacheManager(mapView)
            val zoom = 16

            cacheManager.downloadAreaAsync(
                context,
                box,
                zoom,
                zoom,
                object : CacheManager.CacheManagerCallback {
                    override fun onTaskComplete() {
                        Toast.makeText(context, "1-mile tiles cached!", Toast.LENGTH_SHORT).show()
                    }

                    override fun onTaskFailed(errors: Int) {
                        Toast.makeText(context, "Tile cache failed ($errors tiles).", Toast.LENGTH_SHORT).show()
                    }

                    override fun updateProgress(
                        progress: Int,
                        currentZoomLevel: Int,
                        zoomMin: Int,
                        zoomMax: Int
                    ) {
                    }

                    override fun downloadStarted() {}
                    override fun setPossibleTilesInArea(total: Int) {}
                }
            )
        }

    }
}