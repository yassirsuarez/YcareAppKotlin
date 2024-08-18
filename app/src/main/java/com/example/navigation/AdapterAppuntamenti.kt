package com.example.navigation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AdapterAppuntamenti(private val appuntamentiList: ArrayList<AppuntamentiData>,
                          private val onDeleteClick:(AppuntamentiData)->Unit,
                          private val onUpdateClick:(AppuntamentiData)->Unit,
                          private val onLuogoClick:(AppuntamentiData)->Unit,
                          private val layoutType: Int
) : RecyclerView.Adapter<AdapterAppuntamenti.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.lista_appuntamenti, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val appuntamento: AppuntamentiData = appuntamentiList[position]
        holder.titolo.text = appuntamento.titolo
        holder.luogo.text = appuntamento.luogo
        holder.data.text = appuntamento.data
        holder.ora.text = appuntamento.ora
        holder.luogo.setOnClickListener{
            onLuogoClick(appuntamento)
        }
        holder.deleteButton.setOnClickListener{
            onDeleteClick(appuntamento)
        }
        holder.updateButton.setOnClickListener{
            onUpdateClick(appuntamento)
        }
        if (layoutType == 1) {
            holder.deleteButton.visibility = View.GONE
            holder.updateButton.visibility = View.GONE
        } else {
            holder.deleteButton.visibility = View.VISIBLE
            holder.updateButton.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int {
        return appuntamentiList.size
    }

    fun updateData(newAppuntamentiList: List<AppuntamentiData>) {
        appuntamentiList.clear()
        appuntamentiList.addAll(newAppuntamentiList)
        notifyDataSetChanged()
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titolo: TextView = itemView.findViewById(R.id.titolo_list)
        val luogo: TextView = itemView.findViewById(R.id.luogo_list)
        val data: TextView = itemView.findViewById(R.id.data_list)
        val ora: TextView = itemView.findViewById(R.id.ora_list)
        val deleteButton : ImageView = itemView.findViewById(R.id.delete_button)
        val updateButton: ImageView = itemView.findViewById(R.id.update_button)
    }
}
