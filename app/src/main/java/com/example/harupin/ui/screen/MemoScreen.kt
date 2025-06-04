package com.example.harupin.ui.screen

import android.app.DatePickerDialog
import android.icu.util.Calendar
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
    var location by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedWeather by remember { mutableStateOf("") }
    val calendar = remember { Calendar.getInstance() }

    var selectedDate by remember {
        mutableStateOf(
            "%04d-%02d-%02d".format(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH)
            )
        )
    }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            selectedDate = "%04d-%02d-%02d".format(year, month + 1, dayOfMonth)
            calendar.set(year, month, dayOfMonth)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

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
                text = String.format("경도: %.1f 위도: %.1f", lat, lng),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 🌤️ 날씨 이모지 선택기
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                val weatherOptions = listOf("☀️", "🌤️", "🌧️", "⛈️", "❄️", "🌫️")
                weatherOptions.forEach { emoji ->
                    val isSelected = selectedWeather == emoji
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .then(
                                if (isSelected) Modifier.border(
                                    width = 1.dp,
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

            // 날짜 선택 버튼 (달력 아이콘 포함)
            OutlinedButton(
                onClick = { datePickerDialog.show() },
                modifier = Modifier
                    .height(48.dp)
                    .width(140.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp), // 최소 수평 여백만 남김
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "날짜 선택"
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = selectedDate)
                }
            }

        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 제목 입력란 - 넓게 확장
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("제목") },
                modifier = Modifier
                    .height(56.dp)
                    .weight(1f)
            )

            // 장소 입력란 - 고정 너비
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("장소") },
                modifier = Modifier
                    .height(56.dp)
                    .width(100.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))


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
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH) + 1
                val date = selectedDate
                val time = "%02d:%02d".format(
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE)
                )

                val memo = MemoEntity(
                    title = title,
                    content = content,
                    year = year,
                    month = month,
                    date = date,
                    time = time,
                    latitude = lat,
                    longitude = lng,
                    locationName = location,
                    weather = selectedWeather.ifEmpty { "☀️" },
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
