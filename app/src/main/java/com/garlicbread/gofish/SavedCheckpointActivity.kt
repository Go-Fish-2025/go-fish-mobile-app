package com.garlicbread.gofish

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class SavedCheckpointActivity : AppCompatActivity() {

    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().load(applicationContext, getSharedPreferences("osmdroid", MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = packageName
        Configuration.getInstance().tileFileSystemCacheMaxBytes = 1000L * 1024L * 1024L

        setContentView(R.layout.activity_saved_checkpoint)
        mapView = findViewById(R.id.map_view)

        val lat = intent.getDoubleExtra("Latitude", 0.0)
        val lng = intent.getDoubleExtra("Longitude", 0.0)
        val title = intent.getStringExtra("Title")
        val desc = intent.getStringExtra("Description")

        displayMapAtLocation(lat, lng, title.toString(), desc.toString())
    }

    private fun displayMapAtLocation(lat: Double, lng: Double, title: String, desc: String) {
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(15.0)
        mapView.controller.setCenter(GeoPoint(lat, lng))

        val marker = Marker(mapView)
        marker.position = GeoPoint(lat, lng)
        marker.title = title
        marker.snippet = if (desc.isNotEmpty()) desc else "No description added"
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        mapView.overlays.add(marker)
    }
}
