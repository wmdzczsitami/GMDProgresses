package com.itami.gmdprogresses.manylist

import androidx.room.Entity
import androidx.room.PrimaryKey




@Entity(tableName = "demons")
data class ExtremeDemonEntity(
    @PrimaryKey val id: Long,   // 맵 ID (2374518)
    val rank: Int,              // 순위 (1317)
    val name: String,           // 맵 이름 (Red World)
    val creator: String,        // 퍼블리시어 (saRy)
    val verifier: String,       // 베리파이어 (saRy 또는 -)
    val length: String,         // 맵 길이 (1min 30s)
    val objects: String,        // 맵 오브젝트 (11,088)
    val tier: String,           // 티어 (Beginner Not Worthy List)
    val song: String,           // 노래 (DJ-Nate - Clubstep)
    val myProgress: Int = 0     // 내 최고 기록 (리스트 표시용)
)