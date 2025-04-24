package com.garlicbread.gofish

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class FishLogsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fish_logs)

        val addLog = findViewById<ImageView>(R.id.add_log)
        addLog.setOnClickListener {
            val intent = Intent(this, AddLogActivity::class.java)
            startActivity(intent)
        }

        val smartSearch = findViewById<ImageView>(R.id.smart_search)
        smartSearch.setOnClickListener {
            val intent = Intent(this, SmartSearchActivity::class.java)
            startActivity(intent)
        }

        val allLogs = findViewById<ImageView>(R.id.all_logs)
        allLogs.setOnClickListener {
            val intent = Intent(this, AllLogsActivity::class.java)
            startActivity(intent)
        }
    }
}