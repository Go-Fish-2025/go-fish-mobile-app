package com.garlicbread.gofish

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.garlicbread.gofish.adapter.CheckpointAdapter
import com.garlicbread.gofish.room.GoFishDatabase
import com.garlicbread.gofish.room.repository.CheckpointRepository
import kotlinx.coroutines.launch

class AllCheckpointsActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_checkpoints)

        val recyclerView: RecyclerView = findViewById(R.id.list)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val checkpointDao = GoFishDatabase.getDatabase(applicationContext).checkpointDao()
        val checkpointRepository = CheckpointRepository(checkpointDao)

        val adapter = CheckpointAdapter(emptyList(), this)
        recyclerView.adapter = adapter

        lifecycleScope.launch {
            val checkpoints = checkpointRepository.getAllCheckpoints()
            if (checkpoints.isNotEmpty()) findViewById<TextView>(R.id.tv_name).text = "Saved Checkpoints (${checkpoints.size})"
            findViewById<TextView>(R.id.no_data).isVisible = checkpoints.isEmpty()
            adapter.updateData(checkpoints)
        }
    }
}