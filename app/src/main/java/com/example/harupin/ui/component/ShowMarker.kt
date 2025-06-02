package com.example.harupin.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.compose.ExperimentalNaverMapApi
import com.naver.maps.map.compose.Marker
import com.naver.maps.map.compose.rememberMarkerState

@OptIn(ExperimentalNaverMapApi::class)
@Composable
fun ShowMarker(lat:Double, lng:Double, name:String?) { // 위도 경도 위치이름
    Marker(
        state = rememberMarkerState(position = LatLng(lat,lng)), // 위치정보
        captionText = name// 나중에 데이터 형식에서 이름 받아옴.
    )
}