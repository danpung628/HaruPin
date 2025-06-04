package com.example.harupin.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.harupin.roomDB.MemoEntity
import com.example.harupin.viewmodel.MemoViewModel
import com.example.harupin.viewmodel.MemoViewModelFactory
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import com.example.harupin.roomDB.MemoDatabase
import com.example.harupin.viewmodel.MemoRepository

@Composable
fun SearchScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: MemoViewModel = viewModel(factory = MemoViewModelFactory(MemoRepository(MemoDatabase.getDatabase(context))))
    var query by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val memos by if (query.isEmpty()) {
        viewModel.allMemos
    } else {
        viewModel.searchResults
    }.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = { newQuery ->
                query = newQuery // 검색어 상태 업데이트
                if (newQuery.isEmpty()) {
                    // 검색어 지우면 전체 메모로 돌아감
                } else {
                    isLoading = true
                    viewModel.searchMemos(newQuery) // 검색 실행
                    isLoading = false // 실제로는 비동기 처리 후 변경 필요
                }
            },
            label = { Text("검색") },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            },
            trailingIcon = {
                Row {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = {
                            query = "" // 검색어 지우기
                            // 검색어 지우면 전체 메모로 돌아감
                        }) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "검색어 지우기",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(onClick = {
                        isLoading = true
                        viewModel.searchMemos(query) // 검색 실행
                        isLoading = false // 실제로는 비동기 처리 후 변경 필요
                    }) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "검색",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    isLoading = true
                    viewModel.searchMemos(query) // 키보드 검색 버튼으로 검색 실행
                    isLoading = false
                }
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp)
            )
        } else {
            MemoList(list = memos,navController)
        }
    }
}

@Composable
fun MemoList(list: List<MemoEntity>,navController: NavController) {
    if (list.isEmpty()) {
        Text(
            text = "검색 결과가 없습니다",
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
                        .clickable{ // 누르면 메모 보기로 감
                            navController.navigate("memo?id=${memo.id}")
                        }
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