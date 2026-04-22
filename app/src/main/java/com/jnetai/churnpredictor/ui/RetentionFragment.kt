package com.jnetai.churnpredictor.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModelProvider
import com.jnetai.churnpredictor.databinding.FragmentRetentionBinding
import com.jnetai.churnpredictor.model.RiskLevel
import com.jnetai.churnpredictor.util.ChurnCalculator

class RetentionFragment : Fragment() {

    private var _binding: FragmentRetentionBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MainViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRetentionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.customerRisks.collect { risks ->
                updateStrategies(risks)
            }
        }
    }

    private fun updateStrategies(risks: List<com.jnetai.churnpredictor.model.CustomerRisk>) {
        binding.strategyContainer.removeAllViews()
        val strategies = ChurnCalculator.getStrategies()

        // Show all risk level strategies
        for (level in listOf(RiskLevel.CRITICAL, RiskLevel.HIGH, RiskLevel.MEDIUM, RiskLevel.LOW)) {
            val strategy = strategies[level] ?: continue
            val count = risks.count { it.riskLevel == level }
            val row = layoutInflater.inflate(com.jnetai.churnpredictor.R.layout.item_strategy_card, binding.strategyContainer, false)
            row.findViewById<android.widget.TextView>(com.jnetai.churnpredictor.R.id.tvStrategyTitle).text = "${strategy.title} ($count customers)"
            row.findViewById<android.widget.TextView>(com.jnetai.churnpredictor.R.id.tvStrategyDesc).text = strategy.description
            row.findViewById<android.widget.TextView>(com.jnetai.churnpredictor.R.id.tvStrategyActions).text = strategy.actions.joinToString("\n• ", prefix = "• ")

            val color = when (level) {
                RiskLevel.CRITICAL -> requireContext().getColor(com.jnetai.churnpredictor.R.color.risk_critical)
                RiskLevel.HIGH -> requireContext().getColor(com.jnetai.churnpredictor.R.color.risk_high)
                RiskLevel.MEDIUM -> requireContext().getColor(com.jnetai.churnpredictor.R.color.risk_medium)
                RiskLevel.LOW -> requireContext().getColor(com.jnetai.churnpredictor.R.color.risk_low)
            }
            row.findViewById<View>(com.jnetai.churnpredictor.R.id.strategyIndicator).setBackgroundColor(color)
            binding.strategyContainer.addView(row)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
