package com.example.harupin.ui.component

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.harupin.roomDB.MemoEntity
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.compose.ExperimentalNaverMapApi
import com.naver.maps.map.compose.Marker
import com.naver.maps.map.compose.rememberMarkerState

@OptIn(ExperimentalNaverMapApi::class)
@Composable
fun ShowMarker(memo: MemoEntity, navController: NavController) {
    Marker(
        state = rememberMarkerState(position = LatLng(memo.latitude, memo.longitude)),
        captionText = memo.locationName ?: memo.title,
        onClick = {
            navController.navigate("memo_detail/${memo.id}")
            true
        }
    )
}