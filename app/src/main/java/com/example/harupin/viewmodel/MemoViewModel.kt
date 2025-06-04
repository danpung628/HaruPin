package com.example.harupin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.harupin.roomDB.MemoEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MemoViewModelFactory(
    private val repository: MemoRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MemoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MemoViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class MemoViewModel(private val repository: MemoRepository) : ViewModel() {

    // 메모 전체 목록을 Flow로 노출
    val allMemos: StateFlow<List<MemoEntity>> =
        repository.getAllMemos()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    // 검색 결과 저장용 (선택적 사용)
    private val _searchResults = MutableStateFlow<List<MemoEntity>>(emptyList())
    val searchResults: StateFlow<List<MemoEntity>> = _searchResults

    // 메모 추가
    fun insertMemo(memo: MemoEntity) {
        viewModelScope.launch {
            repository.insertMemo(memo)
        }
    }

    // 메모 수정
    fun updateMemo(memo: MemoEntity) {
        viewModelScope.launch {
            repository.updateMemo(memo)
        }
    }

    // 메모 삭제
    fun deleteMemo(memo: MemoEntity) {
        viewModelScope.launch {
            repository.deleteMemo(memo)
        }
    }

    // 키워드로 검색 (searchResults StateFlow로 결과 저장)
    fun searchMemos(keyword: String) {
        viewModelScope.launch {
            repository.searchMemos(keyword).collect {
                _searchResults.value = it
            }
        }
    }

    fun getAllMemos() {
        viewModelScope.launch {
            repository.getAllMemos().collect {
                _searchResults.value = it
            }
        }
    }

    fun filterMemosByYear(year: String?) {
        viewModelScope.launch {
            if (year == null) {
                repository.getAllMemos().collect { memoList ->
                    _searchResults.value = memoList
                }
            } else {
                repository.filterMemosByYear(year).collect { memoList ->
                    _searchResults.value = memoList
                }
            }
        }
    }

    fun filterMemosByYearAndMonth(year: String?, month: String?) {
        viewModelScope.launch {
            if (year == null || month == null) {
                repository.getAllMemos().collect { memoList ->
                    _searchResults.value = memoList
                }
            } else {
                repository.filterMemosByYearAndMonth(year, month).collect { memoList ->
                    _searchResults.value = memoList
                }
            }
        }
    }
}
