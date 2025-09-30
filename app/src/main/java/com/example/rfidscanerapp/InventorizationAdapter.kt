package com.example.rfidscanner.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.rfidscanner.R
import com.example.rfidscanner.model.InventorizationItem
import java.time.format.DateTimeFormatter
import android.content.Intent
import com.example.rfidscanner.InventorizationDetailActivity

class InventorizationAdapter(
    private val context: Context,
    private val items: List<InventorizationItem>
) : RecyclerView.Adapter<InventorizationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: CardView = view.findViewById(R.id.cardItem)
        val datetime: TextView = view.findViewById(R.id.textDatetime)
        val person: TextView = view.findViewById(R.id.textPerson)
        val location: TextView = view.findViewById(R.id.textLocation)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_inventorization, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val input = item.datetime
        val parsed = java.time.LocalDateTime.parse(input)
        val formatted = parsed.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        holder.datetime.text = formatted
        holder.person.text = item.person
        holder.location.text = item.location

        val color = when (item.status) {
            1 -> 0x90A3D179.toInt() // green
            2 -> 0xFFDDDDDD.toInt() // gray
            3 -> 0x90E65135.toInt() // red
            else -> 0xFFFFFFFF.toInt() // default white
        }

        holder.card.setCardBackgroundColor(color)

        holder.card.setOnClickListener {
            if (item.id != 0) {
                val intent = Intent(context, InventorizationDetailActivity::class.java)
                intent.putExtra("item_id", item.id)
                intent.putExtra("status", item.status)
                context.startActivity(intent)
            }
        }
    }
}