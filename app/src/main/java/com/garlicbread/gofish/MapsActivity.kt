package com.garlicbread.gofish

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.garlicbread.gofish.room.GoFishDatabase
import com.garlicbread.gofish.room.entity.CheckpointEntity
import com.garlicbread.gofish.room.repository.CheckpointRepository
import com.garlicbread.gofish.util.Utils.Companion.downloadTilesAroundLocation
import com.garlicbread.gofish.util.Utils.Companion.toTitleCase
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var addButton: ImageView
    private lateinit var offline: ImageView

    private lateinit var locationPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var checkpointRepository: CheckpointRepository

    private var currentLatLng: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        locationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                setUpMap()
            } else {
                Toast.makeText(this, "Location access denied! Allow it in Settings.", Toast.LENGTH_SHORT).show()
            }
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        checkpointRepository = CheckpointRepository(GoFishDatabase.getDatabase(this).checkpointDao())

        addButton = findViewById(R.id.add_btn)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        addButton.setOnClickListener {
            currentLatLng?.let {
                showAddCheckpointDialog(it)
            } ?: Toast.makeText(this, "Location not yet available", Toast.LENGTH_SHORT).show()
        }

        offline = findViewById<ImageView>(R.id.offline)
        offline.setOnClickListener {
            val intent = Intent(this, AllCheckpointsActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isMyLocationButtonEnabled = true

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            setUpMap()
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun setUpMap(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    currentLatLng = LatLng(it.latitude, it.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng!!, 15f))
                }
            }

            lifecycleScope.launch(Dispatchers.IO) {
                val checkpoints = checkpointRepository.getAllCheckpoints()

                withContext(Dispatchers.Main) {
                    for (checkpoint in checkpoints) {
                        val position = LatLng(checkpoint.latitude, checkpoint.longitude)
                        mMap.addMarker(
                            MarkerOptions()
                                .position(position)
                                .title(checkpoint.title)
                                .snippet(checkpoint.description)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                        )
                    }
                }
            }
        }
    }

    private fun showAddCheckpointDialog(latLng: LatLng) {
        val view = LayoutInflater.from(this).inflate(R.layout.dailog_input, null)
        val titleInput = view.findViewById<EditText>(R.id.checkpoint)
        val descInput = view.findViewById<EditText>(R.id.description)

        AlertDialog.Builder(this)
            .setTitle("Add Checkpoint")
            .setView(view)
            .setPositiveButton("Save") { dialog, _ ->
                val title = titleInput.text.toString().trim().toTitleCase()
                val desc = descInput.text.toString().trim()
                if (title.isNotEmpty()) {
                    saveCheckpoint(title, desc, latLng)
                } else {
                    Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun saveCheckpoint(title: String, desc: String, latLng: LatLng) {
        val checkpoint = CheckpointEntity(
            title = title,
            description = desc,
            latitude = latLng.latitude,
            longitude = latLng.longitude
        )

        CoroutineScope(Dispatchers.IO).launch {
            GoFishDatabase.getDatabase(applicationContext).checkpointDao().insert(checkpoint)
            withContext(Dispatchers.Main) {
                mMap.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title(title)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                )
                Toast.makeText(this@MapsActivity, "Checkpoint saved!", Toast.LENGTH_SHORT).show()
            }
        }

        downloadTilesAroundLocation(this, checkpoint.latitude, checkpoint.longitude)
    }
}
