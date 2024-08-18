package com.example.navigation

import android.content.ActivityNotFoundException
import android.net.Uri
import android.widget.Toast
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.navigation.databinding.AppuntamentiBinding
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import android.content.Intent

class Appuntamenti : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var binding: AppuntamentiBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var myAdapter: AdapterAppuntamenti
    private val viewModel: AppuntamentiViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= AppuntamentiBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        return binding.root

    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.storicoAppuntamenti.setOnClickListener{
            val intent= Intent(requireContext(),AppuntamentiTutti::class.java)
            startActivity(intent)
        }
        binding.aggiungi.setOnClickListener{
            val intent= Intent(requireContext(),DatiAppuntamenti::class.java)
            startActivity(intent)
        }

        recyclerView = binding.appuntamentitot
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)

        myAdapter=AdapterAppuntamenti(arrayListOf(),
        onDeleteClick = { appuntamenti ->
            viewModel.deleteAppuntamento(appuntamenti.id)
        },
        onUpdateClick = { appuntamenti ->
            val intent= Intent(requireContext(),DatiAppuntamenti::class.java)
            intent.putExtra("titolo",appuntamenti.titolo)
            intent.putExtra("luogo",appuntamenti.luogo)
            intent.putExtra("data",appuntamenti.data)
            intent.putExtra("ora",appuntamenti.ora)
            intent.putExtra("id_appuntamento",appuntamenti.id)
            startActivity(intent)
        },
            onLuogoClick = {appuntamento->
                openAddressInGoogleMaps(appuntamento.luogo)},
            1)
        recyclerView.adapter = myAdapter
        viewModel.appuntamentiList.observe(viewLifecycleOwner, { appuntamentiList ->
            myAdapter.updateData(appuntamentiList)
        })
        val user = auth.currentUser
        user?.let {
            val uid = it.uid
        viewModel.datiAppuntamentiListaFragment(uid)
            viewModel.numeroAppuntamentiOggi(uid)}

    }

    fun openAddressInGoogleMaps(address: String?) {
        val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(address)}")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        try {
            startActivity(mapIntent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "Maps non è installato.", Toast.LENGTH_SHORT).show()
        }
    }

}