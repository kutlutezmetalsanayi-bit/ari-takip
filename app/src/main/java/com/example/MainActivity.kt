package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.components.BeeBottomNavBar
import com.example.ui.navigation.Screen
import com.example.ui.screens.AddEditHiveScreen
import com.example.ui.screens.AddFeedingScreen
import com.example.ui.screens.AddInspectionScreen
import com.example.ui.screens.ApiariesScreen
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HiveDetailScreen
import com.example.ui.screens.HivesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.BeeViewModel
import com.example.ui.viewmodel.UiEvent
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        AriTakipApp()
      }
    }
  }
}

@Composable
fun AriTakipApp() {
  val navController = rememberNavController()
  val viewModel: BeeViewModel = viewModel()
  val snackbarHostState = remember { SnackbarHostState() }

  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route

  // Collect UI snackbars
  LaunchedEffect(key1 = true) {
    viewModel.eventFlow.collectLatest { event ->
      when (event) {
        is UiEvent.ShowSnackbar -> {
          snackbarHostState.showSnackbar(event.message)
        }
      }
    }
  }

  val isTopLevelScreen = currentRoute in listOf(
    Screen.Dashboard.route,
    Screen.Apiaries.route,
    Screen.Hives.route,
    Screen.Calendar.route,
    Screen.Settings.route
  )

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    snackbarHost = { SnackbarHost(snackbarHostState) },
    bottomBar = {
      if (isTopLevelScreen) {
        BeeBottomNavBar(
          currentRoute = currentRoute,
          onNavigate = { route ->
            navController.navigate(route) {
              popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
              }
              launchSingleTop = true
              restoreState = true
            }
          }
        )
      }
    }
  ) { innerPadding ->
    NavHost(
      navController = navController,
      startDestination = Screen.Dashboard.route,
      modifier = Modifier.padding(innerPadding)
    ) {
      // 1. Dashboard
      composable(Screen.Dashboard.route) {
        DashboardScreen(
          viewModel = viewModel,
          onNavigateToApiaries = {
            navController.navigate(Screen.Apiaries.route) {
              popUpTo(navController.graph.findStartDestination().id) { saveState = true }
              launchSingleTop = true
              restoreState = true
            }
          },
          onNavigateToHives = {
            navController.navigate(Screen.Hives.route) {
              popUpTo(navController.graph.findStartDestination().id) { saveState = true }
              launchSingleTop = true
              restoreState = true
            }
          },
          onNavigateToAddHive = { apiaryId ->
            navController.navigate(Screen.AddEditHive.createRoute(apiaryId = apiaryId))
          },
          onNavigateToHiveDetail = { hiveId ->
            navController.navigate(Screen.HiveDetail.createRoute(hiveId))
          },
          onNavigateToInspection = { hiveId ->
            navController.navigate(Screen.AddInspection.createRoute(hiveId))
          },
          onNavigateToFeeding = { hiveId ->
            navController.navigate(Screen.AddFeeding.createRoute(hiveId))
          }
        )
      }

      // 2. Apiaries
      composable(Screen.Apiaries.route) {
        ApiariesScreen(
          viewModel = viewModel,
          onNavigateToHivesForApiary = {
            navController.navigate(Screen.Hives.route) {
              popUpTo(navController.graph.findStartDestination().id) { saveState = true }
              launchSingleTop = true
              restoreState = true
            }
          }
        )
      }

      // 3. Hives
      composable(Screen.Hives.route) {
        HivesScreen(
          viewModel = viewModel,
          onNavigateToHiveDetail = { hiveId ->
            navController.navigate(Screen.HiveDetail.createRoute(hiveId))
          },
          onNavigateToAddHive = { apiaryId ->
            navController.navigate(Screen.AddEditHive.createRoute(apiaryId = apiaryId))
          }
        )
      }

      // 4. Calendar & Seasonal Guides
      composable(Screen.Calendar.route) {
        CalendarScreen(viewModel = viewModel)
      }

      // 5. Settings & Cloud Sync
      composable(Screen.Settings.route) {
        SettingsScreen(viewModel = viewModel)
      }

      // 6. Hive Detail
      composable(
        route = Screen.HiveDetail.route,
        arguments = listOf(navArgument("hiveId") { type = NavType.StringType })
      ) { backStackEntry ->
        val hiveId = backStackEntry.arguments?.getString("hiveId") ?: ""
        HiveDetailScreen(
          viewModel = viewModel,
          hiveId = hiveId,
          onNavigateBack = { navController.popBackStack() },
          onNavigateToEditHive = { id ->
            navController.navigate(Screen.AddEditHive.createRoute(hiveId = id))
          },
          onNavigateToAddInspection = { id ->
            navController.navigate(Screen.AddInspection.createRoute(id))
          },
          onNavigateToAddFeeding = { id ->
            navController.navigate(Screen.AddFeeding.createRoute(id))
          }
        )
      }

      // 7. Add / Edit Hive
      composable(
        route = Screen.AddEditHive.route,
        arguments = listOf(
          navArgument("hiveId") {
            type = NavType.StringType
            nullable = true
            defaultValue = null
          },
          navArgument("apiaryId") {
            type = NavType.StringType
            nullable = true
            defaultValue = null
          }
        )
      ) { backStackEntry ->
        val hiveId = backStackEntry.arguments?.getString("hiveId")
        val apiaryId = backStackEntry.arguments?.getString("apiaryId")

        AddEditHiveScreen(
          viewModel = viewModel,
          hiveId = hiveId,
          preselectedApiaryId = apiaryId,
          onNavigateBack = { navController.popBackStack() },
          onHiveSaved = { savedHiveId ->
            navController.popBackStack()
            navController.navigate(Screen.HiveDetail.createRoute(savedHiveId))
          }
        )
      }

      // 8. Add Inspection
      composable(
        route = Screen.AddInspection.route,
        arguments = listOf(navArgument("hiveId") { type = NavType.StringType })
      ) { backStackEntry ->
        val hiveId = backStackEntry.arguments?.getString("hiveId") ?: ""
        AddInspectionScreen(
          viewModel = viewModel,
          hiveId = hiveId,
          onNavigateBack = { navController.popBackStack() },
          onInspectionSaved = { navController.popBackStack() }
        )
      }

      // 9. Add Feeding
      composable(
        route = Screen.AddFeeding.route,
        arguments = listOf(navArgument("hiveId") { type = NavType.StringType })
      ) { backStackEntry ->
        val hiveId = backStackEntry.arguments?.getString("hiveId") ?: ""
        AddFeedingScreen(
          viewModel = viewModel,
          hiveId = hiveId,
          onNavigateBack = { navController.popBackStack() },
          onFeedingSaved = { navController.popBackStack() }
        )
      }
    }
  }
}
