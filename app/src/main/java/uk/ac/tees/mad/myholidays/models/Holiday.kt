package uk.ac.tees.mad.myholidays.models

import java.time.LocalDate

data class Holiday(
    val id: String,
    val name: String,
    val date: LocalDate,
    val country: String,
    val description: String? = null,
    val type: String? = null,
    val imageUrl: String? = null
)
