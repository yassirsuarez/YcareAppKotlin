package com.example.navigation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import android.content.Context

class ExpandedMedicineAdapter(
    private val context: Context,
    private val medicineList: ArrayList<ExpandedMedicineItem>,
    private val onPresaClick: (ExpandedMedicineItem) -> Unit,
    private val onUpdateClick: (ExpandedMedicineItem) -> Unit,
    private val onShareClick:(ExpandedMedicineItem)->Unit)
    : RecyclerView.Adapter<ExpandedMedicineAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.lista_medicine, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val medicina: ExpandedMedicineItem = medicineList[position]
        holder.titolo.text = medicina.nome
        holder.data.text = medicina.Data_inizio
        holder.numeroMedicine.text = medicina.numero_medicine
        holder.voltePerGiorno.text = medicina.Numero_Per_Giorno

        holder.fotoImageView?.let {
            Glide.with(context)
                .load(medicina.foto)
                .error(R.drawable.default_medicne)
                .into(it)
        }

            val orarioTextView=TextView(holder.itemView.context)
            orarioTextView.text=medicina.orario
            holder.orariContainer.addView(orarioTextView)
        holder.presaLinear.setOnClickListener{
            onPresaClick(medicina)
        }
        holder.modificaLinear.setOnClickListener{
            onUpdateClick(medicina)
        }
        holder.shareContainer.setOnClickListener{
            onShareClick(medicina)
        }
    }

    override fun getItemCount(): Int {
        return medicineList.size
    }

    fun updateData(newMedicineList: List<ExpandedMedicineItem>) {
        medicineList.clear()
        medicineList.addAll(newMedicineList)
        notifyDataSetChanged()
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titolo: TextView = itemView.findViewById(R.id.nome_medicina)
        val data: TextView = itemView.findViewById(R.id.data_inizio_medicina)
        val numeroMedicine: TextView = itemView.findViewById(R.id.numero_medicine)
        val voltePerGiorno: TextView = itemView.findViewById(R.id.volte_per_giorno)
        val orariContainer: LinearLayout = itemView.findViewById(R.id.orariContainer)
        val fotoImageView: ImageView = itemView.findViewById(R.id.foto_medicina)
        val presaLinear: LinearLayout = itemView.findViewById(R.id.presa_medicina)
        val deleteLinear: LinearLayout = itemView.findViewById(R.id.delete_medicina)
        val modificaLinear:LinearLayout=itemView.findViewById(R.id.modifica_medicina)
        val shareContainer:LinearLayout=itemView.findViewById(R.id.share_medicine)

        init {
            deleteLinear.visibility = View.GONE
            presaLinear.visibility = View.VISIBLE
        }
    }
}

