import org.junit.Assert.assertEquals
import org.junit.Test

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

fun updatePresa(medicina: MedicinaData): MedicinaData {
    val numOrari = medicina.orari.size

    val updatedNumeroMedicine = medicina.numero_medicine.toIntOrNull()?.let { it - numOrari }?.toString() ?: medicina.numero_medicine

    val updatedOrari = medicina.orari.mapValues { entry ->
        entry.value.copy(stato = true)
    }

    return medicina.copy(numero_medicine = updatedNumeroMedicine, orari = updatedOrari)
}

fun resetMedicina(medicina: MedicinaData): MedicinaData {
    val updatedOrari = medicina.orari.mapValues { entry ->
        entry.value.copy(stato = false)
    }
    return medicina.copy(orari = updatedOrari)
}

class MedicinaDataTest {

    @Test
    fun testUpdatePresa() {
        val medicina = MedicinaData(
            id = "medicina_1",
            user_id = "user_123",
            nome = "Aspirina",
            numero_medicine = "30",
            Numero_Per_Giorno = "2",
            Data_inizio = "2024-08-01",
            orari = mapOf(
                "08:00" to MedicinaData.Orario(orario = "08:00", stato = false),
                "20:00" to MedicinaData.Orario(orario = "20:00", stato = false)
            )
        )

        println("Medicina iniziale:")
        println(medicina)

        val updatedMedicina = updatePresa(medicina)

        println("Medicina dopo updatePresa:")
        println(updatedMedicina)

        updatedMedicina.orari.values.forEach { orario ->
            assertEquals(true, orario.stato)
        }

        val resetMedicina = resetMedicina(updatedMedicina)

        println("Medicina dopo resetPresa:")
        println(resetMedicina)

        resetMedicina.orari.values.forEach { orario ->
            assertEquals(false, orario.stato)
        }
    }
}
