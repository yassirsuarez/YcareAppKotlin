package com.example.navigation

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.example.navigation.databinding.HomeBinding
import java.util.Calendar
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.example.navigation.databinding.PopupOrarioDottoreBinding
import androidx.core.content.ContextCompat

class Home : Fragment() {
    private lateinit var binding: HomeBinding
    private val viewModel: DottoreViewModel by viewModels()
    private lateinit var fotoDoctor: ImageView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = HomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fotoDoctor = binding.fotoDottore
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.aggiungiDott.setOnClickListener {
            val intent = Intent(requireContext(), DatiDottore::class.java)
            startActivity(intent)
        }
        var imageUri: String? = null
        val calendario = Calendar.getInstance()
        val giornoSettimana = calendario.get(Calendar.DAY_OF_WEEK)
        val giornoOdierno = getDay(giornoSettimana)
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val currentTime = getCurrentTime()
        viewModel.datiDottore()

        viewModel.orari.observe(viewLifecycleOwner, Observer { orari ->
            orari?.let {
                if (it.containsKey(giornoOdierno)) {
                    val orariOdierni = it[giornoOdierno]

                    val mattinaInizio = orariOdierni?.get("MattinaInizio")?.let {
                        if (it == "--:--") {
                            null
                        } else {
                            LocalTime.parse(it, formatter)
                        }
                    }
                    val mattinaFine = orariOdierni?.get("MattinaFine")?.let {
                        if (it == "--:--") {
                            null
                        } else {
                            LocalTime.parse(it, formatter)
                        }
                    }
                    val pomeriggioInizio = orariOdierni?.get("PomeriggioInizio")?.let {
                        if (it == "--:--") {
                            null
                        } else {
                            LocalTime.parse(it, formatter)

                        }
                    }
                    val pomeriggioFine = orariOdierni?.get("PomeriggioFine")?.let {
                        if (it == "--:--") {
                            null
                        } else {
                            LocalTime.parse(it, formatter)
                        }
                    }

                    binding.mattinaInizio.setText(mattinaInizio?.toString() ?: "N/A")
                    binding.mattinaFine.setText(mattinaFine?.toString() ?: "N/A")
                    binding.pomeriggioInizio.setText(pomeriggioInizio?.toString() ?: "N/A")
                    binding.pomeriggioFine.setText(pomeriggioFine?.toString() ?: "N/A")

                    binding.luogoDott.setOnClickListener {
                        viewModel.luogo.observe(viewLifecycleOwner) { luogo ->
                            luogo?.let {
                                openAddressInGoogleMaps(luogo)
                            }
                        }
                    }

                    val currentLocalTime = LocalTime.parse(currentTime, formatter)

                    if (mattinaInizio != null && mattinaFine != null &&
                        currentLocalTime.isAfter(mattinaInizio) && currentLocalTime.isBefore(
                            mattinaFine
                        )
                    ) {
                        binding.StatoDott.setText("Disponibile")
                        binding.StatoDott.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark))
                    } else if (pomeriggioInizio != null && pomeriggioFine != null &&
                        currentLocalTime.isAfter(pomeriggioInizio) && currentLocalTime.isBefore(
                            pomeriggioFine
                        )
                    ) {
                        binding.StatoDott.setText("Disponibile")
                        binding.StatoDott.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark))

                    } else {
                        binding.StatoDott.setText("Non Reperibile")
                        binding.StatoDott.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))

                    }
                }

                binding.orarioDottore.setOnClickListener {

                    val inflater = LayoutInflater.from(requireContext())
                    val bindingPopup = DataBindingUtil.inflate<PopupOrarioDottoreBinding>(
                        inflater, R.layout.popup_orario_dottore, null, false
                    )
                    bindingPopup.viewModel = viewModel
                    bindingPopup.lifecycleOwner = this
                    val alertDialog = AlertDialog.Builder(requireContext())
                        .setView(bindingPopup.root)
                        .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                        .create()
                    alertDialog.show()
                }
            }
        })

        viewModel.foto.observe(viewLifecycleOwner, Observer { foto ->
            imageUri = foto
            imageUri?.let { uriString ->
                Glide.with(this)
                    .load(uriString)
                    .into(fotoDoctor)
            }
        })
        binding.dottoreLinearLayout.visibility = View.GONE
        viewModel.nome.observe(viewLifecycleOwner) { nome ->
            nome?.let {
                if (nome.isNotBlank()) {
                    binding.aggiungiDott.visibility = View.GONE
                    binding.dottoreLinearLayout.visibility = View.VISIBLE
                } else {
                    binding.aggiungiDott.visibility = View.VISIBLE
                    binding.dottoreLinearLayout.visibility = View.GONE
                }
            }
        }
        binding.telefonoDottore.setOnClickListener {
            viewModel.numero.observe(viewLifecycleOwner) { numero ->
                numero?.let {
                    val dialIntent = Intent(Intent.ACTION_DIAL)
                    dialIntent.data = Uri.parse("tel:$numero")
                    startActivity(dialIntent)
                }
            }
        }

        binding.modificaDottore.setOnClickListener {
            val intent = Intent(requireContext(), DatiDottore::class.java)
            intent.putExtra("tipo", 1)
            startActivity(intent)
        }
    }

    private fun getDay(giorno: Int): String {
        return when (giorno) {
            Calendar.SUNDAY -> "Domenica"
            Calendar.MONDAY -> "Lunedì"
            Calendar.TUESDAY -> "Martedì"
            Calendar.WEDNESDAY -> "Mercoledì"
            Calendar.THURSDAY -> "Giovedì"
            Calendar.FRIDAY -> "Venerdì"
            Calendar.SATURDAY -> "Sabato"
            else -> ""
        }
    }

    fun getCurrentTime(): String {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY) // 24-hour format
        val minute = calendar.get(Calendar.MINUTE)
        return String.format("%02d:%02d", hour, minute)
    }

    fun openAddressInGoogleMaps(address: String) {
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