package com.example.harupin.viewmodel

import com.example.harupin.roomDB.MemoDatabase
import com.example.harupin.roomDB.MemoEntity
import kotlinx.coroutines.flow.Flow

class MemoRepository(private val db: MemoDatabase) {

    private val dao = db.getMemoDao()

    suspend fun insertMemo(memoEntity: MemoEntity) {
        dao.insertMemo(memoEntity)
    }

    suspend fun updateMemo(memoEntity: MemoEntity) {
        dao.updateMemo(memoEntity)
    }

    suspend fun deleteMemo(memoEntity: MemoEntity) {
        dao.deleteMemo(memoEntity)
    }

    fun getAllMemos(): Flow<List<MemoEntity>> = dao.getAllMemos()

    fun searchMemos(keyword: String): Flow<List<MemoEntity>> = dao.searchMemo(keyword)
}
