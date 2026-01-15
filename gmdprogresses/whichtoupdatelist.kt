package com.itami.gmdprogresses

import androidx.room.*
import com.itami.gmdprogresses.manylist.ExtremeDemonEntity
import com.itami.gmdprogresses.manylist.ProgressRecord
import kotlinx.coroutines.flow.Flow

// 파일명: whichtoupdatelist.kt (또는 DemonDao.kt)
@Dao
interface whichtoupdatelist {

    // 이 부분을 수정하세요! name 대신 rank 순으로 정렬합니다.
    @Query("SELECT * FROM demons ORDER BY rank ASC")
    fun getAllDemons(): Flow<List<ExtremeDemonEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(demons: List<ExtremeDemonEntity>)

    @Update
    suspend fun updateProgress(demon: ExtremeDemonEntity)

    @Insert
    suspend fun insertRecord(record: ProgressRecord)
    // whichtoupdatelist.kt



    @Query("SELECT * FROM progress_records")
    fun getAllRecords(): kotlinx.coroutines.flow.Flow<List<ProgressRecord>>

    @Query("SELECT * FROM progress_records WHERE demonId = :demonId ORDER BY date DESC, percent DESC")
    fun getRecordsForDemon(demonId: Long): Flow<List<ProgressRecord>>
// DemonDatabase.kt 의 entities에 ProgressRecord::class 추가하고 버전 올리기!
}

