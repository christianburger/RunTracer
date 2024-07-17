package com.runtracer.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.runtracer.model.ActivityData
import com.runtracer.viewmodel.ActivityViewModel

@Composable
fun ActivityScreen(
    navController: NavHostController,
    viewModel: ActivityViewModel = viewModel()
) {
    val context = LocalContext.current

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    if (!hasLocationPermission) {
        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            hasLocationPermission = isGranted
        }
        LaunchedEffect(Unit) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Activity Screen", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { viewModel.startActivityTracking("Running") }) {
            Text("Start Running")
        }
        Button(onClick = { viewModel.startActivityTracking("Cycling") }) {
            Text("Start Cycling")
        }
        Button(onClick = { viewModel.startActivityTracking("Swimming") }) {
            Text("Start Swimming")
        }
        Button(onClick = { viewModel.startActivityTracking("Treadmill") }) {
            Text("Start Treadmill")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(text = "Tracking: ${(uiState as? ActivityData.Success)?.activityType ?: "None"}", style = MaterialTheme.typography.titleMedium)
        Text(text = "Duration: ${(uiState as? ActivityData.Success)?.totalDurationSeconds ?: 0} seconds", style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { viewModel.togglePauseResume() }) {
            Text(if ((uiState as? ActivityData.Success)?.isPaused == true) "Resume" else "Pause")
        }
        Button(onClick = { viewModel.stopActivityTracking() }) {
            Text("Stop")
        }
    }
}
