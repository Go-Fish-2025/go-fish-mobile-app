package com.garlicbread.gofish

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.garlicbread.gofish.adapter.FishScanAdapter
import com.garlicbread.gofish.room.GoFishDatabase
import com.garlicbread.gofish.room.repository.FishRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ScanHistoryActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_history)

        val recyclerView: RecyclerView = findViewById(R.id.list)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val fishDao = GoFishDatabase.getDatabase(applicationContext).fishDao()
        val fishRepository = FishRepository(fishDao)

        val adapter = FishScanAdapter(emptyList(), this, resources)
        recyclerView.adapter = adapter

        lifecycleScope.launch {
            fishRepository.getHistory().collectLatest { fishScans ->
                if (fishScans.isNotEmpty()) findViewById<TextView>(R.id.tv_name).text = "Past Scans (${fishScans.size})"
                findViewById<TextView>(R.id.no_data).isVisible = fishScans.isEmpty()
                adapter.updateData(fishScans)
            }
        }
    }
}