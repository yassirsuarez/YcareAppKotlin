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

class MedicineAdapter(private val context: Context, private val medicineList: ArrayList<MedicinaData>,
                      private val onDeleteClick:(MedicinaData)->Unit,
                      private val onUpdateClick:(MedicinaData)->Unit,
                      private val onShareClick:(MedicinaData)->Unit)
    : RecyclerView.Adapter<MedicineAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder{
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.lista_medicine, parent, false)
        return MyViewHolder(itemView)
    }
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val medicina:MedicinaData=medicineList[position]
        holder.titolo.text=medicina.nome
        holder.data.text=medicina.Data_inizio
        holder.numeroMedicine.text=medicina.numero_medicine.toString()
        holder.voltePerGiorno.text=medicina.Numero_Per_Giorno.toString()

        holder.immagine?.let {
                Glide.with(context)
                    .load(medicina.foto)
                    .error(R.drawable.default_medicne)
                    .into(it)
            }

        medicina.orari.values.forEach { orario ->
            val orarioTextView = TextView(holder.itemView.context)
            orarioTextView.text = orario.orario + " "
            holder.orariContainer.addView(orarioTextView)
        }
        holder.deleteLinear.setOnClickListener{
            onDeleteClick(medicina)
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
    fun updateData(newMedicineList: List<MedicinaData>) {
        medicineList.clear()
        medicineList.addAll(newMedicineList)
        notifyDataSetChanged()
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val immagine:ImageView=itemView.findViewById(R.id.foto_medicina)
        val titolo: TextView = itemView.findViewById(R.id.nome_medicina)
        val data: TextView = itemView.findViewById(R.id.data_inizio_medicina)
        val numeroMedicine: TextView = itemView.findViewById(R.id.numero_medicine)
        val voltePerGiorno: TextView = itemView.findViewById(R.id.volte_per_giorno)
        val modificaLinear:LinearLayout=itemView.findViewById(R.id.modifica_medicina)
        val deleteLinear: LinearLayout = itemView.findViewById(R.id.delete_medicina)
        val presaMedicina:LinearLayout=itemView.findViewById(R.id.presa_medicina)
        val orariContainer:LinearLayout=itemView.findViewById(R.id.orariContainer)
        val shareContainer:LinearLayout=itemView.findViewById(R.id.share_medicine)
        init {
            deleteLinear.visibility = View.VISIBLE
            presaMedicina.visibility = View.GONE
        }
    }
    }