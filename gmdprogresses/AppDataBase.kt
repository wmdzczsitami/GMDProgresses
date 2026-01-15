package com.itami.gmdprogresses

import androidx.room.Database
import androidx.room.RoomDatabase
import com.itami.gmdprogresses.manylist.ExtremeDemonEntity
import com.itami.gmdprogresses.manylist.ProgressRecord

// 1. entities 리스트에 올바른 클래스 이름이 있는지 확인
@Database(
    entities = [
        ExtremeDemonEntity::class,
        ProgressRecord::class  // ★ 1. 여기에 새로 만든 엔티티를 추가하세요!
    ],
    version = 2, // ★ 2. 버전을 기존보다 1 높게 수정하세요! (예: 1 -> 2)
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // 2. 반환 타입이 아까 수정한 인터페이스 이름과 맞는지 확인
    abstract fun whichlist(): whichtoupdatelist
}

