package com.example.harupin.roomDB

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * RoomDatabase는 앱 전체에서 단 하나만 존재해야 하므로
 * 싱글톤(Singleton) 패턴으로 생성한다.
 */
@Database(entities = [MemoEntity::class], version = 1, exportSchema = false)
abstract class MemoDatabase : RoomDatabase() {

    // DAO를 가져오는 추상 메서드
    abstract fun getMemoDao(): MemoDao

    companion object {
        // Volatile: 여러 스레드에서 캐시 문제 없이 동기화되도록 보장
        @Volatile
        private var INSTANCE: MemoDatabase? = null

        fun getDatabase(context: Context): MemoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MemoDatabase::class.java,
                    "memo_database" // 실제 저장될 DB 파일 이름
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
