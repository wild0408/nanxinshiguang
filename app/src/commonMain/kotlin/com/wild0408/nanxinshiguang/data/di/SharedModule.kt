package com.wild0408.nanxinshiguang.data.di

import okio.FileSystem
import okio.Path
import okio.SYSTEM
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import com.wild0408.nanxinshiguang.data.portal.PortalSession
import com.wild0408.nanxinshiguang.data.portal.createPortalSession
import com.wild0408.nanxinshiguang.data.repository.PortalCredentialRepository
import com.wild0408.nanxinshiguang.data.repository.DataStorePortalCredentialRepository
import com.wild0408.nanxinshiguang.data.repository.AcademicSummaryRepository
import com.wild0408.nanxinshiguang.data.repository.DataStoreAcademicSummaryRepository

@Module(includes = [
    DatabaseModule::class,
    DataStoreModule::class
])
@ComponentScan("com.wild0408.nanxinshiguang")
class SharedModule {

    @Single
    fun provideElectricityApi(): com.wild0408.nanxinshiguang.data.api.electricity.ElectricityApi =
        com.wild0408.nanxinshiguang.data.api.electricity.createElectricityApi()

    /** 公交仓库全局单例：此前在页面内按需创建，导致每次进入页面都新建一个 HttpClient 且从不关闭。 */
    @Single
    fun provideBusRepository(): com.wild0408.nanxinshiguang.data.api.bus.BusRepository =
        com.wild0408.nanxinshiguang.data.api.bus.createBusRepository()

    /** 每日一言接口（自带超时与 JSON 反序列化） */
    @Single
    fun provideDailyQuoteApi(): com.wild0408.nanxinshiguang.data.api.quote.DailyQuoteApi =
        com.wild0408.nanxinshiguang.data.api.quote.createDailyQuoteApi()

    @Single
    fun providePortalSession(repository: PortalCredentialRepository): PortalSession =
        createPortalSession(repository)

    @Single
    fun providePortalCredentialRepository(repository: DataStorePortalCredentialRepository): PortalCredentialRepository = repository

    @Single
    fun provideAcademicSummaryRepository(repository: DataStoreAcademicSummaryRepository): AcademicSummaryRepository = repository

    @Single
    fun provideFileSystem(): FileSystem = FileSystem.SYSTEM

    @Single
    @Named("FilesDir")
    fun provideFilesDir(appStorage: AppStorage): Path {
        return appStorage.filesDir
    }

    /**
     * 提供全局应用缓存目录路径
     */
    @Single
    @Named("CacheDir")
    fun provideCacheDir(appStorage: AppStorage): Path {
        return appStorage.cacheDir
    }
}
