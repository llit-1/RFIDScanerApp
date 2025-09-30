package com.example.rfidscanner.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.rfidscanner.R
import com.example.rfidscanner.model.InventarizationItemModel

class InventorizationDetailAdapter(
    private val context: Context,
    val items: MutableList<InventarizationItemModel>
) : RecyclerView.Adapter<InventorizationDetailAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val obj: TextView = view.findViewById(R.id.textObj)
        val category: TextView = view.findViewById(R.id.textCategory)
        val name: TextView = view.findViewById(R.id.textObjectName)
        val holderText: TextView = view.findViewById(R.id.textHolder)
        val checkmark: ImageView = view.findViewById(R.id.imageCheck)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_inventorization_detail, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.obj.text = item.obj
        holder.category.text = item.objectCategory
        holder.name.text = item.objectName
        holder.holderText.text = item.holder ?: ""
        holder.checkmark.visibility = if (item.detected) View.VISIBLE else View.INVISIBLE
    }

    fun updateDetectedByObj(obj: String) {
        val index = items.indexOfFirst { it.obj == obj }
        if (index != -1) {
            val item = items[index]
            if (!item.detected) {
                item.detected = true
                notifyItemChanged(index)
            }
        }
    }
}