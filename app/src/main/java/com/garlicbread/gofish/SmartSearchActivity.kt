package com.garlicbread.gofish

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.text.InputType
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.garlicbread.gofish.room.GoFishDatabase
import com.google.android.gms.location.LocationServices
import com.google.android.material.slider.Slider
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*


class SmartSearchActivity : AppCompatActivity() {

    private lateinit var fishName: EditText
    private lateinit var weightFromSlider: Slider
    private lateinit var weightToSlider: Slider
    private lateinit var weightRange: TextView
    private lateinit var lengthFromSlider: Slider
    private lateinit var lengthToSlider: Slider
    private lateinit var lengthRange: TextView
    private lateinit var caughtWithinSlider: Slider
    private lateinit var caughtWithinRange: TextView
    private lateinit var dateInput: EditText
    private lateinit var tagInput: EditText
    private lateinit var searchButton: Button

    private val FROM_SEARCH = "FROM_SEARCH"
    private val LOG_IDS = "LOG_IDS"

    private lateinit var locationPermissionLauncher: ActivityResultLauncher<String>

    private var selectedDate: String? = null
    private var currentLatLng: Pair<Double, Double>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_smart_search)

        fishName = findViewById(R.id.fish_name)
        weightFromSlider = findViewById(R.id.weight_from_slider)
        weightToSlider = findViewById(R.id.weight_to_slider)
        weightRange = findViewById(R.id.weight_range)
        lengthFromSlider = findViewById(R.id.length_from_slider)
        lengthToSlider = findViewById(R.id.length_to_slider)
        lengthRange = findViewById(R.id.length_range)
        caughtWithinSlider = findViewById(R.id.caught_within_slider)
        caughtWithinRange = findViewById(R.id.caught_within_range)
        dateInput = findViewById(R.id.date_picker_input)
        tagInput = findViewById(R.id.tag_input)
        searchButton = findViewById(R.id.button)

        locationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                getLocation()
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        dateInput.apply {
            inputType = InputType.TYPE_NULL
            isFocusable = false
            isFocusableInTouchMode = false
            isClickable = true
        }

        val today = Calendar.getInstance()
        dateInput.setOnClickListener {
            val dpd = DatePickerDialog(this,
                { _, year, month, day ->
                    val formatted = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day)
                    selectedDate = formatted
                    dateInput.setText(formatted)
                }, today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH))

            dpd.datePicker.maxDate = System.currentTimeMillis()
            dpd.show()
        }

        val updateWeightLabel = {
            weightRange.text = String.format(Locale.US, "%.1f – %.1f lbs", weightFromSlider.value, weightToSlider.value)
        }

        val updateLengthLabel = {
            lengthRange.text = String.format(Locale.US, "%.1f – %.1f cms", lengthFromSlider.value, lengthToSlider.value)
        }

        val updateDistanceLabel = {
            caughtWithinRange.text = String.format(Locale.US, "%.0f miles", caughtWithinSlider.value)
        }

        weightFromSlider.addOnChangeListener { _, _, _ -> updateWeightLabel() }
        weightToSlider.addOnChangeListener { _, _, _ -> updateWeightLabel() }
        lengthFromSlider.addOnChangeListener { _, _, _ -> updateLengthLabel() }
        lengthToSlider.addOnChangeListener { _, _, _ -> updateLengthLabel() }
        caughtWithinSlider.addOnChangeListener { _, _, _ -> updateDistanceLabel() }

        updateWeightLabel()
        updateLengthLabel()
        updateDistanceLabel()

        getLocation()

        searchButton.setOnClickListener {
            if (weightToSlider.value < weightFromSlider.value || lengthToSlider.value < lengthFromSlider.value) {
                Toast.makeText(this, "Invalid range(s)!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            performSmartSearch()
        }
    }

    private fun getLocation() {
        val locationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        locationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                currentLatLng = Pair(location.latitude, location.longitude)
            }
        }
    }

    private fun performSmartSearch() {
        CoroutineScope(Dispatchers.IO).launch {
            val dao = GoFishDatabase.getDatabase(applicationContext).logDao()
            val allLogs = dao.getAll()

            val filtered = allLogs.filter { log ->
                val nameMatch = fishName.text.isNullOrBlank() || log.title.contains(fishName.text.toString().trim(), ignoreCase = true)
                val weightMatch = log.weight in weightFromSlider.value..weightToSlider.value
                val lengthMatch = log.length in lengthFromSlider.value..lengthToSlider.value
                val tagMatch = tagInput.text.isNullOrBlank() || tagInput.text.toString().split(",").any { tag -> log.tags.contains(tag.trim(), ignoreCase = true) }
                val dateMatch = selectedDate == null || isSameDay(log.timestamp, selectedDate!!)
                val distanceMatch = currentLatLng?.let { (lat, lon) ->
                    log.latitude != null && log.longitude != null &&
                            calculateDistance(lat, lon, log.latitude, log.longitude) <= caughtWithinSlider.value
                } != false

                nameMatch && weightMatch && lengthMatch && tagMatch && dateMatch && distanceMatch
            }

            withContext(Dispatchers.Main) {
                if (currentLatLng == null) {
                    Toast.makeText(this@SmartSearchActivity, "Location not found, ignoring distance filter.", Toast.LENGTH_SHORT).show()
                }
                val intent = Intent(this@SmartSearchActivity, AllLogsActivity::class.java)
                intent.putExtra(FROM_SEARCH, true)
                intent.putExtra(LOG_IDS, filtered.map { it.id }.joinToString(","))
                startActivity(intent)
            }
        }
    }

    private fun isSameDay(timestamp: Long, dateStr: String): Boolean {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val tsDate = sdf.format(Date(timestamp))
        return tsDate == dateStr
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val loc1 = Location("").apply {
            latitude = lat1
            longitude = lon1
        }
        val loc2 = Location("").apply {
            latitude = lat2
            longitude = lon2
        }
        return loc1.distanceTo(loc2) / 1609.34f
    }
}
