package com.example.harupin.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavBar(navController: NavController) {
    val items = listOf("home", "search", "mypage")
    val icons = listOf(Icons.Default.Home, Icons.Default.Search, Icons.Default.Person)
    val labels = listOf("홈", "검색", "마이")

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEachIndexed { index, screen ->
            NavigationBarItem(
                icon = { Icon(icons[index], contentDescription = labels[index]) },
                label = null,
                selected = currentRoute == screen,
                onClick  = {
                    val isSameTab = currentRoute?.startsWith(screen) == true

                    if (isSameTab) {
                        // **같은 탭**을 다시 눌렀다 → 그 탭 내부 스택만 pop
                        navController.popBackStack(screen, inclusive = false)
                    } else {
                        // **다른 탭**을 눌렀다 → 스택은 살려 두고 화면만 전환
                        navController.navigate(screen) {
                            launchSingleTop = true      // 중복 인스턴스 방지
                            restoreState     = true      // 예전 상태 복원
                        }
                    }
                }
            )
        }
    }
}