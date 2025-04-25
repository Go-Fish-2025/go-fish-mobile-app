package com.garlicbread.gofish.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.garlicbread.gofish.R
import com.garlicbread.gofish.SavedCheckpointActivity
import com.garlicbread.gofish.room.entity.CheckpointEntity
import com.garlicbread.gofish.util.Utils.Companion.toTitleCase


class CheckpointAdapter(
    private var checkpoints: List<CheckpointEntity>,
    private val context: Context
) : RecyclerView.Adapter<CheckpointAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.name)
        val description: TextView = view.findViewById(R.id.description)
        val card: ConstraintLayout = view.findViewById(R.id.card)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.checkpoint_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.text = checkpoints[position].title.toTitleCase()
        holder.description.text = if (checkpoints[position].description.isNotEmpty())
            checkpoints[position].description else "No description added"
        holder.card.setOnClickListener {
            val intent = Intent(context, SavedCheckpointActivity::class.java)
            intent.putExtra("Latitude", checkpoints[position].latitude)
            intent.putExtra("Longitude", checkpoints[position].longitude)
            intent.putExtra("Title", checkpoints[position].title)
            intent.putExtra("Description", checkpoints[position].description)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = checkpoints.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newCheckpoints: List<CheckpointEntity>) {
        checkpoints = newCheckpoints
        notifyDataSetChanged()
    }
}