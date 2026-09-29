package com.wild0408.nanxinshiguang.data.db.main

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.wild0408.nanxinshiguang.data.model.GradeRecord

@Entity(tableName = "grades")
data class GradeEntity(
    @PrimaryKey val id: String,
    val courseName: String,
    val courseType: String,
    val credits: String,
    val score: String,
    val gradePoint: String,
    val semester: String,
    val status: String?,
    val teacher: String?,
    val examType: String?,
    val scoreComposition: String?
) {
    fun toModel() = GradeRecord(id, courseName, courseType, credits, score, gradePoint, semester, status, teacher, examType, scoreComposition)
}

fun GradeRecord.toEntity() = GradeEntity(id, courseName, courseType, credits, score, gradePoint, semester, status, teacher, examType, scoreComposition)
