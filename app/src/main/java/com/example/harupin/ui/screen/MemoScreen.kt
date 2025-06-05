package com.example.harupin.ui.screen

import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.harupin.roomDB.MemoDatabase
import com.example.harupin.roomDB.MemoEntity
import com.example.harupin.viewmodel.MemoRepository
import com.example.harupin.viewmodel.MemoViewModel
import com.example.harupin.viewmodel.MemoViewModelFactory

@Composable
fun WeatherSelector(
    selectedWeather: String,
    onWeatherSelected: (String) -> Unit,
    isEnabled: Boolean
) {
    val weatherOptions = listOf("☀️", "🌤️", "🌧️", "⛈️", "❄️", "🌫️")

    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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
                    .clickable(enabled = isEnabled) { onWeatherSelected(emoji) },
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
fun DateSelector(
    selectedDate: String,
    onClick: () -> Unit,
    isEnabled: Boolean
) {
    OutlinedButton(
        onClick = onClick,
        enabled = isEnabled,
        modifier = Modifier
            .height(48.dp)
            .width(140.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
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

@Composable
fun TitleLocationFields(
    title: String,
    onTitleChange: (String) -> Unit,
    location: String,
    onLocationChange: (String) -> Unit,
    isEnabled: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text("제목") },
            enabled = isEnabled,
            modifier = Modifier
                .height(56.dp)
                .weight(1f)
        )

        OutlinedTextField(
            value = location,
            onValueChange = onLocationChange,
            label = { Text("장소") },
            enabled = isEnabled,
            modifier = Modifier
                .height(56.dp)
                .width(100.dp)
        )
    }
}

@Composable
fun ImageSelector(
    imageUris: List<Uri>,
    onRemoveImage: (Int) -> Unit,
    onAddImageClick: () -> Unit,
    isEnabled: Boolean,
    hasGalleryPermission: Boolean
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        imageUris.forEachIndexed { index, uri ->
            Box(modifier = Modifier.size(80.dp)) {
                Image(
                    painter = rememberAsyncImagePainter(uri),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .border(1.dp, MaterialTheme.colorScheme.primary)
                )
                if (isEnabled) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "삭제",
                        tint = Color.Red,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(20.dp)
                            .clickable { onRemoveImage(index) }
                    )
                }
            }
        }

        if (imageUris.size < 3 && isEnabled && hasGalleryPermission) {
            Button(onClick = onAddImageClick, modifier = Modifier.height(80.dp)) {
                Text("추가")
            }
        } else if (!hasGalleryPermission && isEnabled) {
            Text(
                text = "사진 추가 권한이 없습니다",
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
    }
}


@Composable
fun MemoScreen(
    navController: NavController,
    lat: Double,
    lng: Double,
    edit: Boolean
) {
    val context = LocalContext.current
    var hasGalleryPermission by remember { mutableStateOf(true) } // 기본 true → false되면 버튼 막힘
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasGalleryPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "사진을 추가하려면 갤러리 접근 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }

        val granted = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        hasGalleryPermission = granted

        if (!granted) {
            permissionLauncher.launch(permission)
        }
    }

    val db = MemoDatabase.getDatabase(context)
    val viewModelFactory = MemoViewModelFactory(MemoRepository(db))
    val viewModel: MemoViewModel = viewModel(factory = viewModelFactory)

    var isEditMode by remember { mutableStateOf(edit) }


    val imageUris = remember { mutableStateOf<List<Uri>>(emptyList()) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        imageUris.value = uris.take(3) // 최대 3장까지 선택
    }

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
            if (isEditMode) {
                // 편집 중일 때 "취소"로 편집 종료
                Button(onClick = { isEditMode = false }) {
                    Text("취소")
                }
            } else {
                // 읽기 전용일 때 "닫기"로 화면 나가기
                Button(onClick = { navController.popBackStack() }) {
                    Text("닫기")
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 🌤️ 날씨 이모지 선택기
            WeatherSelector(
                selectedWeather = selectedWeather,
                onWeatherSelected = { selectedWeather = it },
                isEnabled = isEditMode
            )

            // 날짜 선택 버튼 (달력 아이콘 포함)
            DateSelector(
                selectedDate = selectedDate,
                onClick = { datePickerDialog.show() },
                isEnabled = isEditMode
            )

        }
        TitleLocationFields(
            title = title,
            onTitleChange = { title = it },
            location = location,
            onLocationChange = { location = it },
            isEnabled = isEditMode
        )




        OutlinedTextField(
            value = content,
            enabled = isEditMode,
            onValueChange = { content = it },
            label = { Text("내용") },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        )

        Text(text = "사진 추가 (최대 3장)", style = MaterialTheme.typography.labelMedium)

        ImageSelector(
            imageUris = imageUris.value,
            onRemoveImage = { index ->
                imageUris.value = imageUris.value.toMutableList().also { it.removeAt(index) }
            },
            onAddImageClick = { imagePicker.launch("image/*") },
            isEnabled = isEditMode,
            hasGalleryPermission = hasGalleryPermission
        )


        if (isEditMode) {
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
                        imageUri1 = imageUris.value.getOrNull(0)?.toString(),
                        imageUri2 = imageUris.value.getOrNull(1)?.toString(),
                        imageUri3 = imageUris.value.getOrNull(2)?.toString()
                    )
                    viewModel.insertMemo(memo)
                    isEditMode = false
                    navController.navigate("home")
                    //navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("메모 저장하기")
            }
        }
    }
}

