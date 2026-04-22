package com.jnetai.churnpredictor.util

import com.jnetai.churnpredictor.model.Customer
import com.jnetai.churnpredictor.model.Interaction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object JsonExporter {

    private val gson = Gson()

    data class ExportData(
        val exportDate: String,
        val customers: List<CustomerExport>,
        val interactions: List<Interaction>
    )

    data class CustomerExport(
        val id: Long,
        val name: String,
        val email: String,
        val signupDate: String,
        val planType: String,
        val notes: String
    )

    fun exportToJson(customers: List<Customer>, interactions: List<Interaction>): String {
        val exports = customers.map { c ->
            CustomerExport(c.id, c.name, c.email, c.signupDate, c.planType, c.notes)
        }
        val data = ExportData(
            exportDate = java.time.LocalDate.now().toString(),
            customers = exports,
            interactions = interactions
        )
        return gson.toJson(data)
    }
}