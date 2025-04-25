package com.garlicbread.gofish

import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.garlicbread.gofish.data.FishDetails
import com.garlicbread.gofish.retrofit.RetrofitInstance
import com.garlicbread.gofish.room.GoFishDatabase
import com.garlicbread.gofish.room.entity.LogEntity
import com.garlicbread.gofish.util.Utils.Companion.logout
import com.garlicbread.gofish.util.Utils.Companion.toTitleCase
import com.google.android.flexbox.FlexboxLayout
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.net.toUri


class LogDetailsActivity : AppCompatActivity(), OnMapReadyCallback {

    private var lat: Double? = null
    private var lon: Double? = null
    private val LOG_ID = "LOG_ID"
    private var uri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_details)

        val logId = intent.getLongExtra(LOG_ID, -1L)
        if (logId == -1L) {
            Toast.makeText(this, "Invalid log", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val imageView = findViewById<ImageView>(R.id.loading)
        Glide.with(this)
            .asGif()
            .load(R.drawable.loading)
            .into(imageView)

        findViewById<ImageView>(R.id.trophy).setOnClickListener {
            if (uri != null) {
                sendImageToApi(uri!!)
            }
            else {
                Toast.makeText(this, "Fish image needed !!", Toast.LENGTH_LONG).show()
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            val log = GoFishDatabase.getDatabase(applicationContext).logDao().getLogById(logId)
            withContext(Dispatchers.Main) {
                log?.let { populateLog(it) }
            }
        }
    }

    private fun sendImageToApi(uri: Uri) {
        val contentResolver = applicationContext.contentResolver
        val fileName = "image_${System.currentTimeMillis()}.jpg"
        val tempFile = File.createTempFile(fileName, null, cacheDir)

        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                tempFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to read image", Toast.LENGTH_LONG).show()
            return
        }

        val requestFile = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("file", fileName, requestFile)

        findViewById<ImageView>(R.id.loading_overlay).isVisible = true
        RetrofitInstance.api.identifyFish(imagePart).enqueue(object : Callback<FishDetails> {
            override fun onResponse(call: Call<FishDetails>, response: Response<FishDetails>) {
                findViewById<ImageView>(R.id.loading_overlay).isVisible = false

                if (response.code() == 401) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        GoFishDatabase.getDatabase(this@LogDetailsActivity).clearAllTables()
                    }
                    logout(this@LogDetailsActivity)
                    return
                }

                if (response.isSuccessful) {
                    val fishDetails = response.body()
                    fishDetails?.let {
                        lifecycleScope.launch {
                            val recordCatch = fishDetails.details.recordCatch
                            showRecordCatchDialog(
                                recordCatch.angler,
                                recordCatch.date,
                                recordCatch.location,
                                recordCatch.type,
                                recordCatch.weight
                            )
                        }
                    } ?: Toast.makeText(this@LogDetailsActivity, "Failed to identify fish !!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@LogDetailsActivity, "Failed to identify fish !!", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<FishDetails>, t: Throwable) {
                findViewById<ImageView>(R.id.loading_overlay).isVisible = false
                Toast.makeText(this@LogDetailsActivity, "Server Error. Try again later.", Toast.LENGTH_LONG).show()
            }
        })
    }


    private fun showRecordCatchDialog(
        angler: String,
        date: String,
        location: String,
        type: String,
        weight: String
    ) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_record_catch, null)

        dialogView.findViewById<TextView>(R.id.tv_record_catch_angler).text = angler
        dialogView.findViewById<TextView>(R.id.tv_record_catch_date).text = date
        dialogView.findViewById<TextView>(R.id.tv_record_catch_location).text = location
        dialogView.findViewById<TextView>(R.id.tv_record_catch_type).text = type
        dialogView.findViewById<TextView>(R.id.tv_record_catch_weight).text = weight

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialogView.findViewById<Button>(R.id.ok_button).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }


    private fun populateLog(log: LogEntity) {
        uri = log.imageUri.toUri()
        findViewById<TextView>(R.id.tv_name).text = log.title.toTitleCase()

        findViewById<ImageView>(R.id.img_fish).setPadding(0)
        Glide.with(this)
            .load(log.imageUri)
            .apply(
                RequestOptions()
                    .transform(CenterCrop(), RoundedCorners(50))
            )
            .placeholder(R.drawable.fish_dp)
            .error(R.drawable.fish_dp)
            .into(findViewById<ImageView>(R.id.img_fish))

        findViewById<TextView>(R.id.weight).text = String.format(Locale.getDefault(), "%.1f lbs", log.weight)
        findViewById<TextView>(R.id.length).text = String.format(Locale.getDefault(), "%.1f cms", log.length)

        val date = Date(log.timestamp)
        findViewById<TextView>(R.id.date).text = SimpleDateFormat("d MMM yyyy", Locale.US).format(date)
        findViewById<TextView>(R.id.time).text = SimpleDateFormat("h:mm a", Locale.US).format(date).lowercase()

        findViewById<TextView>(R.id.desc).text = log.desc

        val tagContainer = findViewById<FlexboxLayout>(R.id.tag_container)
        if (log.tags.isNotBlank()) {
            val tags = log.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            if (tags.isNotEmpty()) {
                tagContainer.isVisible = true
                tags.forEach { tag ->
                    val tagView = layoutInflater.inflate(R.layout.tag_layout, tagContainer, false)
                    tagView.findViewById<TextView>(R.id.title).text = tag
                    tagContainer.addView(tagView)
                }
            } else {
                tagContainer.isVisible = false
            }
        } else {
            tagContainer.isVisible = false
        }

        lat = log.latitude
        lon = log.longitude

        if (lat != null && lon != null) {
            val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
            mapFragment.getMapAsync(this)
            findViewById<FrameLayout>(R.id.map_container).isVisible = true
        } else {
            findViewById<FrameLayout>(R.id.map_container).isVisible = false
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        lat?.let { latitude ->
            lon?.let { longitude ->
                val location = LatLng(latitude, longitude)
                googleMap.addMarker(
                    MarkerOptions()
                        .position(location)
                        .title("Catch Location")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                )
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 12f))
            }
        }
    }
}
