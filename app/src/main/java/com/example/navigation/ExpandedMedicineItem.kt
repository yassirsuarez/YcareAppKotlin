package com.example.navigation

data class ExpandedMedicineItem(
    val id:String="",
    val user_id: String = "",
    val nome: String = "",
    val numero_medicine: String = "",
    val Numero_Per_Giorno: String = "",
    val Data_inizio: String = "",
    val foto: String? = null,
    val orario: String = "",
    val stato:Boolean= false
)
