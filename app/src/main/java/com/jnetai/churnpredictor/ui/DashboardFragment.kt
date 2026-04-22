package com.jnetai.churnpredictor.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModelProvider
import com.jnetai.churnpredictor.R
import com.jnetai.churnpredictor.databinding.FragmentDashboardBinding
import com.jnetai.churnpredictor.model.RiskLevel

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MainViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.dashboardStats.collect { stats ->
                binding.tvTotalCustomers.text = stats.totalCustomers.toString()
                binding.tvCriticalCount.text = stats.criticalCount.toString()
                binding.tvHighCount.text = stats.highCount.toString()
                binding.tvMediumCount.text = stats.mediumCount.toString()
                binding.tvLowCount.text = stats.lowCount.toString()
                binding.tvAvgRisk.text = String.format("%.1f", stats.avgRiskScore)
                binding.tvRetention.text = String.format("%.1f%%", stats.monthlyRetentionEstimate)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.customerRisks.collect { risks ->
                updateRiskList(risks)
            }
        }
    }

    private fun updateRiskList(risks: List<com.jnetai.churnpredictor.model.CustomerRisk>) {
        val sorted = risks.sortedByDescending { it.riskScore }
        binding.riskContainer.removeAllViews()
        for (risk in sorted.take(20)) {
            val row = LayoutInflater.from(requireContext()).inflate(R.layout.item_risk_row, binding.riskContainer, false)
            row.findViewById<TextView>(R.id.tvRiskName).text = risk.customer.name
            row.findViewById<TextView>(R.id.tvRiskScore).text = String.format("%.0f", risk.riskScore)
            row.findViewById<TextView>(R.id.tvRiskLevel).text = risk.riskLevel.name
            val color = when (risk.riskLevel) {
                RiskLevel.CRITICAL -> requireContext().getColor(R.color.risk_critical)
                RiskLevel.HIGH -> requireContext().getColor(R.color.risk_high)
                RiskLevel.MEDIUM -> requireContext().getColor(R.color.risk_medium)
                RiskLevel.LOW -> requireContext().getColor(R.color.risk_low)
            }
            row.findViewById<View>(R.id.riskIndicator).setBackgroundColor(color)
            binding.riskContainer.addView(row)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
