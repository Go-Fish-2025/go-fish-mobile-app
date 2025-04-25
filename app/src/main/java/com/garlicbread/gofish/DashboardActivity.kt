package com.garlicbread.gofish

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.garlicbread.gofish.WeatherActivity
import com.garlicbread.gofish.data.FishDetails
import com.garlicbread.gofish.data.asFishEntity
import com.garlicbread.gofish.retrofit.RetrofitInstance
import com.garlicbread.gofish.room.GoFishDatabase
import com.garlicbread.gofish.room.repository.CheckpointRepository
import com.garlicbread.gofish.room.repository.FishRepository
import com.garlicbread.gofish.room.repository.LogRepository
import com.garlicbread.gofish.util.Utils.Companion.logout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.util.TimeZone
import kotlin.math.ln


class DashboardActivity : AppCompatActivity() {

    private lateinit var captureButton: ImageView
    private lateinit var compassButton: ImageView
    private lateinit var scanHistoryButton: ImageView
    private lateinit var weatherButton: ImageView
    private lateinit var fishingLogsButton: ImageView
    private lateinit var mapsButton: ImageView

    private var capturedImageFile: File? = null
    private lateinit var fishRepository: FishRepository
    private lateinit var logRepository: LogRepository

    private lateinit var checkpointRepository: CheckpointRepository

    private lateinit var locationPermissionLauncher: ActivityResultLauncher<String>

    private val TAG = "DashboardActivity"
    private val FISH_ID = "FISH_ID"

    private var lat: Double? = null
    private var lon: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val checkpointDao = GoFishDatabase.getDatabase(applicationContext).checkpointDao()
        checkpointRepository = CheckpointRepository(checkpointDao)

