package com.wild0408.nanxinshiguang.data.di

import okio.Path

/**
 * 跨平台文件存储路径抽象
 */
interface AppStorage {
    val filesDir: Path
    val cacheDir: Path
    fun getDatabasePath(dbName: String): String
}