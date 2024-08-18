package com.example.navigation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MyAdapter(private val userList: ArrayList<User>,
                private val onDeleteClick:(User)->Unit,
                private val onUpdateClick:(User)->Unit): RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.lista_storico, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val user: User = userList[position]
        holder.peso.text = user.peso
        holder.altezza.text = user.altezza
        holder.data.text = user.data

        holder.deleteButton.setOnClickListener{
            onDeleteClick(user)
        }
        if (position != 0) {
            holder.updateButton.visibility = View.GONE}
        holder.updateButton.setOnClickListener{
            onUpdateClick(user)
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }
    fun updateData(newUserList: List<User>) {
        userList.clear()
        userList.addAll(newUserList)
        notifyDataSetChanged()
    }

    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val peso: TextView = itemView.findViewById(R.id.peso_list)
        val altezza: TextView = itemView.findViewById(R.id.altezza_list)
        val data: TextView = itemView.findViewById(R.id.data_list)
        val deleteButton : ImageView = itemView.findViewById(R.id.delete_button)
        val updateButton: ImageView = itemView.findViewById(R.id.update_button)
    }
}
