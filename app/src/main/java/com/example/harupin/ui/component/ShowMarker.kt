package com.example.harupin.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.example.harupin.roomDB.MemoEntity
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.compose.ExperimentalNaverMapApi
import com.naver.maps.map.compose.Marker
import com.naver.maps.map.compose.rememberMarkerState
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.MarkerIcons

@OptIn(ExperimentalNaverMapApi::class)
@Composable
fun ShowMarker(memo: MemoEntity, navController: NavController) {

    var mycolor = MarkerIcons.GREEN
    if(memo.isFavorite == true){
        mycolor = MarkerIcons.RED
    }

    Marker(
        state = rememberMarkerState(position = LatLng(memo.latitude, memo.longitude)),
        captionText = memo.locationName ?: memo.title,
        onClick = {
            navController.navigate("memo?id=${memo.id}")
            true
        },
        icon = mycolor
    )
}