package com.runtracer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HealthScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Health Screen", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        // Add more UI components specific to HealthScreen here
        Text(text = "Here you can track health metrics such as heart rate, steps, etc.", style = MaterialTheme.typography.bodyMedium)

        // Example buttons for navigating or performing actions
        Button(onClick = { /* TODO: Implement action */ }) {
            Text("View Health Data")
        }
    }
}
