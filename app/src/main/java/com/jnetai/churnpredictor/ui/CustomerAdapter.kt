package com.jnetai.churnpredictor.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jnetai.churnpredictor.databinding.ItemCustomerBinding
import com.jnetai.churnpredictor.model.Customer
import com.jnetai.churnpredictor.model.PlanType

class CustomerAdapter(
    private val onClick: (Customer) -> Unit,
    private val onLongClick: (Customer) -> Unit
) : ListAdapter<Customer, CustomerAdapter.ViewHolder>(CustomerDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCustomerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val customer = getItem(position)
        holder.bind(customer)
    }

    inner class ViewHolder(private val binding: ItemCustomerBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(customer: Customer) {
            binding.tvName.text = customer.name
            binding.tvEmail.text = customer.email
            binding.tvPlan.text = try {
                PlanType.valueOf(customer.planType).displayName
            } catch (e: Exception) {
                customer.planType
            }
            binding.tvSignupDate.text = customer.signupDate

            binding.root.setOnClickListener { onClick(customer) }
            binding.root.setOnLongClickListener {
                onLongClick(customer)
                true
            }
        }
    }

    class CustomerDiffCallback : DiffUtil.ItemCallback<Customer>() {
        override fun areItemsTheSame(oldItem: Customer, newItem: Customer) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Customer, newItem: Customer) = oldItem == newItem
    }
}