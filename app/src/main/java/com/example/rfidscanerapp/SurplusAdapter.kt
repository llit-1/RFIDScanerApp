package com.example.rfidscanner.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rfidscanner.R
import com.example.rfidscanner.model.SurplusItemModel

class SurplusAdapter(private var items: List<SurplusItemModel>) :
    RecyclerView.Adapter<SurplusAdapter.SurplusViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SurplusViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_surplus, parent, false)
        return SurplusViewHolder(view)
    }

    override fun onBindViewHolder(holder: SurplusViewHolder, position: Int) {
        val item = items[position]
        holder.obj.text = item.obj
        holder.name.text = item.objectName
        holder.category.text = item.objectCategory
        holder.holder.text = item.holder ?: "—"
        holder.location.text = item.location ?: "—"
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<SurplusItemModel>) {
        items = newItems
        notifyDataSetChanged()
    }

    class SurplusViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val obj: TextView = itemView.findViewById(R.id.textObj)
        val name: TextView = itemView.findViewById(R.id.textName)
        val category: TextView = itemView.findViewById(R.id.textCategory)
        val holder: TextView = itemView.findViewById(R.id.textHolder)
        val location: TextView = itemView.findViewById(R.id.textLocation)
    }
}