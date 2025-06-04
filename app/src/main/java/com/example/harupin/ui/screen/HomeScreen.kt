package com.example.harupin.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.harupin.roomDB.MemoDatabase
import com.example.harupin.roomDB.MemoEntity
import com.example.harupin.ui.component.ShowMarker
import com.example.harupin.viewmodel.MemoRepository
import com.example.harupin.viewmodel.MemoViewModel
import com.example.harupin.viewmodel.MemoViewModelFactory
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.compose.ExperimentalNaverMapApi
import com.naver.maps.map.compose.LocationTrackingMode
import com.naver.maps.map.compose.MapProperties
import com.naver.maps.map.compose.MapUiSettings
import com.naver.maps.map.compose.Marker
import com.naver.maps.map.compose.NaverMap
import com.naver.maps.map.compose.rememberCameraPositionState
import com.naver.maps.map.compose.rememberFusedLocationSource
import com.naver.maps.map.compose.rememberMarkerState

@OptIn(ExperimentalNaverMapApi::class, ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(navController: NavController) {
    // DB 정보
    val context = LocalContext.current
    val database = MemoDatabase.getDatabase(context)
    val repository = MemoRepository(database)
    val viewModel: MemoViewModel = viewModel(factory = MemoViewModelFactory(repository))
    val allMemos by viewModel.allMemos.collectAsState(initial = emptyList())

    // 권한 정보
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    LaunchedEffect(Unit) {
        permissionsState.launchMultiplePermissionRequest()
    }
    val granted = permissionsState.permissions.any { it.status.isGranted }

    // 카메라 및 위치 정보
    val cameraPositionState = rememberCameraPositionState()
    val locationSource = rememberFusedLocationSource()

    // 클러스터링 로직
    val zoomLevel by remember { derivedStateOf { cameraPositionState.position.zoom } }
    val clusterThreshold = 0.02 // 클러스터링 거리 임계값 (도 단위, 약 2.2km)
    val clusters = remember(allMemos, zoomLevel) {
        if (zoomLevel < 14) { // 줌 레벨이 낮을 때 클러스터링 적용
            clusterMarkers(allMemos, clusterThreshold)
        } else {
            allMemos.map { Cluster(listOf(it), it.latitude, it.longitude) }
        }
    }

    // NaverMap 표시
    NaverMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        locationSource = locationSource,
        properties = MapProperties(
            locationTrackingMode = if (granted) LocationTrackingMode.Face else LocationTrackingMode.None
        ),
        uiSettings = MapUiSettings(
            isLocationButtonEnabled = true
        ),
        onMapLongClick = { _, latlng ->
            navController.navigate("memo?lat=${latlng.latitude}&lng=${latlng.longitude}")
        }
    ) {
        // 클러스터 및 개별 마커 렌더링
        clusters.forEach { cluster ->
            if (cluster.memos.size > 1) {
                // 클러스터 마커
                Marker(
                    state = rememberMarkerState(position = LatLng(cluster.lat, cluster.lng)),
                    captionText = "${cluster.memos.size}",
                    captionColor = Color.White,
                    captionHaloColor = Color.Black,
                    onClick = {
                        cameraPositionState.move(
                            CameraUpdate.scrollAndZoomTo(
                                LatLng(cluster.lat, cluster.lng),
                                zoomLevel + 2
                            ).animate(CameraAnimation.Easing, 500)
                        )
                        true
                    }
                )
            } else {
                // 개별 마커
                val memo = cluster.memos.first()
                ShowMarker(memo = memo, navController = navController)
            }
        }
    }

    // 권한 거부 시 메시지 표시
    if (!granted) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "위치 권한이 필요합니다.",
                color = Color.Red,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

// 클러스터링 데이터 클래스
data class Cluster(
    val memos: List<MemoEntity>,
    val lat: Double,
    val lng: Double
)

// 격자 기반 클러스터링 함수
fun clusterMarkers(memos: List<MemoEntity>, threshold: Double): List<Cluster> {
    val clusters = mutableListOf<Cluster>()
    val remainingMemos = memos.toMutableList()

    while (remainingMemos.isNotEmpty()) {
        val memo = remainingMemos.removeAt(0)
        val nearbyMemos = mutableListOf(memo)
        val iterator = remainingMemos.iterator()

        while (iterator.hasNext()) {
            val other = iterator.next()
            val distance = haversine(memo.latitude, memo.longitude, other.latitude, other.longitude)
            if (distance < threshold) {
                nearbyMemos.add(other)
                iterator.remove()
            }
        }

        // 클러스터의 중심점 계산 (평균 좌표)
        val avgLat = nearbyMemos.map { it.latitude }.average()
        val avgLng = nearbyMemos.map { it.longitude }.average()
        clusters.add(Cluster(nearbyMemos, avgLat, avgLng))
    }

    return clusters
}

// Haversine 공식으로 두 좌표 간 거리 계산 (단위: 도)
fun haversine(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val dLat = Math.toRadians(lat2 - lat1)
    val dLng = Math.toRadians(lng2 - lng1)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLng / 2) * Math.sin(dLng / 2)
    return 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
}