package com.jnetai.churnpredictor.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jnetai.churnpredictor.ChurnPredictorApp
import com.jnetai.churnpredictor.model.*
import com.jnetai.churnpredictor.util.ChurnCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = (application as ChurnPredictorApp).database
    private val customerDao = db.customerDao()
    private val interactionDao = db.interactionDao()

    val customers: StateFlow<List<Customer>> = customerDao.getAllCustomers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedCustomer = MutableStateFlow<Customer?>(null)
    val selectedCustomer: StateFlow<Customer?> = _selectedCustomer

    val interactionsForSelected: StateFlow<List<Interaction>> = _selectedCustomer.flatMapLatest { c ->
        if (c != null) interactionDao.getForCustomer(c.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _customerRisks = MutableStateFlow<List<CustomerRisk>>(emptyList())
    val customerRisks: StateFlow<List<CustomerRisk>> = _customerRisks

    private val _dashboardStats = MutableStateFlow(DashboardStats())
    val dashboardStats: StateFlow<DashboardStats> = _dashboardStats

    data class DashboardStats(
        val totalCustomers: Int = 0,
        val criticalCount: Int = 0,
        val highCount: Int = 0,
        val mediumCount: Int = 0,
        val lowCount: Int = 0,
        val avgRiskScore: Float = 0f,
        val monthlyRetentionEstimate: Float = 0f
    )

    init {
        viewModelScope.launch {
            customers.collect { recalculateRisks() }
        }
    }

    fun addCustomer(customer: Customer) {
        viewModelScope.launch(Dispatchers.IO) {
            customerDao.insert(customer)
        }
    }

    fun updateCustomer(customer: Customer) {
        viewModelScope.launch(Dispatchers.IO) {
            customerDao.update(customer)
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch(Dispatchers.IO) {
            interactionDao.deleteForCustomer(customer.id)
            customerDao.delete(customer)
        }
    }

    fun selectCustomer(customer: Customer?) {
        _selectedCustomer.value = customer
    }

    fun addInteraction(interaction: Interaction) {
        viewModelScope.launch(Dispatchers.IO) {
            interactionDao.insert(interaction)
        }
    }

    fun deleteInteraction(interaction: Interaction) {
        viewModelScope.launch(Dispatchers.IO) {
            interactionDao.delete(interaction)
        }
    }

    private suspend fun recalculateRisks() {
        val allCustomers = customerDao.getAllCustomersSync()
        val risks = mutableListOf<CustomerRisk>()
        for (c in allCustomers) {
            val interactions = interactionDao.getForCustomerSync(c.id)
            risks.add(ChurnCalculator.calculateRisk(c, interactions))
        }
        _customerRisks.value = risks

        val total = risks.size
        val critical = risks.count { it.riskLevel == RiskLevel.CRITICAL }
        val high = risks.count { it.riskLevel == RiskLevel.HIGH }
        val medium = risks.count { it.riskLevel == RiskLevel.MEDIUM }
        val low = risks.count { it.riskLevel == RiskLevel.LOW }
        val avgScore = if (total > 0) risks.map { it.riskScore }.average().toFloat() else 0f
        val retention = if (total > 0) ((total - critical - high).toFloat() / total) * 100 else 100f

        _dashboardStats.value = DashboardStats(
            totalCustomers = total,
            criticalCount = critical,
            highCount = high,
            mediumCount = medium,
            lowCount = low,
            avgRiskScore = avgScore,
            monthlyRetentionEstimate = retention
        )
    }

    fun getAllInteractionsSync(): List<Interaction> {
        var result = emptyList<Interaction>()
        viewModelScope.launch(Dispatchers.IO) {
            // not ideal but we need sync access for export
        }
        return result
    }

    suspend fun getAllInteractions(): List<Interaction> {
        return interactionDao.getAllInteractions().first()
    }

    suspend fun getAllCustomersSync(): List<Customer> {
        return customerDao.getAllCustomersSync()
    }
}