package com.example.navigation

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before

@RunWith(AndroidJUnit4::class)
class MedicinaViewModelTest {

    private lateinit var viewModel: MedicinaViewModel

    @Before
    fun setup() {
        // Inizializza il ViewModel
        viewModel = MedicinaViewModel()
    }

    @Test
    fun testPrepareExpandedData() {
        // Dati di esempio
        val orari1 = mapOf(
            "1" to MedicinaData.Orario("08:00", false),
            "2" to MedicinaData.Orario("14:00", true),
            "3" to MedicinaData.Orario("20:00", false)
        )

        val orari2 = mapOf(
            "1" to MedicinaData.Orario("09:00", true),
            "2" to MedicinaData.Orario("15:00", false)
        )

        val medicinaDataList = listOf(
            MedicinaData(
                id = "1",
                user_id = "101",
                nome = "Aspirina",
                numero_medicine = "30",
                Numero_Per_Giorno = "3",
                Data_inizio = "2023-08-01",
                foto = "foto_aspirina.png",
                orari = orari1
            ),
            MedicinaData(
                id = "2",
                user_id = "102",
                nome = "Paracetamolo",
                numero_medicine = "20",
                Numero_Per_Giorno = "2",
                Data_inizio = "2023-08-02",
                foto = "foto_paracetamolo.png",
                orari = orari2
            )
        )

        // Chiamare la funzione da testare tramite il ViewModel
        val result = viewModel.prepareExpandedData(medicinaDataList)

        // Lista attesa
        val expectedList = listOf(
            ExpandedMedicineItem(
                id = "1",
                user_id = "101",
                nome = "Aspirina",
                numero_medicine = "30",
                Numero_Per_Giorno = "3",
                Data_inizio = "2023-08-01",
                foto = "foto_aspirina.png",
                orario = "08:00",
                stato = false
            ),
            ExpandedMedicineItem(
                id = "1",
                user_id = "101",
                nome = "Aspirina",
                numero_medicine = "30",
                Numero_Per_Giorno = "3",
                Data_inizio = "2023-08-01",
                foto = "foto_aspirina.png",
                orario = "20:00",
                stato = false
            ),
            ExpandedMedicineItem(
                id = "2",
                user_id = "102",
                nome = "Paracetamolo",
                numero_medicine = "20",
                Numero_Per_Giorno = "2",
                Data_inizio = "2023-08-02",
                foto = "foto_paracetamolo.png",
                orario = "15:00",
                stato = false
            )
        )

        // Verificare che il risultato sia corretto
        assertEquals(expectedList, result)
    }
}
