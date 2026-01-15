package com.itami.gmdprogresses

import android.app.Application
import androidx.room.Room

class GDApplication : Application() {
    // 앱 전체에서 공통으로 사용할 데이터베이스 인스턴스
    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "demon_database"
        ).build()
    }
}