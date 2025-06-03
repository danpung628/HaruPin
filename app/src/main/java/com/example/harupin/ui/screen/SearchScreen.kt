// com/example/harupin/ui/screen/SearchScreen.kt

package com.example.harupin.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.harupin.roomDB.MemoEntity
import com.example.harupin.viewmodel.SearchViewModel
import com.example.harupin.viewmodel.SearchViewModelFactory

@Composable
fun SearchScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: SearchViewModel = viewModel(factory = SearchViewModelFactory(context))
    var query by remember { mutableStateOf("") }

    val memos by viewModel.memos.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = { newQuery ->
                query = newQuery
                viewModel.searchMemos(newQuery) // 실시간 검색
            },
            label = { Text("Search") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        MemoList(list = memos)
    }
}

@Composable
fun MemoList(list: List<MemoEntity>) {
    LazyColumn {
        items(list) { memo ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Text(text = memo.title, fontSize = 18.sp)
                Text(text = "Date: ${memo.date} ${memo.time}", fontSize = 14.sp)
                Text(text = "Location: ${memo.locationName ?: "N/A"}", fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
