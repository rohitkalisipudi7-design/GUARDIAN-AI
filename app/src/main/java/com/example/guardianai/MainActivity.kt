package com.example.guardianai

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.guardianai.pipeline.EmergencyLocationProvider
import com.example.guardianai.pipeline.EnforceDispatcher
import com.example.guardianai.pipeline.ListenService
import com.example.guardianai.theme.GuardianAITheme
import com.example.guardianai.ui.main.MainScreen

class MainActivity : ComponentActivity() {

  private val permissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestMultiplePermissions()
  ) { permissions ->
    EmergencyLocationProvider.startActiveTracking(this)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    com.example.guardianai.services.ListenerStatus.init(this)
    ListenService.init(this)
    EnforceDispatcher.init(this)
    EmergencyLocationProvider.startActiveTracking(this)

    // Load Gemini API Key from SharedPreferences
    val sharedPrefs = getSharedPreferences("guardian_ai_prefs", android.content.Context.MODE_PRIVATE)
    val apiKey = sharedPrefs.getString("gemini_api_key", "") ?: ""
    com.example.guardianai.pipeline.GeminiApiClient.setApiKey(apiKey)

    val permissionsNeeded = mutableListOf(
      Manifest.permission.ACCESS_FINE_LOCATION,
      Manifest.permission.ACCESS_COARSE_LOCATION,
      Manifest.permission.SEND_SMS
    )
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      permissionsNeeded.add(Manifest.permission.POST_NOTIFICATIONS)
    }

    val ungranted = permissionsNeeded.filter {
      ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
    }
    if (ungranted.isNotEmpty()) {
      permissionLauncher.launch(ungranted.toTypedArray())
    }

    enableEdgeToEdge()
    setContent {
      GuardianAITheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          MainScreen()
        }
      }
    }
  }
}
