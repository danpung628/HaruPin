package com.example.harupin.ui.screen

import android.icu.util.Calendar
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.harupin.roomDB.MemoDatabase
import com.example.harupin.roomDB.MemoEntity
import com.example.harupin.viewmodel.MemoRepository
import com.example.harupin.viewmodel.MemoViewModel
import com.example.harupin.viewmodel.MemoViewModelFactory

@Composable
fun WeatherEmojiSelector(
    selected: String,
    onSelect: (String) -> Unit
) {
    val weatherOptions = listOf("☀️", "🌤️", "🌧️", "⛈️", "❄️", "🌫️")

    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        weatherOptions.forEach { emoji ->
            val isSelected = selected == emoji
            IconToggleButton(
                checked = isSelected,
                onCheckedChange = {
                    onSelect(if (isSelected) "" else emoji)
                }
            ) {
                Box(
                    modifier = Modifier
                        .size(if (isSelected) 56.dp else 48.dp)
                        .background(
                            color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = emoji,
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }
        }
    }
}

@Composable
fun MemoScreen(
    navController: NavController,
    lat: Double,
    lng: Double
) {
    val context = LocalContext.current
    val db = MemoDatabase.getDatabase(context)
    val viewModelFactory = MemoViewModelFactory(MemoRepository(db))
    val viewModel: MemoViewModel = viewModel(factory = viewModelFactory)

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedWeather by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 📍 위도/경도 표시
            Text(
                text = String.format("📍 %.1f, %.1f", lat, lng),
                style = MaterialTheme.typography.bodyMedium
            )

            // 🌤️ 날씨 이모지 선택기
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                val weatherOptions = listOf("☀️", "🌤️", "🌧️", "⛈️", "❄️", "🌫️")
                weatherOptions.forEach { emoji ->
                    val isSelected = selectedWeather == emoji
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .then(
                                if (isSelected) Modifier.border(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(50)
                                ) else Modifier
                            )
                            .clickable { selectedWeather = emoji },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
            
        }

        // 제목 입력란 - Row 안에서 weight로 너비 확보
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("제목") },
            modifier = Modifier
                .fillMaxWidth()
        )

        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("내용") },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                val now = Calendar.getInstance()
                val year = now.get(Calendar.YEAR)
                val month = now.get(Calendar.MONTH) + 1
                val date = "%04d-%02d-%02d".format(year, month, now.get(Calendar.DAY_OF_MONTH))
                val time = "%02d:%02d".format(now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE))

                val memo = MemoEntity(
                    title = title,
                    content = content,
                    year = year,
                    month = month,
                    date = date,
                    time = time,
                    latitude = lat,
                    longitude = lng,
                    locationName = null,
                    weather = selectedWeather.ifEmpty { "☀️" },
                    temperature = null,
                    imageUri = null
                )
                viewModel.insertMemo(memo)
                navController.popBackStack()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("메모 저장하기")
        }
    }
}
