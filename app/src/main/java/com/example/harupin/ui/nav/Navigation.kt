// ui/nav/Navigation.kt

package com.example.harupin.ui.nav

import android.R.attr.type
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.harupin.ui.screen.HomeScreen
import com.example.harupin.ui.screen.MemoScreen
import com.example.harupin.ui.screen.MyPageScreen
import com.example.harupin.ui.screen.SearchScreen

@Composable
fun NavigationGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomeScreen(navController) }
        composable("search") { SearchScreen(navController) }
        composable("mypage") { MyPageScreen(navController) }
        composable("memo") { MemoScreen(navController) }
        composable(
            route = "memo?lat={lat}&lng={lng}",
            arguments = listOf(
                navArgument("lat") { type = NavType.StringType },
                navArgument("lng") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val lat = backStackEntry.arguments?.getString("lat")?.toDoubleOrNull() ?: 0.0
            val lng = backStackEntry.arguments?.getString("lng")?.toDoubleOrNull() ?: 0.0
            MemoScreen(navController, lat, lng)
        }
    }
}
