package com.example.navigation

import android.app.Activity
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import com.example.navigation.databinding.MedicinaXBinding
import androidx.lifecycle.Observer
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.widget.*
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*


class Medicina_x : AppCompatActivity() {
    private lateinit var binding: MedicinaXBinding
    private lateinit var pulsantiOra: LinearLayout
    private lateinit var volteGiorno: EditText
    private val calendar = Calendar.getInstance()
    private val viewModel:MedicinaViewModel by viewModels()
    private lateinit var fotoMedicine: ImageView
    private var imageUri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(!intent.getStringExtra("id_medicina").isNullOrEmpty()){
           viewModel.datiMedicinaX(intent.getStringExtra("id_medicina"))

            viewModel.medicineX.observe(this,{medicina->
                if (medicina != null) {
                    findViewById<EditText>(R.id.medicina_nome).setText(medicina.nome)
                    findViewById<EditText>(R.id.medicine_numero).setText(medicina.numero_medicine)
                    findViewById<TextView>(R.id.label_data_inizio_medicina).text = medicina.Data_inizio
                    findViewById<EditText>(R.id.medicina_volte_giorno).setText(medicina.Numero_Per_Giorno)
                    val timeButtonsContainer = findViewById<LinearLayout>(R.id.pulsanti_ore)
                    timeButtonsContainer.removeAllViews()
                    medicina.Numero_Per_Giorno?.toIntOrNull()?.let { timesInt ->
                        val orariList = medicina.orari.values.map { it.orario }
                        for (time in 1..timesInt) {
                            val timeButton = Button(this).apply {
                                text = orariList.getOrNull(time - 1)
                                setOnClickListener {
                                    showTimePickerDialog(this, time) }
                            }
                            timeButtonsContainer.addView(timeButton)
                            viewModel.setOrari(time, orariList.getOrNull(time - 1))
                        }
                    } ?: run {
                        Toast.makeText(this, "Inserisci un numero valido di volte al giorno", Toast.LENGTH_SHORT).show()
                    }
                } })
        }
        binding = MedicinaXBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.lifecycleOwner=this
        binding.viewModel=viewModel

        viewModel.voltePerGiorno.observe(this, Observer { times ->
            val timeButtonsContainer = findViewById<LinearLayout>(R.id.pulsanti_ore)
            timeButtonsContainer.removeAllViews()
            times?.toIntOrNull()?.let { timesInt ->
                for (time in 1..timesInt) {
                    val timeButton = Button(this).apply {
                        if(!intent.getStringExtra("id_medicina").isNullOrEmpty()){

                        }
                        else text = "Seleziona Ora $time"
                        setOnClickListener {
                            showTimePickerDialog(this, time) }
                    }
                    timeButtonsContainer.addView(timeButton)

                }
            } ?: run {
                Toast.makeText(this, "Inserisci un numero valido di volte al giorno", Toast.LENGTH_SHORT).show()
            }
        })
        binding.InizioMedicina.setOnClickListener{
            showDatePicker()
        }


        binding.medicinaConferma.setOnClickListener{
            if(!intent.getStringExtra("id_medicina").isNullOrEmpty()){
                viewModel.aggiornaMedicina(imageUri,intent.getStringExtra("id_medicina"))}
            else viewModel.inserireMedicina(imageUri)

            finish()
            val intent=Intent(this,MainActivity::class.java)
            startActivity(intent)
        }

        fotoMedicine= binding.medicinePhoto
        fotoMedicine.setOnClickListener {
            selectImage()
        }

    }



    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            this, {DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, monthOfYear, dayOfMonth)
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val formattedDate = dateFormat.format(selectedDate.time)
               binding.labelDataInizioMedicina.text = "$formattedDate"
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }
    private fun showTimePickerDialog(button: Button,id:Int) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
            button.text = formattedTime
            viewModel.setOrari(id, formattedTime)
        }, hour, minute, true).show()
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 1)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            imageUri?.let {
                Glide.with(this).load(it).into(fotoMedicine)
            }
        }
    }
}

