package com.jnetai.churnpredictor.util

import com.jnetai.churnpredictor.model.*
import java.time.LocalDate
import java.time.temporal.ChronoUnit

object ChurnCalculator {

    fun calculateRisk(customer: Customer, interactions: List<Interaction>): CustomerRisk {
        val today = LocalDate.now()
        val daysInactive = calculateDaysInactive(interactions, today)
        val recentComplaints = countRecentComplaints(interactions)
        val hasDowngraded = hasRecentDowngrade(interactions)

        val score = computeScore(daysInactive, recentComplaints, hasDowngraded, interactions)
        val level = scoreToLevel(score)

        return CustomerRisk(
            customer = customer,
            riskLevel = level,
            riskScore = score,
            daysInactive = daysInactive,
            recentComplaints = recentComplaints,
            hasDowngraded = hasDowngraded
        )
    }

    private fun calculateDaysInactive(interactions: List<Interaction>, today: LocalDate): Int {
        val logins = interactions.filter { it.type == InteractionType.LOGIN.name }
        if (logins.isEmpty()) {
            return 999 // No logins ever
        }
        val lastLogin = logins.maxOfOrNull {
            LocalDate.parse(it.date)
        } ?: return 999
        return ChronoUnit.DAYS.between(lastLogin, today).toInt().coerceAtLeast(0)
    }

    private fun countRecentComplaints(interactions: List<Interaction>): Int {
        val thirtyDaysAgo = LocalDate.now().minusDays(30).toString()
        return interactions.count {
            it.type == InteractionType.SUPPORT_TICKET.name && it.date >= thirtyDaysAgo
        }
    }

    private fun hasRecentDowngrade(interactions: List<Interaction>): Boolean {
        val thirtyDaysAgo = LocalDate.now().minusDays(30).toString()
        return interactions.any {
            it.type == InteractionType.PLAN_DOWNGRADE.name && it.date >= thirtyDaysAgo
        }
    }

    private fun computeScore(daysInactive: Int, recentComplaints: Int, hasDowngraded: Boolean, interactions: List<Interaction>): Float {
        var score = 0f

        // Inactivity factor (0-40 points)
        score += when {
            daysInactive > 60 -> 40f
            daysInactive > 30 -> 30f
            daysInactive > 14 -> 20f
            daysInactive > 7 -> 10f
            else -> 0f
        }

        // Complaint factor (0-30 points)
        score += when {
            recentComplaints >= 3 -> 30f
            recentComplaints == 2 -> 20f
            recentComplaints == 1 -> 10f
            else -> 0f
        }

        // Downgrade factor (0-20 points)
        if (hasDowngraded) score += 20f

        // Low feedback score factor (0-10 points)
        val feedbacks = interactions.filter {
            it.type == InteractionType.FEEDBACK.name && it.feedbackScore != null
        }
        if (feedbacks.isNotEmpty()) {
            val avgFeedback = feedbacks.map { it.feedbackScore!! }.average().toFloat()
            if (avgFeedback < 2f) score += 10f
            else if (avgFeedback < 3f) score += 5f
        }

        return score.coerceIn(0f, 100f)
    }

    private fun scoreToLevel(score: Float): RiskLevel = when {
        score >= 70 -> RiskLevel.CRITICAL
        score >= 50 -> RiskLevel.HIGH
        score >= 25 -> RiskLevel.MEDIUM
        else -> RiskLevel.LOW
    }

    fun getStrategies(): Map<RiskLevel, RetentionStrategy> = mapOf(
        RiskLevel.LOW to RetentionStrategy(
            RiskLevel.LOW,
            "Maintain & Nurture",
            "Low-risk customers who are engaged. Keep them happy.",
            listOf("Send appreciation emails", "Offer loyalty rewards", "Request referrals", "Share product updates")
        ),
        RiskLevel.MEDIUM to RetentionStrategy(
            RiskLevel.MEDIUM,
            "Engage & Monitor",
            "Showing early signs of disengagement. Proactive outreach needed.",
            listOf("Schedule check-in calls", "Offer training sessions", "Provide usage tips", "Send personalized recommendations")
        ),
        RiskLevel.HIGH to RetentionStrategy(
            RiskLevel.HIGH,
            "Intervene & Retain",
            "At significant risk. Immediate action required.",
            listOf("Assign dedicated account manager", "Offer discount or credit", "Create custom retention plan", "Escalate support tickets")
        ),
        RiskLevel.CRITICAL to RetentionStrategy(
            RiskLevel.CRITICAL,
            "Emergency Save",
            "Extremely likely to churn. All hands on deck.",
            listOf("Executive outreach call", "Offer significant incentive", "Free plan upgrade (temporary)", "On-site visit if B2B", "Personal apology for any issues")
        )
    )
}