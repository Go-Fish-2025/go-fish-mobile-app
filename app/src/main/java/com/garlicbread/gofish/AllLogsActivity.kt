package com.garlicbread.gofish

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.garlicbread.gofish.adapter.LogAdapter
import com.garlicbread.gofish.room.GoFishDatabase
import com.garlicbread.gofish.room.entity.LogEntity
import com.garlicbread.gofish.room.repository.LogRepository
import kotlinx.coroutines.launch

class AllLogsActivity : AppCompatActivity() {

    private val LOG_IDS = "LOG_IDS"
    private val FROM_SEARCH = "FROM_SEARCH"

    @SuppressLint("SetTextI18n", "CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_logs)

        val recyclerView: RecyclerView = findViewById(R.id.list)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val fromSearch = intent.getBooleanExtra(FROM_SEARCH, false)
        val logIds = intent.getStringExtra(LOG_IDS)
        if (fromSearch && logIds == "") {
            findViewById<TextView>(R.id.no_data).text = "No matching logs found. Maybe try going back and changing the parameters."
            findViewById<TextView>(R.id.no_data).isVisible = true
            return
        }

        val logDao = GoFishDatabase.getDatabase(applicationContext).logDao()
        val logRepository = LogRepository(logDao)

        val adapter = LogAdapter(emptyList(), this)
        recyclerView.adapter = adapter

        lifecycleScope.launch {
            if (fromSearch) {
                val allLogs = logIds?.split(",")?.mapNotNull {
                    logRepository.getLogById(it.toLong())
                } ?: emptyList<LogEntity>()

                if (allLogs.isNotEmpty()) {
                    findViewById<TextView>(R.id.tv_name).text = "Fishing Logs (${allLogs.size})"
                    findViewById<TextView>(R.id.no_data).isVisible = false
                    adapter.updateData(allLogs)
                }
                else {
                    findViewById<TextView>(R.id.no_data).text = "No matching logs found. Maybe try going back and changing the parameters."
                    findViewById<TextView>(R.id.no_data).isVisible = true
                }
            }
            else {
                val allLogs = logRepository.getAllLogs()
                if (allLogs.isNotEmpty()) findViewById<TextView>(R.id.tv_name).text = "Fishing Logs (${allLogs.size})"
                findViewById<TextView>(R.id.no_data).isVisible = allLogs.isEmpty()
                adapter.updateData(allLogs)
            }
        }
    }
}