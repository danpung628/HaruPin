package com.example.harupin.roomDB

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoDao {

    /**
     * 새로운 메모를 데이터베이스에 삽입하거나,
     * 동일한 기본 키(id)를 가진 메모가 이미 있으면 해당 메모를 덮어쓴다.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemo(memo: MemoEntity)

    /**
     * 기존 메모의 내용을 수정한다.
     * 메모 객체의 id 값을 기준으로 해당 메모를 찾아 업데이트한다.
     */
    @Update
    suspend fun updateMemo(memo: MemoEntity)

    /**
     * 모든 메모를 날짜 및 시간 순으로 내림차순 정렬하여 반환한다.
     * Flow를 반환하므로 실시간으로 UI에 반영 가능하다.
     */
    @Query("SELECT * FROM MemoTable ORDER BY date DESC, time DESC")
    fun getAllMemos(): Flow<List<MemoEntity>>


    /**
     * 제목 또는 위치 이름에 검색어가 포함된 메모들을 반환한다.
     * 최신 메모가 먼저 오도록 날짜 기준으로 정렬한다.
     */
    @Query("SELECT * FROM MemoTable WHERE title LIKE '%' || :keyword || '%' OR locationName LIKE '%' || :keyword || '%' ORDER BY date DESC")
    fun searchMemo(keyword: String): Flow<List<MemoEntity>>


    /**
     * 특정 메모를 데이터베이스에서 삭제한다.
     */
    @Delete
    suspend fun deleteMemo(memo: MemoEntity)


    //id 기준으로 데이터를 들고옴.
    @Query("SELECT * FROM MemoTable WHERE id = :id")
    fun getById(id: Int): Flow<List<MemoEntity>>

}
