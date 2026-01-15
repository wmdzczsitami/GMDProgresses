package com.itami.gmdprogresses.manylist

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey


@Entity(
    tableName = "progress_records",
    foreignKeys = [ForeignKey(
        entity = ExtremeDemonEntity::class,
        parentColumns = ["id"],
        childColumns = ["demonId"],
        onDelete = ForeignKey.CASCADE
    )]
)

data class ProgressRecord(
    @PrimaryKey(autoGenerate = true) val recordId: Int = 0,
    val demonId: Long,
    val percent: Int,
    val videoUrl: String, // ★ 유튜브 링크 저장용 필드 추가
    val date: String
)