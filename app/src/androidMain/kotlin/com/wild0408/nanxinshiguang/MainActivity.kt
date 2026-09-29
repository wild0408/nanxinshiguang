package com.wild0408.nanxinshiguang

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.wild0408.nanxinshiguang.widget.electricity.ElectricityWidgetRenderer
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : AppCompatActivity() {

    private val targetDestinationFlow = MutableStateFlow<Destination?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        targetDestinationFlow.value = parseDestination(intent)

        setContent {
            AndroidAppRoot(
                targetDestinationFlow = targetDestinationFlow
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        parseDestination(intent)?.let { dest ->
            targetDestinationFlow.value = dest
        }
    }

    private fun parseDestination(intent: Intent?): Destination? {
        val extra = intent?.getStringExtra("extra_destination")
            ?: intent?.getStringExtra(ElectricityWidgetRenderer.EXTRA_DESTINATION)
            ?: return null
        return when (extra) {
            ElectricityWidgetRenderer.DESTINATION_ELECTRICITY -> Destination.ElectricityCenter
            else -> null
        }
    }
}
