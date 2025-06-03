package com.example.harupin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.harupin.roomDB.MemoDatabase
import com.example.harupin.roomDB.MemoEntity
import com.example.harupin.ui.screen.MainScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val db = MemoDatabase.getDatabase(this)
        lifecycleScope.launch {
            db.getMemoDao().insertMemo(
                MemoEntity(
                    title = "Travel",
                    content = "경복궁 방문",
                    year = 2025, month = 6, date = "2025-06-03", time = "16:00",
                    latitude = 37.5796, longitude = 126.9769, locationName = "경복궁",
                    weather = "맑음", temperature = 25.0, imageUri = null
                )
            )
        }
        setContent {
            MainScreen()
        }
    }
}

