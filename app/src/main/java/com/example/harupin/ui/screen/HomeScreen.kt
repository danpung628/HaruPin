package com.example.harupin.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.harupin.ui.component.ShowMarker
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.model.Marker
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
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

    //권한 정보==================
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
    //권한정보==================

    //카메라 등의 현재 위치 정보
    val cameraPositionState = rememberCameraPositionState()
    val locationSource = rememberFusedLocationSource()

    //클릭용
    val makerPostion = remember { mutableStateOf<LatLng?>(null) }

    //현재 위치 보기
    if (granted) {
        NaverMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            locationSource = locationSource,
            properties = MapProperties(
                locationTrackingMode = LocationTrackingMode.Face
            ),
            uiSettings = MapUiSettings(
                isLocationButtonEnabled = true
            ),
            onMapLongClick = { _, latlng ->
                //문자 열로 송신
                navController.navigate("memo?lat=${latlng.latitude}&lng=${latlng.longitude}")
                //Navgraph로 memo 작성으로 연결
            }
        ) {

        }
    }

    //자유롭게 보기
    else {
        NaverMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            locationSource = locationSource,
            properties = MapProperties(
                locationTrackingMode = LocationTrackingMode.None
            ),
            uiSettings = MapUiSettings(
                isLocationButtonEnabled = true
            ),
            onMapLongClick = { _, latlng ->
                //문자 열로 송신
                navController.navigate("memo?lat=${latlng.latitude}&lng=${latlng.longitude}")
                //Navgraph로 memo 작성으로 연결
            }
        )
        {

        }
    }


}