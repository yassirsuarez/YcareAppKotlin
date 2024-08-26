package com.example.navigation

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.navigation.databinding.MedicineBinding
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager

class Medicine : Fragment() {

    private lateinit var binding: MedicineBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var myAdapter: ExpandedMedicineAdapter
    private val viewModel: MedicinaViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = MedicineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.aggiungiMedicina.setOnClickListener {
            val intent = Intent(requireContext(), Medicina_x::class.java)
            startActivity(intent)
        }

        binding.mostraMedicine.setOnClickListener {
            val intent = Intent(requireContext(), MedicineTutte::class.java)
            startActivity(intent)
        }

        recyclerView = binding.medicineMain
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)

        myAdapter = ExpandedMedicineAdapter(requireContext(),arrayListOf(),
            onPresaClick = { medicina ->
                viewModel.updatePresa(medicina.id, medicina.orario,requireContext())
                val intent = Intent(requireContext(), MainActivity::class.java)
                requireActivity().finish()
                startActivity(intent)
            },
            onUpdateClick = {medicina->
                val intent=Intent(requireContext(),Medicina_x::class.java)
                intent.putExtra("id_medicina", medicina.id)
                startActivity(intent)
            },
            onShareClick={medicina->
                viewModel.shareMedicineInfoSpecifico(requireContext(),medicina)
            }
        )
        recyclerView.adapter = myAdapter

        viewModel.expandedMedicineList.observe(viewLifecycleOwner, { expandedList ->
            myAdapter.updateData(expandedList)
        })
        viewModel.datiMedicineList()
    }
}

