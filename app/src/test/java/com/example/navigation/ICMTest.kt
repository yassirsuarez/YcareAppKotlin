import org.junit.Assert.assertEquals
import org.junit.Test

class ICMTest {

    @Test
    fun testCalcolaBmiSottopeso() {
        val result = calcolaBmi(50, 180)
        assertEquals("sottopeso", result.first)
        assertEquals("15,43", result.second)
    }

    @Test
    fun testCalcolaBmiNormopeso() {
        val result = calcolaBmi(70, 175)
        assertEquals("normopeso", result.first)
        assertEquals("22,86", result.second)
    }

    @Test
    fun testCalcolaBmiObesoClasse1() {
        val result = calcolaBmi(95, 160)
        assertEquals("obeso classe 2", result.first)
        assertEquals("37,11", result.second)
    }
}
fun calcolaBmi(peso: Int, altezza: Int): Pair<String, String> {
    val altezzaInMetri = altezza / 100.0
    val risultato = peso / (altezzaInMetri * altezzaInMetri)
    val risultatoFormattato = String.format("%.2f", risultato)

    val testo = when {
        risultato < 18.5 -> "sottopeso"
        risultato >= 18.5 && risultato < 25 -> "normopeso"
        risultato >= 25 && risultato < 30 -> "pre-obeso"
        risultato >= 30 && risultato < 35 -> "obeso classe 1"
        risultato >= 35 && risultato < 40 -> "obeso classe 2"
        risultato >= 40 -> "obeso classe 3"
        else -> "errore"
    }

    return Pair(testo, risultatoFormattato)
}