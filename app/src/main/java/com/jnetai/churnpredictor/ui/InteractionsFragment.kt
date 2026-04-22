package com.jnetai.churnpredictor.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModelProvider
import com.jnetai.churnpredictor.R
import com.jnetai.churnpredictor.databinding.FragmentInteractionsBinding
import com.jnetai.churnpredictor.databinding.DialogAddInteractionBinding
import com.jnetai.churnpredictor.model.Customer
import com.jnetai.churnpredictor.model.Interaction
import com.jnetai.churnpredictor.model.InteractionType
import com.jnetai.churnpredictor.model.RiskLevel
import com.jnetai.churnpredictor.util.ChurnCalculator

class InteractionsFragment : Fragment() {

    private var _binding: FragmentInteractionsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: InteractionAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentInteractionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        adapter = InteractionAdapter(
            onDelete = { viewModel.deleteInteraction(it) }
        )
        binding.recyclerInteractions.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selectedCustomer.collect { customer ->
                if (customer != null) {
                    binding.tvCustomerName.text = customer.name
                    binding.tvCustomerEmail.text = customer.email
                    showRiskBadge(customer)
                    showRetentionStrategy(customer)
                } else {
                    binding.tvCustomerName.text = "No customer selected"
                    binding.tvCustomerEmail.text = ""
                    binding.strategyContainer.removeAllViews()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.interactionsForSelected.collect { interactions ->
                adapter.submitList(interactions)
            }
        }

        binding.fabAddInteraction.setOnClickListener {
            if (viewModel.selectedCustomer.value == null) {
                Toast.makeText(requireContext(), "Select a customer first", Toast.LENGTH_SHORT).show()
            } else {
                showAddInteractionDialog()
            }
        }
    }

    private fun showRiskBadge(customer: Customer) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.customerRisks.collect { risks ->
                val risk = risks.find { it.customer.id == customer.id }
                if (risk != null) {
                    binding.tvRiskLevel.text = risk.riskLevel.name
                    val color = when (risk.riskLevel) {
                        RiskLevel.CRITICAL -> requireContext().getColor(R.color.risk_critical)
                        RiskLevel.HIGH -> requireContext().getColor(R.color.risk_high)
                        RiskLevel.MEDIUM -> requireContext().getColor(R.color.risk_medium)
                        RiskLevel.LOW -> requireContext().getColor(R.color.risk_low)
                    }
                    binding.tvRiskLevel.setBackgroundColor(color)
                    binding.tvRiskScore.text = "Score: ${String.format("%.0f", risk.riskScore)}"
                }
            }
        }
    }

    private fun showRetentionStrategy(customer: Customer) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.customerRisks.collect { risks ->
                val risk = risks.find { it.customer.id == customer.id }
                val strategy = ChurnCalculator.getStrategies()[risk?.riskLevel ?: RiskLevel.LOW]
                if (strategy != null) {
                    binding.strategyContainer.removeAllViews()
                    val titleView = LayoutInflater.from(requireContext()).inflate(R.layout.item_strategy, binding.strategyContainer, false)
                    // We'll just set the title and actions
                    val tvTitle = titleView.findViewById<android.widget.TextView>(R.id.tvStrategyTitle)
                    val tvDesc = titleView.findViewById<android.widget.TextView>(R.id.tvStrategyDesc)
                    val tvActions = titleView.findViewById<android.widget.TextView>(R.id.tvStrategyActions)
                    tvTitle.text = strategy.title
                    tvDesc.text = strategy.description
                    tvActions.text = strategy.actions.joinToString("\n• ", prefix = "• ")
                    binding.strategyContainer.addView(titleView)
                }
            }
        }
    }

    private fun showAddInteractionDialog() {
        val dialogBinding = DialogAddInteractionBinding.inflate(layoutInflater)
        val typeNames = InteractionType.values().map { it.displayName }
        dialogBinding.spinnerType.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, typeNames)
        dialogBinding.etDate.setText(java.time.LocalDate.now().toString())

        AlertDialog.Builder(requireContext())
            .setTitle("Log Interaction")
            .setView(dialogBinding.root)
            .setPositiveButton("Add") { _, _ ->
                val typeIndex = dialogBinding.spinnerType.selectedItemPosition
                val type = InteractionType.values()[typeIndex].name
                val desc = dialogBinding.etDescription.text.toString().trim()
                val date = dialogBinding.etDate.text.toString().trim()
                val feedbackStr = dialogBinding.etFeedbackScore.text.toString().trim()
                val feedbackScore = feedbackStr.toFloatOrNull()?.coerceIn(1f, 5f)

                if (desc.isBlank()) {
                    Toast.makeText(requireContext(), "Description is required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val interaction = Interaction(
                    customerId = viewModel.selectedCustomer.value!!.id,
                    type = type,
                    description = desc,
                    date = date.ifBlank { java.time.LocalDate.now().toString() },
                    feedbackScore = feedbackScore
                )
                viewModel.addInteraction(interaction)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
