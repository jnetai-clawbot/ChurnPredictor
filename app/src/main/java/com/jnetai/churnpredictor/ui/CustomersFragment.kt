package com.jnetai.churnpredictor.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.jnetai.churnpredictor.R
import com.jnetai.churnpredictor.databinding.FragmentCustomersBinding
import com.jnetai.churnpredictor.databinding.DialogAddCustomerBinding
import com.jnetai.churnpredictor.model.Customer
import com.jnetai.churnpredictor.model.PlanType

class CustomersFragment : Fragment() {

    private var _binding: FragmentCustomersBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: CustomerAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCustomersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        adapter = CustomerAdapter(
            onClick = { customer -> viewModel.selectCustomer(customer) },
            onLongClick = { customer -> showDeleteDialog(customer) }
        )
        binding.recyclerCustomers.adapter = adapter

        viewLifecycleOwner.lifecycleScopeLaunchCollectors {
            viewModel.customers.collect { customers ->
                adapter.submitList(customers)
            }
        }

        binding.fabAddCustomer.setOnClickListener { showAddCustomerDialog() }
    }

    private fun showAddCustomerDialog() {
        val dialogBinding = DialogAddCustomerBinding.inflate(layoutInflater)
        val planNames = PlanType.values().map { it.displayName }
        dialogBinding.spinnerPlan.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, planNames)
        dialogBinding.etSignupDate.setText(java.time.LocalDate.now().toString())

        AlertDialog.Builder(requireContext())
            .setTitle("Add Customer")
            .setView(dialogBinding.root)
            .setPositiveButton("Add") { _, _ ->
                val name = dialogBinding.etName.text.toString().trim()
                val email = dialogBinding.etEmail.text.toString().trim()
                val signupDate = dialogBinding.etSignupDate.text.toString().trim()
                val planIndex = dialogBinding.spinnerPlan.selectedItemPosition
                val planType = PlanType.values()[planIndex].name

                if (name.isBlank() || email.isBlank()) {
                    Toast.makeText(requireContext(), "Name and email are required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val customer = Customer(
                    name = name,
                    email = email,
                    signupDate = signupDate.ifBlank { java.time.LocalDate.now().toString() },
                    planType = planType
                )
                viewModel.addCustomer(customer)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteDialog(customer: Customer) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Customer?")
            .setMessage("Delete ${customer.name}? This will also delete all their interactions.")
            .setPositiveButton("Delete") { _, _ -> viewModel.deleteCustomer(customer) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun androidx.lifecycle.LifecycleOwner.lifecycleScopeLaunchCollectors(block: suspend () -> Unit) {
        androidx.lifecycle.lifecycleScope.launch {
            block()
        }
    }
}