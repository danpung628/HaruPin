// ui/nav/Navigation.kt

package com.example.harupin.ui.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
    }
}
