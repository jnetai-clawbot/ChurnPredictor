package com.jnetai.churnpredictor.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

enum class PlanType(val displayName: String) {
    FREE("Free"),
    BASIC("Basic"),
    PRO("Pro"),
    ENTERPRISE("Enterprise")
}

enum class InteractionType(val displayName: String) {
    SUPPORT_TICKET("Support Ticket"),
    PURCHASE("Purchase"),
    LOGIN("Login"),
    FEEDBACK("Feedback"),
    PLAN_DOWNGRADE("Plan Downgrade"),
    PLAN_UPGRADE("Plan Upgrade")
}

@Entity(tableName = "customers", indices = [Index(value = ["email"], unique = true)])
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val email: String,
    val signupDate: String, // ISO date string yyyy-MM-dd
    val planType: String,   // PlanType name
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "interactions", indices = [Index(value = ["customerId"])])
data class Interaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val type: String,        // InteractionType name
    val description: String,
    val date: String,        // ISO date string yyyy-MM-dd
    val feedbackScore: Float? = null, // 1-5 for feedback type
    val createdAt: Long = System.currentTimeMillis()
)

enum class RiskLevel { LOW, MEDIUM, HIGH, CRITICAL }

data class CustomerRisk(
    val customer: Customer,
    val riskLevel: RiskLevel,
    val riskScore: Float, // 0-100
    val daysInactive: Int,
    val recentComplaints: Int,
    val hasDowngraded: Boolean
)

data class RetentionStrategy(
    val riskLevel: RiskLevel,
    val title: String,
    val description: String,
    val actions: List<String>
)