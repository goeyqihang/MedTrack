package com.qihang.medtrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.qihang.medtrack.data.session.SessionManager
import com.qihang.medtrack.ui.addmed.AddMedicationScreen
import com.qihang.medtrack.ui.claim.ClaimAccountScreen
import com.qihang.medtrack.ui.cliniciandashboard.ClinicianDashboardScreen
import com.qihang.medtrack.ui.clinicianlogin.ClinicianLoginScreen
import com.qihang.medtrack.ui.components.BottomNavBar
import com.qihang.medtrack.ui.components.BottomTab
import com.qihang.medtrack.ui.home.HomeScreen
import com.qihang.medtrack.ui.login.LoginScreen
import com.qihang.medtrack.ui.medcoach.MedCoachScreen
import com.qihang.medtrack.ui.settings.SettingsScreen
import com.qihang.medtrack.ui.signup.SignUpScreen
import com.qihang.medtrack.ui.symptoms.SymptomsScreen
import com.qihang.medtrack.ui.symptomtrend.SymptomTrendScreen
import com.qihang.medtrack.ui.theme.MedTrackTheme
import com.qihang.medtrack.ui.welcome.WelcomeScreen

/** Routes of the screens outside the bottom bar (tab routes live in [BottomTab]). */
private object Routes {
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val SIGN_UP = "sign_up"
    const val CLAIM = "claim"
    const val ADD_MEDICATION = "add_medication"
    const val SYMPTOM_TREND = "symptom_trend"
    const val CLINICIAN_LOGIN = "clinician_login"
    const val CLINICIAN_DASHBOARD = "clinician_dashboard"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val session = (application as MedTrackApplication).container.sessionManager

        setContent {
            MedTrackTheme {
                MedTrackApp(session)
            }
        }
    }
}

@Composable
fun MedTrackApp(session: SessionManager) {
    val navController = rememberNavController()

    val startDestination =
        if (session.loggedInPatientId != null) BottomTab.Home.route else Routes.WELCOME

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in BottomTab.routes

    val logoutAndGoWelcome: () -> Unit = {
        session.logout()
        navController.navigate(Routes.WELCOME) {
            popUpTo(0) { inclusive = true }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) BottomNavBar(navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.WELCOME) {
                WelcomeScreen(
                    modifier = Modifier.fillMaxSize(),
                    onLoginClick = { navController.navigate(Routes.LOGIN) },
                    onSignupClick = { navController.navigate(Routes.SIGN_UP) },
                    onClaimClick = { navController.navigate(Routes.CLAIM) }
                )
            }

            composable(Routes.LOGIN) {
                LoginScreen(
                    modifier = Modifier.fillMaxSize(),
                    onLoginSuccess = {
                        navController.navigate(BottomTab.Home.route) {
                            popUpTo(Routes.WELCOME) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.SIGN_UP) {
                SignUpScreen(
                    modifier = Modifier.fillMaxSize(),
                    onNavigateToLogin = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.WELCOME)
                        }
                    }
                )
            }

            composable(Routes.CLAIM) {
                ClaimAccountScreen(
                    modifier = Modifier.fillMaxSize(),
                    onClaimSuccess = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.WELCOME)
                        }
                    }
                )
            }

            composable(BottomTab.Home.route) {
                HomeScreen(
                    modifier = Modifier.fillMaxSize(),
                    onNavigateToAddMedication = { navController.navigate(Routes.ADD_MEDICATION) }
                )
            }

            composable(BottomTab.Symptoms.route) {
                SymptomsScreen(
                    modifier = Modifier.fillMaxSize(),
                    onViewTrends = { navController.navigate(Routes.SYMPTOM_TREND) }
                )
            }

            composable(Routes.SYMPTOM_TREND) {
                SymptomTrendScreen(
                    modifier = Modifier.fillMaxSize(),
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(BottomTab.MedCoach.route) {
                MedCoachScreen(modifier = Modifier.fillMaxSize())
            }

            composable(BottomTab.Settings.route) {
                SettingsScreen(
                    modifier = Modifier.fillMaxSize(),
                    onLogout = logoutAndGoWelcome,
                    onClinicianLogin = { navController.navigate(Routes.CLINICIAN_LOGIN) }
                )
            }

            composable(Routes.CLINICIAN_LOGIN) {
                ClinicianLoginScreen(
                    modifier = Modifier.fillMaxSize(),
                    onNavigateBack = { navController.popBackStack() },
                    onLoginSuccess = {
                        navController.navigate(Routes.CLINICIAN_DASHBOARD) {
                            popUpTo(Routes.CLINICIAN_LOGIN) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.CLINICIAN_DASHBOARD) {
                ClinicianDashboardScreen(
                    modifier = Modifier.fillMaxSize(),
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Routes.ADD_MEDICATION) {
                AddMedicationScreen(
                    modifier = Modifier.fillMaxSize(),
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
