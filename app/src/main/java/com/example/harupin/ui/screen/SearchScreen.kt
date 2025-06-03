package com.example.harupin.ui.screen

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
import com.example.harupin.viewmodel.SearchViewModel
import com.example.harupin.viewmodel.SearchViewModelFactory
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction

@Composable
fun SearchScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: SearchViewModel = viewModel(factory = SearchViewModelFactory(context))
    var query by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val memos by viewModel.memos.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = { newQuery ->
                query = newQuery // 검색어 상태 업데이트
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
                            viewModel.searchMemos("") // 검색 결과 초기화
                        }) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "검색어 지우기",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(onClick = {
                        isLoading = true // 로딩 시작
                        viewModel.searchMemos(query) // 검색 실행
                        isLoading = false // 로딩 종료 (실제로는 searchMemos가 비동기라면 콜백 필요)
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
            MemoList(list = memos)
        }
    }
}

@Composable
fun MemoList(list: List<MemoEntity>) {
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