package com.garlicbread.gofish

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.garlicbread.gofish.data.FishDetails
import com.garlicbread.gofish.data.asFishEntity
import com.garlicbread.gofish.retrofit.RetrofitInstance
import com.garlicbread.gofish.room.GoFishDatabase
import com.garlicbread.gofish.room.repository.FishRepository
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream


class DashboardActivity : AppCompatActivity() {

    private lateinit var captureButton: ImageView
    private lateinit var compassButton: ImageView
    private lateinit var scanHistoryButton: ImageView
    private lateinit var weatherButton: ImageView
    private lateinit var fishingLogsButton: ImageView

    private var capturedImageFile: File? = null
    private lateinit var fishRepository: FishRepository

    private val TAG = "DashboardActivity"
    private val FISH_ID = "FISH_ID"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        captureButton = findViewById(R.id.camera)
        captureButton.setOnClickListener {
            checkPermissionsAndCapture()
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
        fishRepository = FishRepository(fishDao)

        val imageView = findViewById<ImageView>(R.id.loading)
        Glide.with(this)
            .asGif()
            .load(R.drawable.loading)
            .into(imageView)
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

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions.all { it.value }) {
            captureImage()
        } else {
            Toast.makeText(this, "Camera and storage permissions are required", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkPermissionsAndCapture() {
        val permissions = arrayOf(
            Manifest.permission.CAMERA
        )
        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (permissionsToRequest.isEmpty()) {
            captureImage()
        } else {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
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

    private fun sendImageToApi(imageFile: File) {
        val requestFile = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("file", imageFile.name, requestFile)

        findViewById<ImageView>(R.id.loading).isVisible = true
        RetrofitInstance.api.identifyFish(imagePart).enqueue(object : Callback<FishDetails> {
            override fun onResponse(call: Call<FishDetails>, response: Response<FishDetails>) {
                findViewById<ImageView>(R.id.loading).isVisible = false
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