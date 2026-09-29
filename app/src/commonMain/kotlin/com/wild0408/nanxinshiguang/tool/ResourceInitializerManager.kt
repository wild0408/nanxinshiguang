package com.wild0408.nanxinshiguang.tool

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import okio.openZip
import okio.use
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import nanxinshiguang.generated.resources.Res

/**
 * 资源与缓存初始化管理器。
 *
 * 负责应用启动时的静态离线资源解压校验，以及过期的分享与下载临时缓存清理。
 */
@Suppress("unused")
@Single(createdAtStart = true)
class ResourceInitializerManager(
    private val fileSystem: FileSystem,
    @Named("FilesDir") private val filesDir: Path,
    @Named("CacheDir") private val cacheDir: Path
) {
    private val targetRepoDir: Path = filesDir / "repo"
    private val targetGradeRepoDir: Path = filesDir / "repo" / "grades"
    private val shareTempDir: Path = cacheDir / "share_temp"

    init {
        CoroutineScope(Dispatchers.IO).launch {
            initializeOfflineRepo()
            // 成绩脚本随 APK 发布，启动时覆盖同步，避免旧版本脚本残留导致流程不一致。
            initializeOfflineGradeRepo(forceOverwrite = true)
            clearTempCaches()
        }
    }

    @OptIn(ExperimentalResourceApi::class)
    suspend fun initializeOfflineGradeRepo(forceOverwrite: Boolean = false): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            if (!forceOverwrite && fileSystem.exists(targetGradeRepoDir / "resources")) return@runCatching
            val zipBytes = Res.readBytes("files/offline_grades.zip")
            val tempZipFile = filesDir / "temp_offline_grades.zip"
            fileSystem.write(tempZipFile) { write(zipBytes) }
            try {
                val zipFileSystem = fileSystem.openZip(tempZipFile)
                if (fileSystem.exists(targetGradeRepoDir)) fileSystem.deleteRecursively(targetGradeRepoDir)
                fileSystem.createDirectories(targetGradeRepoDir)
                unzipDirectory(zipFileSystem, "/".toPath(), targetGradeRepoDir)
            } finally {
                fileSystem.delete(tempZipFile)
            }
        }
    }

    /**
     * 校验并解压内置离线适配仓库。
     *
     * @param forceOverwrite 是否强制清除并重新解压覆盖现有本地仓库
     */
    @OptIn(ExperimentalResourceApi::class)
    suspend fun initializeOfflineRepo(forceOverwrite: Boolean = false): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            if (!forceOverwrite && fileSystem.exists(targetRepoDir / "index")) {
                return@runCatching
            }

            val zipBytes = Res.readBytes("files/offline_schools.zip")
            val tempZipFile = filesDir / "temp_offline_schools.zip"

            fileSystem.write(tempZipFile) {
                write(zipBytes)
            }

            try {
                val zipFileSystem = fileSystem.openZip(tempZipFile)

                if (fileSystem.exists(targetRepoDir)) {
                    fileSystem.deleteRecursively(targetRepoDir)
                }
                fileSystem.createDirectories(targetRepoDir)

                unzipDirectory(zipFileSystem, "/".toPath(), targetRepoDir)
            } finally {
                fileSystem.delete(tempZipFile)
            }
        }
    }

    /**
     * 清理应用生成的临时文件与过期缓存目录。
     */
    suspend fun clearTempCaches(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            if (fileSystem.exists(shareTempDir)) {
                fileSystem.deleteRecursively(shareTempDir)
            }

            val tempSchoolsRepo = cacheDir / "temp_schools_repo"
            val tempIndexRepo = cacheDir / "temp_index_repo"
            if (fileSystem.exists(tempSchoolsRepo)) fileSystem.deleteRecursively(tempSchoolsRepo)
            if (fileSystem.exists(tempIndexRepo)) fileSystem.deleteRecursively(tempIndexRepo)
        }
    }

    /**
     * 递归解压 Zip 虚拟文件系统中的目录与文件。
     */
    private fun unzipDirectory(
        zipFileSystem: FileSystem,
        currentZipPath: Path,
        targetDir: Path
    ) {
        val entries = zipFileSystem.list(currentZipPath)
        for (entry in entries) {
            val destinationPath = targetDir / entry.name

            if (!destinationPath.toString().startsWith(targetDir.toString())) {
                throw IllegalArgumentException("Illegal zip path: ${entry.name}")
            }

            val metadata = zipFileSystem.metadata(entry)

            if (metadata.isDirectory) {
                fileSystem.createDirectories(destinationPath)
                unzipDirectory(zipFileSystem, entry, destinationPath)
            } else {
                destinationPath.parent?.let { fileSystem.createDirectories(it) }

                zipFileSystem.source(entry).use { source ->
                    fileSystem.sink(destinationPath).buffer().use { sink ->
                        sink.writeAll(source)
                    }
                }
            }
        }
    }
}
