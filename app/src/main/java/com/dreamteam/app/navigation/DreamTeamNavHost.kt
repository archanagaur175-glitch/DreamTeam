package com.dreamteam.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dreamteam.app.ui.SettingsScreen
import com.dreamteam.feature.circadian.ui.CircadianScreen
import com.dreamteam.feature.dashboard.ui.DashboardDestination
import com.dreamteam.feature.dashboard.ui.DashboardScreen
import com.dreamteam.feature.logger.ui.CorrelationsScreen
import com.dreamteam.feature.logger.ui.LoggerScreen
import com.dreamteam.feature.sleepdebt.ui.DebtScreen
import com.dreamteam.feature.smartalarm.ui.AlarmSetupScreen
import kotlinx.serialization.Serializable

@Serializable data object DashboardRoute
@Serializable data object DebtRoute
@Serializable data object CircadianRoute
@Serializable data object AlarmSetupRoute
@Serializable data object LoggerRoute
@Serializable data object CorrelationsRoute
@Serializable data object SettingsRoute

@Composable
fun DreamTeamNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = DashboardRoute) {
        composable<DashboardRoute> {
            DashboardScreen(
                onNavigate = { destination ->
                    navController.navigate(
                        when (destination) {
                            DashboardDestination.Debt -> DebtRoute
                            DashboardDestination.Circadian -> CircadianRoute
                            DashboardDestination.Alarm -> AlarmSetupRoute
                            DashboardDestination.Logger -> LoggerRoute
                            DashboardDestination.Correlations -> CorrelationsRoute
                            DashboardDestination.Settings -> SettingsRoute
                        },
                    )
                },
            )
        }
        composable<DebtRoute> { DebtScreen(onBack = { navController.popBackStack() }) }
        composable<CircadianRoute> { CircadianScreen(onBack = { navController.popBackStack() }) }
        composable<AlarmSetupRoute> { AlarmSetupScreen(onBack = { navController.popBackStack() }) }
        composable<LoggerRoute> { LoggerScreen(onBack = { navController.popBackStack() }) }
        composable<CorrelationsRoute> { CorrelationsScreen(onBack = { navController.popBackStack() }) }
        composable<SettingsRoute> { SettingsScreen(onBack = { navController.popBackStack() }) }
    }
}
