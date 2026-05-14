package com.paryavaran.kavalu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.paryavaran.kavalu.ui.screens.*
import com.paryavaran.kavalu.ui.theme.ParyavaranTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ParyavaranTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()
    val startDestination = if (auth.currentUser != null) "home" else "login"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(
                onNavigateToRegister = { navController.navigate("register") },
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("register") {
            RegisterScreen(
                onNavigateToLogin = { navController.navigate("login") },
                onRegisterSuccess = {
                    navController.navigate("home") {
                        popUpTo("register") { inclusive = true }
                    }
                }
            )
        }
        composable("home") {
            HomeScreen(
                onLogout = {
                    auth.signOut()
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onNavigateToReport = { navController.navigate("report") },
                onNavigateToNotifications = { navController.navigate("notifications") },
                onNavigateToMap = { navController.navigate("map") }
            )
        }
        composable("report") {
            ReportScreen(
                onBack = { navController.popBackStack() },
                onReportSuccess = { navController.popBackStack() }
            )
        }
        composable("notifications") {
            NotificationScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("map") {
            MapScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
