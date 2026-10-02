# -------------------------------------------------------------------------
# R8/ProGuard 混淆配置文件
# -------------------------------------------------------------------------

# 基础全局设置 ---
-keepattributes SourceFile,LineNumberTable,Signature,InnerClasses,EnclosingMethod,AnnotationDefault,*Annotation*

# 依赖注入 (Koin) ---
# 保留 Koin 核心类及 DSL 相关
-keep class org.koin.** { *; }

# 保留 Koin Annotations 及其生成的模块
# 注：本项目使用 Koin 编译器插件（org.koin.plugin.module.dsl + @KoinApplication），
# DI 代码生成在应用自身包内，`org.koin.ksp.generated` 包并不存在，故不再保留该包。
-keep @org.koin.core.annotation.Module class * { *; }

# 确保 Koin 能够调用被注解类的构造函数进行依赖注入
-keepclassmembers class * {
    @org.koin.core.annotation.Single <init>(...);
    @org.koin.core.annotation.Factory <init>(...);
    @org.koin.core.annotation.KoinViewModel <init>(...);
    @org.koin.core.annotation.Named <init>(...);
}

# 原生组件与 WorkManager
# AppWidgetProvider 与 ListenableWorker 的 keep 规则已由库自带 consumer 规则覆盖：
# AppWidgetProvider 由 Manifest 声明、AAPT2/R8 会自动保留；WorkManager 的 AAR 自带
# `-keep class * extends androidx.work.ListenableWorker` 等规则。重复声明只会妨碍裁剪。
-keep class com.wild0408.nanxinshiguang.widget.** { *; }

# 网络库 (Ktor)
-dontwarn io.ktor.**

# 移除 Android 系统调试日志 (v/d/i/w)
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
}

# 数据解析 (Kotlinx Serialization & Wire Protobuf) ---
# kotlin.Metadata 无需显式保留：上面 -keepattributes 已含 *Annotation*，
# kotlin-stdlib 的 consumer 规则也会保留被 keep 类的注解属性。
-keep @kotlinx.serialization.Serializable class * { ** Companion; }
-keep class **$$serializer { *; }

-keep class * implements com.squareup.wire.Message {
    <fields>;
    <methods>;
}
-keep class * implements com.squareup.wire.WireEnum { *; }
-keepclassmembers class * implements com.squareup.wire.Message {
    public static *** ADAPTER;
}
-keep class * extends com.squareup.wire.ProtoAdapter { *; }


# 数据模型与数据库
# androidx.sqlite 的 keep 已由 Room 的 consumer 规则覆盖，仅保留 dontwarn 以免缺失类告警。
-dontwarn androidx.sqlite.**
-keep class com.wild0408.nanxinshiguang.data.db.** { *; }
-keep class com.wild0408.nanxinshiguang.data.model.** { *; }
-keep class com.wild0408.nanxinshiguang.ui.viewmodel.** { *; }
