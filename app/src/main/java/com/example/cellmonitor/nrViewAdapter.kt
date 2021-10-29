package com.example.cellmonitor

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class nrViewAdapter(private val context: Context) :
    RecyclerView.Adapter<nrViewAdapter.ViewHolder>(){

    private val items :MutableList<NetworkData> = mutableListOf()

    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val time:TextView = view.findViewById(R.id.time)
        val state:TextView = view.findViewById(R.id.networkState)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_item,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.time.text = item.time
        holder.state.text = item.state

    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun add(item:NetworkData){
        this.items.add(items.size,item)
        notifyDataSetChanged()
    }
}