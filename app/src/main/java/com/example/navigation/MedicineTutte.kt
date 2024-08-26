package com.example.navigation

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.navigation.databinding.MedicineTutteBinding


class MedicineTutte : AppCompatActivity(){
    private lateinit var binding: MedicineTutteBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var myAdapter: MedicineAdapter
    private val viewModel: MedicinaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MedicineTutteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val toolbar: Toolbar = binding.toolbar2
        setSupportActionBar(toolbar)
        recyclerView = binding.medicineTutte
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        myAdapter=MedicineAdapter(this,arrayListOf(),
            onDeleteClick = {medicina ->
                viewModel.deleteMedicina(medicina.id,this)},
            onUpdateClick = {medicina->
                val intent=Intent(this,Medicina_x::class.java)
                intent.putExtra("id_medicina", medicina.id)
                startActivity(intent)},
            onShareClick = {medicina->
                viewModel.shareMedicineInfo(this,medicina)
            }
        )
        recyclerView.adapter = myAdapter
        viewModel.medicineList.observe(this, { appuntamentiList ->
            myAdapter.updateData(appuntamentiList)
        })
        viewModel.datiMedicineList()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
}