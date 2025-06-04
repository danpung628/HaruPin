package com.example.harupin.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.harupin.roomDB.MemoEntity
import com.example.harupin.ui.screen.MemoScreen
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.compose.ExperimentalNaverMapApi
import com.naver.maps.map.compose.Marker
import com.naver.maps.map.compose.rememberMarkerState
import okhttp3.Route

@OptIn(ExperimentalNaverMapApi::class)
@Composable
fun ShowMarker(memo: MemoEntity, navController: NavController) { // 위도 경도 위치이름
    Marker(
        state = rememberMarkerState(position = LatLng(memo.latitude, memo.longitude)), // 위치정보
        captionText = memo.locationName, // 나중에 데이터 형식에서 이름 받아옴.
        onClick = {
            navController.navigate("메모 보는 위치로?id=${memo.id}") // 메모 보여주는 위치로가기 아직 없음.
            true
        } // 나중에 네비용
    )
}