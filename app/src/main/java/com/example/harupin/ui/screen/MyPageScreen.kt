package com.example.harupin.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.harupin.roomDB.MemoDatabase
import com.example.harupin.roomDB.MemoEntity
import com.example.harupin.viewmodel.MemoRepository
import com.example.harupin.viewmodel.MemoViewModel
import com.example.harupin.viewmodel.MemoViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPageScreen(navController: NavController) {
    val context = LocalContext.current
    val database = MemoDatabase.getDatabase(context)
    val repository = MemoRepository(database)
    val viewModel: MemoViewModel = viewModel(factory = MemoViewModelFactory(repository))

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    // allMemos를 기본으로 사용하고, 필터링 시에는 searchResults 사용
    val allMemos by viewModel.allMemos.collectAsState()
    val filteredMemos by viewModel.searchResults.collectAsState()

    // 현재 표시할 메모 목록 결정
    val displayMemos = if (filteredMemos.isEmpty() && allMemos.isNotEmpty()) allMemos else filteredMemos

    var expandedYears by remember { mutableStateOf(setOf<String>()) }
    var expandedMonths by remember { mutableStateOf(setOf<String>()) }

    // 컴포넌트 시작 시 전체 메모 로드
    LaunchedEffect(Unit) {
        viewModel.getAllMemos()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                DrawerContent(
                    memos = displayMemos,
                    onYearSelected = { year ->
                        expandedYears = if (expandedYears.contains(year)) {
                            expandedYears - year
                        } else {
                            expandedYears + year
                        }
                        viewModel.filterMemosByYear(null) // 기본적으로 전체 메모 표시
                    },
                    onMonthSelected = { year, month ->
                        val monthKey = "$year-$month"
                        expandedMonths = if (expandedMonths.contains(monthKey)) {
                            expandedMonths - monthKey
                        } else {
                            expandedMonths + monthKey
                        }
                        viewModel.filterMemosByYearAndMonth(year, month) // 월 클릭 시 해당 연도-월 필터링
                    },
                    onMemoSelected = { memo ->
                        // 메모 상세 화면으로 내비게이션 경로 설정
                        navController.navigate("memo_detail/${memo.id}")
                        coroutineScope.launch { drawerState.close() }
                    },
                    expandedYears = expandedYears,
                    expandedMonths = expandedMonths
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = "마이페이지") },
                    navigationIcon = {
                        IconButton(onClick = {
                            coroutineScope.launch {
                                if (drawerState.isOpen) drawerState.close() else drawerState.open()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "메뉴 열기"
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { /* 메모 추가 화면으로 이동 */ }) {
                    Icon(Icons.Default.Add, contentDescription = "메모 추가")
                }
            }
        ) { contentPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
            ) {
                PageList(list = displayMemos)
            }
        }
    }
}

@Composable
fun DrawerContent(
    memos: List<MemoEntity>,
    onYearSelected: (String) -> Unit,
    onMonthSelected: (String, String) -> Unit,
    onMemoSelected: (MemoEntity) -> Unit,
    expandedYears: Set<String>,
    expandedMonths: Set<String>
) {
    // 연도 목록 추출 (최신순)
    val years = memos.map { it.date.split("-")[0] }.distinct().sortedDescending()

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        this@LazyColumn.items(years) { year ->
            // 연도 항목
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onYearSelected(year) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "${year}년",
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f)
                )
            }

            // 선택된 연도일 경우 월 목록 표시
            if (expandedYears.contains(year)) {
                val months = memos
                    .filter { it.date.startsWith(year) }
                    .map { it.date.split("-")[1] }
                    .distinct()
                    .sortedDescending() // 월 최신순 정렬

                this@LazyColumn.items(months) { month ->
                    // 월 항목 (들여쓰기)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onMonthSelected(year, month) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "${month}월",
                            fontSize = 16.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // 선택된 월일 경우 메모 목록 표시
                    if (expandedMonths.contains("$year-$month")) {
                        val monthMemos = memos
                            .filter { it.date.startsWith("$year-$month") }
                        // MemoDao에서 date DESC, time DESC로 정렬됨

                        this@LazyColumn.items(monthMemos) { memo ->
                            // 메모 항목 (추가 들여쓰기)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onMemoSelected(memo) }
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                            ) {
                                Spacer(modifier = Modifier.width(32.dp))
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(text = memo.title, fontSize = 14.sp)
                                    Text(text = "날짜: ${memo.date} ${memo.time}", fontSize = 12.sp)
                                    Text(text = "위치: ${memo.locationName ?: "N/A"}", fontSize = 12.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PageList(list: List<MemoEntity>) {
    if (list.isEmpty()) {
        Text(
            text = "메모가 없습니다",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    } else {
        LazyColumn {
            items(list) { memo ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                ) {
                    Text(text = memo.title, fontSize = 18.sp)
                    Text(text = "날짜: ${memo.date} ${memo.time}", fontSize = 14.sp)
                    Text(text = "위치: ${memo.locationName ?: "N/A"}", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}