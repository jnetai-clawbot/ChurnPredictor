package com.jnetai.churnpredictor.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.jnetai.churnpredictor.BuildConfig
import com.jnetai.churnpredictor.R
import com.jnetai.churnpredictor.databinding.FragmentAboutBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

class AboutFragment : Fragment() {

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MainViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        binding.tvVersion.text = "Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
        binding.tvAppName.text = "ChurnPredictor"

        binding.btnCheckUpdate.setOnClickListener { checkForUpdates() }
        binding.btnShare.setOnClickListener { shareApp() }
    }

    private fun checkForUpdates() {
        binding.btnCheckUpdate.isEnabled = false
        binding.btnCheckUpdate.text = "Checking..."

        lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    val url = URL("https://api.github.com/repos/jnetai-clawbot/ChurnPredictor/releases/latest")
                    val conn = url.openConnection()
                    conn.setRequestProperty("Accept", "application/vnd.github+json")
                    val json = conn.getInputStream().bufferedReader().readText()
                    val obj = JSONObject(json)
                    val tagName = obj.optString("tag_name", "unknown")
                    val htmlUrl = obj.optString("html_url", "")
                    "$tagName|$htmlUrl"
                }
                val parts = result.split("|")
                val remoteVersion = parts[0]
                val releaseUrl = parts.getOrElse(1) { "" }

                if (remoteVersion != BuildConfig.VERSION_NAME) {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Update Available")
                        .setMessage("New version $remoteVersion is available.")
                        .setPositiveButton("Download") { _, _ ->
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(releaseUrl))
                            startActivity(intent)
                        }
                        .setNegativeButton("Later", null)
                        .show()
                } else {
                    Toast.makeText(requireContext(), "You're on the latest version!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to check updates: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.btnCheckUpdate.isEnabled = true
                binding.btnCheckUpdate.text = "Check for Updates"
            }
        }
    }

    private fun shareApp() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "ChurnPredictor")
            putExtra(Intent.EXTRA_TEXT, "Check out ChurnPredictor - Customer Churn Prediction App!\nhttps://github.com/jnetai-clawbot/ChurnPredictor")
        }
        startActivity(Intent.createChooser(intent, "Share via"))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}