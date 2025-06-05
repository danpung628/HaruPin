package com.example.harupin.viewmodel

import com.example.harupin.roomDB.MemoDao
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

    fun getAllMemos() = dao.getAllMemos()

    fun searchMemos(keyword: String) = dao.searchMemo(keyword)

    fun filterMemosByYear(year: String) = dao.filterMemosByYear("$year%")

    fun filterMemosByYearAndMonth(year: String, month: String) =
        dao.filterMemosByYearAndMonth("$year-$month%")

    suspend fun updateMemoFavorite(memoId: Int, isFavorite: Boolean) {
        dao.updateMemoFavorite(memoId, isFavorite)
    }

    fun getById(id: Int) = dao.getById(id)


}
