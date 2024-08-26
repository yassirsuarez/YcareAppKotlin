package com.example.navigation

import android.content.Intent
import android.os.Bundle
import android.app.TimePickerDialog
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.app.DatePickerDialog
import androidx.activity.viewModels
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*
import android.widget.Toast

class DatiAppuntamenti: AppCompatActivity() {
    private val viewModel: AppuntamentiViewModel by viewModels()
    private lateinit var auth: FirebaseAuth
    private lateinit var btnDatePicker: Button
    private lateinit var tvSelectedDate: TextView
    private val calendar = Calendar.getInstance()
    private lateinit var btnTimePicker: Button
    private lateinit var tvSelectedTimer: TextView
    lateinit var titolo: EditText
    lateinit var luogo: EditText
    lateinit var uid: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dati_appuntamenti)
        titolo=findViewById(R.id.titolo_appuntamenti)
        luogo=findViewById(R.id.luogo_appuntamento)
        btnDatePicker = findViewById(R.id.btnPicker)
        tvSelectedDate = findViewById(R.id.testo_data)
        btnTimePicker = findViewById(R.id.timepicker)
        tvSelectedTimer = findViewById(R.id.testo_ora)
        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        user?.let {
            uid = it.uid}

        btnDatePicker.setOnClickListener {
            showDatePicker()
        }

        btnTimePicker.setOnClickListener {
            val cal = Calendar.getInstance()
            val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                tvSelectedTimer.text = SimpleDateFormat("HH:mm").format(cal.time)
            }
            TimePickerDialog(this, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }
        if (intent.getStringExtra("id_appuntamento") != null) {
            titolo.setText(intent.getStringExtra("titolo"))
            luogo.setText(intent.getStringExtra("luogo"))
            tvSelectedDate.setText(intent.getStringExtra("data"))
            tvSelectedTimer.setText(intent.getStringExtra("ora"))
        }
        findViewById<Button>(R.id.conferma_dati_appuntamento).setOnClickListener{
            if(uid!=null){
                if(intent.getStringExtra("id_appuntamento") !=null){
            viewModel.UpdateAppuntamento(uid,titolo.text.toString(),luogo.text.toString(),tvSelectedDate.text.toString(),tvSelectedTimer.text.toString(),intent.getStringExtra("id_appuntamento"),this)
            }
                else viewModel.inserisciDatiAppuntamenti(uid,titolo.text.toString(),luogo.text.toString(),tvSelectedDate.text.toString(),tvSelectedTimer.text.toString(),this)

                viewModel.risultato.observe(this,  { success ->
                    if (success) {
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                })
        }
        }

    }
    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            this, {DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, monthOfYear, dayOfMonth)
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val formattedDate = dateFormat.format(selectedDate.time)
                tvSelectedDate.text = "$formattedDate"
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }
}