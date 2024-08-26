package com.example.navigation
import org.junit.Assert.assertEquals
import org.junit.Test

// Data class per MedicinaData e ExpandedMedicineItem
data class MedicinaData(
    val id: String = "",
    val user_id: String = "",
    val nome: String = "",
    val numero_medicine: String = "",
    val Numero_Per_Giorno: String = "",
    val Data_inizio: String = "",
    val foto: String? = null,
    val orari: Map<String, Orario> = emptyMap()
) {
    data class Orario(
        val orario: String = "",
        val stato: Boolean = false
    )
}

data class ExpandedMedicineItem(
    val id: String = "",
    val user_id: String = "",
    val nome: String = "",
    val numero_medicine: String = "",
    val Numero_Per_Giorno: String = "",
    val Data_inizio: String = "",
    val foto: String? = null,
    val orario: String = "",
    val stato: Boolean = false
)

// Metodo da testare
fun prepareExpandedData(medicinaDataList: List<MedicinaData>): List<ExpandedMedicineItem> {
    val expandedList = mutableListOf<ExpandedMedicineItem>()

    for (medicina in medicinaDataList) {
        medicina.orari.forEach { (id, orario) ->
            if (orario.stato == false) {
                expandedList.add(
                    ExpandedMedicineItem(
                        id = medicina.id,
                        user_id = medicina.user_id,
                        nome = medicina.nome,
                        numero_medicine = medicina.numero_medicine,
                        Numero_Per_Giorno = medicina.Numero_Per_Giorno,
                        Data_inizio = medicina.Data_inizio,
                        foto = medicina.foto,
                        orario = orario.orario,
                        stato = orario.stato
                    )
                )
            }
        }
    }

    return expandedList.sortedBy { it.orario }
}


class MedicinaViewModelTest {

    @Test
    fun testPrepareExpandedData() {
        val medicinaDataList = listOf(
            MedicinaData(
                id = "1",
                user_id = "user1",
                nome = "Medicine A",
                numero_medicine = "10",
                Numero_Per_Giorno = "2",
                Data_inizio = "2024-08-25",
                foto = "fotoA",
                orari = mapOf(
                    "1" to MedicinaData.Orario(orario = "08:00", stato = false),
                    "2" to MedicinaData.Orario(orario = "12:00", stato = true)
                )
            ),
            MedicinaData(
                id = "2",
                user_id = "user2",
                nome = "Medicine B",
                numero_medicine = "5",
                Numero_Per_Giorno = "1",
                Data_inizio = "2024-08-26",
                foto = "fotoB",
                orari = mapOf(
                    "3" to MedicinaData.Orario(orario = "09:00", stato = false)
                )
            )
        )

        val expected = listOf(
            ExpandedMedicineItem(
                id = "1",
                user_id = "user1",
                nome = "Medicine A",
                numero_medicine = "10",
                Numero_Per_Giorno = "2",
                Data_inizio = "2024-08-25",
                foto = "fotoA",
                orario = "08:00",
                stato = false
            ),
            ExpandedMedicineItem(
                id = "2",
                user_id = "user2",
                nome = "Medicine B",
                numero_medicine = "5",
                Numero_Per_Giorno = "1",
                Data_inizio = "2024-08-26",
                foto = "fotoB",
                orario = "09:00",
                stato = false
            )
        )

        // Esecuzione del metodo da testare
        val result = prepareExpandedData(medicinaDataList)

        // Verifica del risultato
        assertEquals(expected, result)
    }
}
