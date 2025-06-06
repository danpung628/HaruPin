package com.example.harupin.roomDB

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "MemoTable")
data class MemoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,              // 메모 제목
    val content: String,            // 메모 본문

    val year: Int,                 // 예: 2025
    val month: Int,                // 예: 6
    val date: String,              // 예: "2025-06-02"
    val time: String,              // 예: "14:30"

    val latitude: Double,           // 위치 정보 (위도)
    val longitude: Double,          // 위치 정보 (경도)
    val locationName: String,       // 위치 이름 (예: 경복궁)

    val weather: String,            // 날씨 정보 (예: 맑음, 흐림)

    // 이미지는 최대 3장 추가
    val imageUri1: String? = null,
    val imageUri2: String? = null,
    val imageUri3: String? = null,

    val isFavorite: Boolean? = false // 즐겨찾기 여부
)