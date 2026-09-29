package com.wild0408.nanxinshiguang.data.db.main

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface GradeDao {
    @Query("SELECT * FROM grades ORDER BY semester DESC, courseName ASC")
    fun observeAll(): Flow<List<GradeEntity>>

    @Query("DELETE FROM grades WHERE semester = :semester")
    suspend fun deleteSemester(semester: String)

    @Upsert
    suspend fun upsertAll(records: List<GradeEntity>)
}