        val missingPerms = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            missingPerms.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            missingPerms.add(Manifest.permission.CAMERA)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                missingPerms.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        locationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                getLocation()
            }
            if (missingPerms.size > 1){
                ActivityCompat.requestPermissions(this, missingPerms.toTypedArray(), 1)
            }
        }

        if (missingPerms.contains(Manifest.permission.ACCESS_FINE_LOCATION)) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        else if (missingPerms.isNotEmpty()){
            ActivityCompat.requestPermissions(this, missingPerms.toTypedArray(), 1)
        }

        captureButton = findViewById(R.id.camera)
        captureButton.setOnClickListener {
            checkPermissionsAndCapture()
        }

        mapsButton = findViewById(R.id.maps)
        mapsButton.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }

        compassButton = findViewById(R.id.compass)
        compassButton.setOnClickListener {
            val intent = Intent(this, CompassActivity::class.java)
            startActivity(intent)
        }

        scanHistoryButton = findViewById(R.id.history)
        scanHistoryButton.setOnClickListener {
            val intent = Intent(this, ScanHistoryActivity::class.java)
            startActivity(intent)
        }

        weatherButton = findViewById(R.id.weather)
        weatherButton.setOnClickListener {
            val intent = Intent(this, WeatherActivity::class.java)
            startActivity(intent)
        }

        fishingLogsButton = findViewById(R.id.logs)
        fishingLogsButton.setOnClickListener {
            val intent = Intent(this, FishLogsActivity::class.java)
            startActivity(intent)
        }

        val fishDao = GoFishDatabase.getDatabase(applicationContext).fishDao()
        val logDao = GoFishDatabase.getDatabase(applicationContext).logDao()
        fishRepository = FishRepository(fishDao)
        logRepository = LogRepository(logDao)

        lifecycleScope.launch {
            logRepository.getLogCount().collectLatest {
                findViewById<TextView>(R.id.catch_count).text = it.toString()
            }
        }

        val imageView = findViewById<ImageView>(R.id.loading)
        Glide.with(this)
            .asGif()
            .load(R.drawable.loading)
            .into(imageView)

        findViewById<TextView>(R.id.greeting).text = getGreeting()

        findViewById<ImageView>(R.id.sign_out).setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Yes") { _, _ ->
                    lifecycleScope.launch(Dispatchers.IO) {
                        GoFishDatabase.getDatabase(this@DashboardActivity).clearAllTables()
                    }
                    logout(this, true, true)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        getLocation()
    }

    private fun checkNearestCheckpoint(currentLat: Double, currentLon: Double) {
        lifecycleScope.launch {
            val nearest = checkpointRepository.getNearestCheckpoint(currentLat, currentLon)
            if (nearest != null) {
                showNotification(this@DashboardActivity, "Look up !!", "You're within 10 miles of your saved checkpoint, ${nearest.title}")
            }
        }
    }

    private fun checkWeather(lat: Double, lng: Double){
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val rawResponse = RetrofitInstance.api.getWeather(lat, lng, TimeZone.getDefault().id)

                if (rawResponse.code() == 401) {
                    withContext(Dispatchers.Main) {
                        logout(this@DashboardActivity)
                    }
                    return@launch
                }

                val response = rawResponse.body()
                response?.let {
                    if (it.stormAlert.startsWith("Severe")) showNotification(
                        this@DashboardActivity,
                        "Watch Out !!",
                        "Severe storm heading your way. Better postpone your fishing plans."
                    )
                }
            } catch (_: Exception) { }
        }
    }

    private fun showNotification(context: Context, title: String, message: String, channelId: String = "Alerts", notificationId: Int = 1) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Alerts"
            val descriptionText = "Notifies about weather events or checkpoints"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.fishing)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }

    private fun getLocation() {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        val location: Location? = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        lat = location?.latitude
        lon = location?.longitude

        if (lat != null && lon != null){
            checkWeather(lat!!, lon!!)
            checkNearestCheckpoint(lat!!, lon!!)
        }
    }

    private val captureImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            val imageBitmap = data?.extras?.get("data") as? Bitmap
            if (imageBitmap != null) {
                capturedImageFile = saveBitmapToFile(imageBitmap)
                capturedImageFile?.let { sendImageToApi(it) }
            } else {
                Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkPermissionsAndCapture() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Enable camera permission from settings", Toast.LENGTH_LONG).show()
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            }
            startActivity(intent)
        }
        else {
            captureImage()
        }
    }

    private fun captureImage() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (cameraIntent.resolveActivity(packageManager) != null) {
            captureImageLauncher.launch(cameraIntent)
        } else {
            Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveBitmapToFile(bitmap: Bitmap): File {
        val file = File(cacheDir, "captured_image_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        }
        return file
    }

    private fun getGreeting(): String {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)

        return when (hour) {
            in 5..11 -> "Good Morning,"
            in 12..16 -> "Good Afternoon,"
            in 17..20 -> "Good Evening,"
            else -> "Good Night,"
        }
    }


    private fun sendImageToApi(imageFile: File) {
        val requestFile = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("file", imageFile.name, requestFile)

        findViewById<ImageView>(R.id.loading).isVisible = true
        RetrofitInstance.api.identifyFish(imagePart).enqueue(object : Callback<FishDetails> {
            override fun onResponse(call: Call<FishDetails>, response: Response<FishDetails>) {
                findViewById<ImageView>(R.id.loading).isVisible = false

                if (response.code() == 401) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        GoFishDatabase.getDatabase(this@DashboardActivity).clearAllTables()
                    }
                    logout(this@DashboardActivity)
                    return
                }

                if (response.isSuccessful) {
                    val fishDetails = response.body()
                    fishDetails?.let {
                        lifecycleScope.launch {
                            val fishId = fishRepository.insertFish(it.asFishEntity())

                            if (fishId != -1L) {
                                Log.d(TAG, "Saved fish to database: $fishId")
                                val intent = Intent(this@DashboardActivity, FishDetailsActivity::class.java).apply {
                                    putExtra(this@DashboardActivity.FISH_ID, fishId)
                                }
                                startActivity(intent)
                            }
                            else {
                                Toast.makeText(this@DashboardActivity, "Failed to parse data !!", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                    if (fishDetails == null) {
                        Toast.makeText(this@DashboardActivity, "Failed to identify fish !!", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this@DashboardActivity, "Failed to identify fish !!", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<FishDetails>, t: Throwable) {
                findViewById<ImageView>(R.id.loading).isVisible = false
                Toast.makeText(this@DashboardActivity, "Server Error. Try again later.", Toast.LENGTH_LONG).show()
            }
        })
    }
}