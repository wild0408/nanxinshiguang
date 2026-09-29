package com.wild0408.nanxinshiguang.data.db.main

import androidx.room3.Dao
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import androidx.room3.Query
import androidx.room3.Insert
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "electricity_history")
data class ElectricityHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: String,
    val roomKey: String,
    val recordedAt: Long,
    val balance: Double
)

@Dao
interface ElectricityHistoryDao {
    @Query("SELECT * FROM electricity_history WHERE studentId = :studentId AND roomKey = :roomKey ORDER BY recordedAt DESC LIMIT 100")
    fun observe(studentId: String, roomKey: String): Flow<List<ElectricityHistory>>

    @Insert
    suspend fun insert(item: ElectricityHistory)

    @Query("SELECT * FROM electricity_history WHERE studentId = :studentId AND roomKey = :roomKey ORDER BY recordedAt DESC LIMIT 1")
    suspend fun latest(studentId: String, roomKey: String): ElectricityHistory?

    @Query("DELETE FROM electricity_history WHERE studentId = :studentId AND roomKey = :roomKey")
    suspend fun delete(studentId: String, roomKey: String)

    @Query("DELETE FROM electricity_history WHERE id NOT IN (SELECT id FROM electricity_history WHERE studentId = :studentId AND roomKey = :roomKey ORDER BY recordedAt DESC LIMIT 100) AND studentId = :studentId AND roomKey = :roomKey")
    suspend fun trim(studentId: String, roomKey: String)
}
