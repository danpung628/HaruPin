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
    val displayMemos =
        if (filteredMemos.isEmpty() && allMemos.isNotEmpty()) allMemos else filteredMemos

    var expandedYears by remember { mutableStateOf(setOf<String>()) }
    var expandedMonths by remember { mutableStateOf(setOf<String>()) }

    // 컴포넌트 시작 시 전체 메모 로드
    LaunchedEffect(Unit) {
        viewModel.getAllMemos()
    }

    // 햄버거바가 열릴 때마다 상태 초기화
    LaunchedEffect(drawerState.isOpen) {
        if (drawerState.isOpen) {
            expandedYears = setOf()
            expandedMonths = setOf()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                DrawerContent(
                    allMemos = allMemos, // 전체 메모 리스트 전달
                    displayMemos = displayMemos, // 현재 표시되는 메모 리스트 전달
                    onYearSelected = { year ->
                        expandedYears = if (expandedYears.contains(year)) {
                            expandedYears - year
                        } else {
                            expandedYears + year
                        }
                        viewModel.filterMemosByYear(null) // 기본적으로 전체 메모 표시
                    },
                    onMonthSelected = { year, month ->
                        viewModel.filterMemosByYearAndMonth(year, month) // 월 클릭 시 해당 연도-월 필터링
                        // 상태 초기화를 먼저 하고 햄버거바 닫기
                        expandedYears = setOf()
                        expandedMonths = setOf()
                        coroutineScope.launch {
                            drawerState.close()
                        }
                    },
                    onMemoSelected = { memo ->
                        // 메모 상세 화면으로 내비게이션 경로 설정
                        navController.navigate("memo_detail/${memo.id}")
                        // 상태 초기화를 먼저 하고 햄버거바 닫기
                        expandedYears = setOf()
                        expandedMonths = setOf()
                        coroutineScope.launch {
                            drawerState.close()
                        }
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
                            if (drawerState.isOpen) {
                                // 상태 초기화를 먼저 하고 햄버거바 닫기
                                expandedYears = setOf()
                                expandedMonths = setOf()
                                coroutineScope.launch {
                                    drawerState.close()
                                }
                            } else {
                                coroutineScope.launch {
                                    drawerState.open()
                                }
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
        ) { contentPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
            ) {
                PageList(list = displayMemos,navController = navController)
            }
        }
    }
}

@Composable
fun DrawerContent(
    allMemos: List<MemoEntity>, // 전체 메모 리스트 (연도/월 목록 생성용)
    displayMemos: List<MemoEntity>, // 현재 표시되는 메모 리스트 (메모 선택용)
    onYearSelected: (String) -> Unit,
    onMonthSelected: (String, String) -> Unit,
    onMemoSelected: (MemoEntity) -> Unit,
    expandedYears: Set<String>,
    expandedMonths: Set<String>
) {
    // 전체 메모를 기준으로 연도 목록 추출 (최신순)
    val years = allMemos.map { it.date.split("-")[0] }.distinct().sortedDescending()

    LazyColumn(
        modifier = Modifier.fillMaxWidth()
    ) {
        years.forEach { year ->
            // 연도 항목
            item {
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
            }

            // 선택된 연도일 경우 월 목록 표시
            if (expandedYears.contains(year)) {
                val months = allMemos // 전체 메모를 기준으로 월 목록 생성
                    .filter { it.date.startsWith(year) }
                    .map { it.date.split("-")[1] }
                    .distinct()
                    .sortedDescending() // 월 최신순 정렬

                months.forEach { month ->
                    // 월 항목 (들여쓰기)
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onMonthSelected(year, month) }
                                .padding(horizontal = 32.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "${month}월",
                                fontSize = 16.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PageList(list: List<MemoEntity>, navController: NavController) {
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
                Page(memo, Modifier) { -> //메모 누르면 넘어감
                    navController.navigate("memo?id=${memo.id}")
                }
            }
        }
    }
}

@Composable
fun Page(memo: MemoEntity, modifier: Modifier = Modifier, onClicked: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .clickable{onClicked()}
    ) {
        Text(text = memo.title, fontSize = 18.sp)
        Text(text = "날짜: ${memo.date} ${memo.time}", fontSize = 14.sp)
        Text(text = "위치: ${memo.locationName ?: "N/A"}", fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))
    }

}