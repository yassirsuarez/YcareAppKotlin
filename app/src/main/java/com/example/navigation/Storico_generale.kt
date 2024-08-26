package com.example.navigation

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.navigation.databinding.StoricoGeneraleBinding
import androidx.appcompat.widget.Toolbar

class Storico_generale : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var myAdapter: MyAdapter
    private lateinit var binding: StoricoGeneraleBinding
    private val viewModel: DatiUtenteViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = StoricoGeneraleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val id = intent.getStringExtra("id")
        if (id == null) {
            finish()
            return
        }

        recyclerView = binding.storicotot
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        myAdapter = MyAdapter(arrayListOf(),
            onDeleteClick = { user ->
                viewModel.deleteDatoSalute(user, id)
            },
            onUpdateClick = { user ->
                val intent = Intent(this, DatiSalute::class.java)
                intent.putExtra("peso", user.peso)
                intent.putExtra("altezza", user.altezza)
                intent.putExtra("data", user.data)
                startActivity(intent)
            }
        )
        recyclerView.adapter = myAdapter

        viewModel.userList.observe(this) { userList ->
            myAdapter.updateData(userList)
        }

        viewModel.datiSaluteLista(id)

        val toolbar: Toolbar = binding.toolbar2
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
