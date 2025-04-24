package com.garlicbread.gofish

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.signature.ObjectKey
import com.garlicbread.gofish.room.GoFishDatabase
import com.garlicbread.gofish.room.entity.LogEntity
import com.garlicbread.gofish.util.Utils.Companion.toTitleCase
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.slider.Slider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddLogActivity : AppCompatActivity() {

    private lateinit var nameField: EditText
    private lateinit var weightSlider: Slider
    private lateinit var weightText: TextView
    private lateinit var lengthSlider: Slider
    private lateinit var lengthText: TextView
    private lateinit var descField: EditText
    private lateinit var tagInput: EditText
    private lateinit var tagContainer: FlexboxLayout
    private lateinit var imageView: ImageView
    private lateinit var submitButton: Button
    private lateinit var tagInfo: TextView
    private lateinit var imageInfo: TextView

    private var tagList = mutableListOf<String>()
    private var imageUri: Uri? = null
    private var lat: Double? = null
    private var lon: Double? = null
    private var radiusPx = 30f

    private lateinit var locationPermissionLauncher: ActivityResultLauncher<String>

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            Glide.with(this)
                .load(imageUri)
                .signature(ObjectKey(System.currentTimeMillis()))
                .transform(CenterCrop(), RoundedCorners(radiusPx.toInt()))
                .into(imageView)
            imageInfo.isVisible = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_log)

        val missingPerms = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            missingPerms.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            missingPerms.add(Manifest.permission.CAMERA)
        }

        locationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                getLocation()
            }
            if (missingPerms.contains(Manifest.permission.CAMERA))
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1)
        }

        if (missingPerms.contains(Manifest.permission.ACCESS_FINE_LOCATION)) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        else if (missingPerms.isNotEmpty()) { // only camera is not allowed
            ActivityCompat.requestPermissions(this, missingPerms.toTypedArray(), 1)
        }

        nameField = findViewById(R.id.name)
        weightSlider = findViewById(R.id.weight_slider)
        weightText = findViewById(R.id.weight)
        lengthSlider = findViewById(R.id.length_slider)
        lengthText = findViewById(R.id.length)
        descField = findViewById(R.id.descTextInput)
        tagInput = findViewById(R.id.tag_text)
        tagContainer = findViewById(R.id.tag_container)
        imageView = findViewById(R.id.image)
        submitButton = findViewById(R.id.button)
        tagInfo = findViewById(R.id.mini_info_1)
        imageInfo = findViewById(R.id.mini_info_2)

        radiusPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            30f,
            resources.displayMetrics
        )

        weightSlider.addOnChangeListener { _, value, _ ->
            weightText.text = String.format(Locale.US, "%.1f", value)
        }

        lengthSlider.addOnChangeListener { _, value, _ ->
            lengthText.text = String.format(Locale.US, "%.1f", value)
        }

        nameField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val hasText = !s.isNullOrBlank()
                submitButton.isEnabled = hasText
                submitButton.alpha = if (hasText) 1.0f else 0.4f
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        nameField.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                nameField.setText(nameField.text.trim())
                v.clearFocus()
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
                true
            } else {
                false
            }
        }

        tagInput.setOnEditorActionListener { v, actionId, _ ->
            val text = tagInput.text.toString().trim().toTitleCase()
            if (text.isNotEmpty() && !tagList.contains(text)) {
                tagList.add(text)
                addTagView(text)
            }
            tagInput.text.clear()
            tagInput.clearFocus()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(v.windowToken, 0)
            true
        }

        val uri = createImageUri()
        imageView.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                imageUri = uri
                cameraLauncher.launch(uri)
            } else {
                Toast.makeText(this, "Enable camera permission from settings", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
            }
        }

        imageView.setOnLongClickListener {
            imageUri = null
            imageView.setImageURI(null)
            imageView.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.add_photo, null))
            imageInfo.isVisible = false
            true
        }

        getLocation()

        submitButton.setOnClickListener {
            saveLog()
        }
    }

    private fun addTagView(tag: String) {
        val tagView = layoutInflater.inflate(R.layout.tag_layout, tagContainer, false)
        tagView.findViewById<TextView>(R.id.title)?.text = tag
        tagContainer.addView(tagView)
        tagView.setOnClickListener {
            tagContainer.removeView(tagView)
            tagList.remove(tag)
            if (tagList.isEmpty()) tagInfo.isVisible = false
        }
        tagInfo.isVisible = true
    }

    private fun createImageUri(): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val imageFile = File(getExternalFilesDir(null), "IMG_$timeStamp.jpg")
        return FileProvider.getUriForFile(this, "$packageName.fileprovider", imageFile)
    }

    private fun getLocation() {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        val location: Location? = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        lat = location?.latitude
        lon = location?.longitude
    }

    private fun saveLog() {
        val log = LogEntity(
            id = 0,
            title = nameField.text.trim().toString(),
            weight = weightSlider.value.toDouble(),
            length = lengthSlider.value.toDouble(),
            desc = descField.text.trim().toString(),
            tags = tagList.joinToString(","),
            imageUri = imageUri?.toString() ?: "",
            latitude = lat,
            longitude = lon,
            timestamp = System.currentTimeMillis()
        )

        CoroutineScope(Dispatchers.IO).launch {
            val dao = GoFishDatabase.getDatabase(this@AddLogActivity).logDao()
            dao.insert(log)
            withContext(Dispatchers.Main) {
                Toast.makeText(this@AddLogActivity, "Log saved!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
