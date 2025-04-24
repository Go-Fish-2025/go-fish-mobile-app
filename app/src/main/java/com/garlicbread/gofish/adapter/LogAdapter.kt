package com.garlicbread.gofish.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.garlicbread.gofish.LogDetailsActivity
import com.garlicbread.gofish.R
import com.garlicbread.gofish.room.entity.LogEntity
import com.garlicbread.gofish.util.Utils.Companion.formatTimestamp
import com.garlicbread.gofish.util.Utils.Companion.toTitleCase


class LogAdapter(
    private var logs: List<LogEntity>,
    private val context: Context
) : RecyclerView.Adapter<LogAdapter.ViewHolder>() {

    private val LOG_ID = "LOG_ID"

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.name)
        val image: ImageView = view.findViewById(R.id.imageView)
        val date: TextView = view.findViewById(R.id.date)
        val card: ConstraintLayout = view.findViewById(R.id.card)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fish_log_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.text = logs[position].title.toTitleCase()

        Glide.with(context)
            .load(logs[position].imageUri)
            .apply(
                RequestOptions()
                    .transform(CenterCrop(), RoundedCorners(50))
            )
            .placeholder(R.drawable.fish_dp)
            .error(R.drawable.fish_dp)
            .into(holder.image)

        holder.date.text = formatTimestamp(logs[position].timestamp)

        holder.card.setOnClickListener {
            val intent = Intent(context, LogDetailsActivity::class.java)
            intent.putExtra(LOG_ID, logs[position].id)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = logs.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newLogs: List<LogEntity>) {
        logs = newLogs
        notifyDataSetChanged()
    }
}