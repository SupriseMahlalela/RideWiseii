package com.ridewise.app.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material3.Text
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ridewise.app.ui.screens.admin.AdminDashboardScreen
import com.ridewise.app.ui.screens.authentication.LoginScreen
import com.ridewise.app.ui.screens.authentication.RegisterScreen
import com.ridewise.app.ui.screens.authentication.VerifyCodeScreen
import com.ridewise.app.ui.screens.driver.DriverDashboardScreen
import com.ridewise.app.ui.screens.homescreens.SplashScreen
import com.ridewise.app.ui.screens.homescreens.WelcomeScreen
import com.ridewise.app.ui.screens.owner.*
import com.ridewise.app.ui.screens.parent.ParentDashboardScreen

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        // ===== SPLASH =====
        composable("splash") {
            SplashScreen(
                onTimeout = {
                    navController.navigate("welcome") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        // ===== WELCOME =====
        composable("welcome") {
            WelcomeScreen(navController = navController)
        }

        // ===== REGISTER =====
        composable("register") {
            RegisterScreen(navController = navController)
        }

        // ===== LOGIN =====
        composable("login") {
            LoginScreen(navController = navController)
        }

        // ===== VERIFY 2FA =====
        composable("verify") {
            VerifyCodeScreen(navController = navController)
        }

        // ===== PARENT DASHBOARD =====
        composable("parent_dashboard") {
            ParentDashboardScreen(navController = navController)
        }

        // ===== DRIVER DASHBOARD (placeholder) =====
        composable("driver_dashboard") {
            Text(
                text = "Driver Dashboard - Coming soon! 🚗",
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
            )
        }

        // ===== ADMIN DASHBOARD (placeholder) =====
        composable("admin_dashboard") {
            Text(
                text = "Admin Dashboard - Coming soon! 🛠️",
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
            )
        }

        // ===== FALLBACK DASHBOARD (if role is unknown) =====
        composable("dashboard") {
            Text(
                text = "Dashboard - Unknown role",
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
            )
        }
        composable("parent_dashboard") {
            ParentDashboardScreen(navController = navController)
        }
        composable("admin_dashboard") {
            AdminDashboardScreen(navController = navController)
        }
        composable("driver_dashboard") {
            DriverDashboardScreen(navController = navController)
        }
        composable("owner_dashboard") {
            OwnerDashboardScreen(navController = navController)
        }
    }}