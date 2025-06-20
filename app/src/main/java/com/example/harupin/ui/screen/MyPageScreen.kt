package com.example.harupin.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite // 하트 아이콘 추가
import androidx.compose.material.icons.filled.FavoriteBorder // 비어있는 하트 아이콘 추가
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star // 별 아이콘 추가
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color // 색상 처리를 위해 추가
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

    // 즐겨찾기 필터 상태 추가
    var showOnlyFavorites by remember { mutableStateOf(false) }

    // 현재 표시할 메모 목록 결정
    val baseMemos =
        if (filteredMemos.isEmpty() && allMemos.isNotEmpty()) allMemos else filteredMemos
    val displayMemos = if (showOnlyFavorites) {
        baseMemos.filter { it.isFavorite == true } // 즐겨찾기 필터링
    } else {
        baseMemos
    }

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
                    allMemos = allMemos,
                    displayMemos = displayMemos,
                    onYearSelected = { year ->
                        expandedYears = if (expandedYears.contains(year)) {
                            expandedYears - year
                        } else {
                            expandedYears + year
                        }
                        viewModel.filterMemosByYear(null)
                    },
                    onMonthSelected = { year, month ->
                        viewModel.filterMemosByYearAndMonth(year, month)
                        expandedYears = setOf()
                        expandedMonths = setOf()
                        coroutineScope.launch {
                            drawerState.close()
                        }
                    },
                    onMemoSelected = { memo ->
                        navController.navigate("memo_detail/${memo.id}")
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
                    },
                    actions = {
                        // 즐겨찾기 필터 버튼 추가
                        IconButton(onClick = {
                            showOnlyFavorites = !showOnlyFavorites
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = if (showOnlyFavorites) "전체 보기" else "즐겨찾기만 보기",
                                tint = if (showOnlyFavorites) Color.Yellow else MaterialTheme.colorScheme.onSurface
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
                PageList(
                    list = displayMemos,
                    navController = navController,
                    onFavoriteToggle = { memo ->
                        viewModel.updateMemoFavorite(
                            memo.id,
                            !(memo.isFavorite ?: false)
                        ) // 즐겨찾기 상태 토글
                    }
                )
            }
        }
    }
}

@Composable
fun DrawerContent(
    allMemos: List<MemoEntity>,
    displayMemos: List<MemoEntity>,
    onYearSelected: (String) -> Unit,
    onMonthSelected: (String, String) -> Unit,
    onMemoSelected: (MemoEntity) -> Unit,
    expandedYears: Set<String>,
    expandedMonths: Set<String>
) {
    val years = allMemos.map { it.date.split("-")[0] }.distinct().sortedDescending()

    LazyColumn(
        modifier = Modifier.fillMaxWidth()
    ) {
        years.forEach { year ->
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

            if (expandedYears.contains(year)) {
                val months = allMemos
                    .filter { it.date.startsWith(year) }
                    .map { it.date.split("-")[1] }
                    .distinct()
                    .sortedDescending()

                months.forEach { month ->
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
fun PageList(
    list: List<MemoEntity>,
    navController: NavController,
    onFavoriteToggle: (MemoEntity) -> Unit // 즐겨찾기 토글 콜백 추가
) {
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
                Page(
                    memo = memo,
                    modifier = Modifier,
                    onClicked = { navController.navigate("memo?id=${memo.id}") },
                    onFavoriteToggle = onFavoriteToggle // 즐겨찾기 토글 콜백 전달
                )
            }
        }
    }
}

@Composable
fun Page(
    memo: MemoEntity,
    modifier: Modifier = Modifier,
    onClicked: () -> Unit,
    onFavoriteToggle: (MemoEntity) -> Unit // 즐겨찾기 토글 콜백 추가
) {
    val context = LocalContext.current
    val db = MemoDatabase.getDatabase(context)
    val viewModelFactory = MemoViewModelFactory(MemoRepository(db))
    val viewModel: MemoViewModel = viewModel(factory = viewModelFactory)


    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        DeleteConfirmDialog(
            onConfirm = {
                viewModel.deleteMemo(memo)
                showDialog = false
            },
            onDismiss = {
                showDialog = false
            }
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .combinedClickable(
                    onLongClick = { showDialog = true },
                    onClick = onClicked,
                )
        ) {
            Text(text = memo.title, fontSize = 18.sp)
            Text(text = "날짜: ${memo.date} ${memo.time}", fontSize = 14.sp)
            Text(text = "위치: ${memo.locationName ?: "N/A"}", fontSize = 14.sp)
        }

        // 즐겨찾기 하트 버튼 추가
        IconButton(
            onClick = { onFavoriteToggle(memo) }
        ) {
            Icon(
                imageVector = if (memo.isFavorite == true) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = if (memo.isFavorite == true) "즐겨찾기 해제" else "즐겨찾기 추가",
                tint = if (memo.isFavorite == true) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}


@Composable
fun DeleteConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("삭제 확인") },
        text = { Text("정말로 삭제하시겠습니까?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("네")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("아니요")
            }
        }
    )
}

