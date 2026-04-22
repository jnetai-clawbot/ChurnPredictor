package com.jnetai.churnpredictor.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModelProvider
import com.jnetai.churnpredictor.R
import com.jnetai.churnpredictor.databinding.FragmentExportBinding
import com.jnetai.churnpredictor.util.JsonExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class ExportFragment : Fragment() {

    private var _binding: FragmentExportBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MainViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentExportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        binding.btnExportJson.setOnClickListener { exportToJson() }
        binding.btnShareJson.setOnClickListener { shareJson() }
    }

    private fun exportToJson() {
        lifecycleScope.launch {
            try {
                val customers = viewModel.getAllCustomersSync()
                val interactions = viewModel.getAllInteractions()
                val json = JsonExporter.exportToJson(customers, interactions)

                withContext(Dispatchers.IO) {
                    val dir = File(requireContext().getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS), "exports")
                    dir.mkdirs()
                    val file = File(dir, "churn_predictor_export_${System.currentTimeMillis()}.json")
                    file.writeText(json)
                    withContext(Dispatchers.Main) {
                        binding.tvExportPath.text = "Saved to: ${file.absolutePath}"
                        Toast.makeText(requireContext(), "Exported ${customers.size} customers", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun shareJson() {
        lifecycleScope.launch {
            try {
                val customers = viewModel.getAllCustomersSync()
                val interactions = viewModel.getAllInteractions()
                val json = JsonExporter.exportToJson(customers, interactions)

                val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(android.content.Intent.EXTRA_TEXT, json)
                    putExtra(android.content.Intent.EXTRA_SUBJECT, "ChurnPredictor Data Export")
                }
                startActivity(android.content.Intent.createChooser(shareIntent, "Share export"))
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Share failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