@Composable
fun MemoScreen(
    navController: NavController,
    id: Int,
    edit: Boolean
) {
    val context = LocalContext.current
    var hasGalleryPermission by remember { mutableStateOf(true) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasGalleryPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "사진을 추가하려면 갤러리 접근 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }
        val granted = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        hasGalleryPermission = granted
        if (!granted) permissionLauncher.launch(permission)
    }

    val db = MemoDatabase.getDatabase(context)
    val viewModelFactory = MemoViewModelFactory(MemoRepository(db))
    val viewModel: MemoViewModel = viewModel(factory = viewModelFactory)
    val memo by viewModel.searchResults.collectAsState()
    var isEditMode by remember { mutableStateOf(edit) }

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var selectedWeather by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("") }
    val imageUris = remember { mutableStateOf<List<Uri>>(emptyList()) }
    val calendar = remember { Calendar.getInstance() }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris -> imageUris.value = uris.take(3) }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day ->
            selectedDate = "%04d-%02d-%02d".format(year, month + 1, day)
            calendar.set(year, month, day)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    LaunchedEffect(id) { viewModel.getById(id) }
    LaunchedEffect(memo) {
        memo.firstOrNull()?.let {
            title = it.title
            content = it.content
            location = it.locationName ?: ""
            selectedWeather = it.weather
            selectedDate = it.date
            imageUris.value = listOfNotNull(it.imageUri1, it.imageUri2, it.imageUri3).map { uri -> Uri.parse(uri) }
        }
    }

    memo.firstOrNull()?.let { currentMemo ->
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("경도: %.1f 위도: %.1f".format(currentMemo.latitude, currentMemo.longitude))
                Button(onClick = {
                    if (isEditMode) isEditMode = false else navController.popBackStack()
                }) { Text(if (isEditMode) "취소" else "닫기") }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 🌤️ 날씨 이모지 선택기
                WeatherSelector(
                    selectedWeather = selectedWeather,
                    onWeatherSelected = { selectedWeather = it },
                    isEnabled = isEditMode
                )

                // 날짜 선택 버튼 (달력 아이콘 포함)
                DateSelector(
                    selectedDate = selectedDate,
                    onClick = { datePickerDialog.show() },
                    isEnabled = isEditMode
                )

            }

            TitleLocationFields(
                title = title,
                onTitleChange = { title = it },
                location = location,
                onLocationChange = { location = it },
                isEnabled = isEditMode
            )

            OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("내용") }, enabled = isEditMode, modifier = Modifier.fillMaxWidth().height(150.dp))

            Text("사진 추가 (최대 3장)", style = MaterialTheme.typography.labelMedium)
            ImageSelector(
                imageUris = imageUris.value,
                onRemoveImage = { index ->
                    imageUris.value = imageUris.value.toMutableList().also { it.removeAt(index) }
                },
                onAddImageClick = { imagePicker.launch("image/*") },
                isEnabled = isEditMode,
                hasGalleryPermission = hasGalleryPermission
            )

            if (!isEditMode) {
                Button(onClick = { isEditMode = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("편집하기")
                }
            } else {
                Button(onClick = {
                    val updated = currentMemo.copy(
                        title = title,
                        content = content,
                        locationName = location,
                        weather = selectedWeather,
                        date = selectedDate,
                        imageUri1 = imageUris.value.getOrNull(0)?.toString(),
                        imageUri2 = imageUris.value.getOrNull(1)?.toString(),
                        imageUri3 = imageUris.value.getOrNull(2)?.toString()
                    )
                    viewModel.updateMemo(updated)
                    isEditMode = false
                }, modifier = Modifier.fillMaxWidth()) {
                    Text("저장")
                }
            }
        }
    } ?: Text("메모 불러오는 중...")
}


