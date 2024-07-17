package com.runtracer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.runtracer.ui.theme.RunTracerTheme
import com.runtracer.ui.ActivityScreen
import com.runtracer.ui.HealthScreen
import com.runtracer.ui.SleepScreen
import com.runtracer.ui.DeviceScreen
import com.runtracer.viewmodel.ActivityViewModel

class MainActivity : ComponentActivity() {
    private val activityViewModel: ActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RunTracerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(activityViewModel = activityViewModel)
                }
            }
        }
    }
}

@Composable
fun MainScreen(activityViewModel: ActivityViewModel) {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        NavigationGraph(navController, Modifier.padding(innerPadding), activityViewModel)
    }
}

@Composable
fun NavigationGraph(navController: NavHostController, modifier: Modifier = Modifier, activityViewModel: ActivityViewModel) {
    NavHost(navController, startDestination = BottomNavItem.Activity.screen_route, modifier = modifier) {
        composable(BottomNavItem.Activity.screen_route) { ActivityScreen(navController = navController, viewModel = activityViewModel) }
        composable(BottomNavItem.Health.screen_route) { HealthScreen() }
        composable(BottomNavItem.Sleep.screen_route) { SleepScreen() }
        composable(BottomNavItem.Device.screen_route) { DeviceScreen() }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem.Activity,
        BottomNavItem.Health,
        BottomNavItem.Sleep,
        BottomNavItem.Device
    )

    BottomAppBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = item.icon),
                        contentDescription = stringResource(id = item.title)
                    )
                },
                label = { Text(stringResource(id = item.title)) },
                selected = currentRoute == item.screen_route,
                onClick = {
                    navController.navigate(item.screen_route) {
                        navController.graph.startDestinationRoute?.let {
                            popUpTo(it) { saveState = true }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

sealed class BottomNavItem(val title: Int, val icon: Int, val screen_route: String) {
    object Activity : BottomNavItem(R.string.screen_activity, R.drawable.ic_run, "activity")
    object Health : BottomNavItem(R.string.screen_health, R.drawable.ic_heart, "health")
    object Sleep : BottomNavItem(R.string.screen_sleep, R.drawable.ic_sleep, "sleep")
    object Device : BottomNavItem(R.string.screen_device, R.drawable.ic_device, "device")
}
