package com.garlicbread.gofish.adapter

import HourlyForecast
import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.garlicbread.gofish.R
import com.garlicbread.gofish.util.Utils.Companion.formatPrecipitation
import com.garlicbread.gofish.util.Utils.Companion.formatTemperature
import com.garlicbread.gofish.util.Utils.Companion.formatWind
import com.garlicbread.gofish.util.Utils.Companion.getStormIndicator
import java.text.SimpleDateFormat
import java.util.Locale


class HourlyForecastAdapter(
    private var forecast: List<HourlyForecast>,
    private val resources: Resources
) : RecyclerView.Adapter<HourlyForecastAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.main_icon)
        val time: TextView = view.findViewById(R.id.time)
        val temp: TextView = view.findViewById(R.id.temp)
        val rain: TextView = view.findViewById(R.id.rain)
        val wind: TextView = view.findViewById(R.id.wind)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.hourly_forecast_item, parent, false)
        return ViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = forecast[position]

        holder.icon.setImageDrawable(getStormIndicator(data.stormAlert, resources))
        holder.time.text = formatTime(data.time)
        holder.temp.text = formatTemperature(data.temperature)
        holder.rain.text = formatPrecipitation(data.precipitation, false)
        holder.wind.text = formatWind(data.windSpeed, data.windDirection, false)
    }

    override fun getItemCount(): Int = forecast.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newForecast: MutableList<HourlyForecast>) {
        forecast = newForecast
        notifyDataSetChanged()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun formatTime(timeStr: String): String {
        val inputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val outputFormat = SimpleDateFormat("h a", Locale.getDefault())
        val time = inputFormat.parse(timeStr)
        return if (time != null) outputFormat.format(time) else ""
    }

}