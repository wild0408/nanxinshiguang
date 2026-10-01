# 南信拾光

面向南京信息工程大学（NUIST）的课程表与校园信息工具。
本项目基于[拾光课程表](https://github.com/XingHeYuZhuan/shiguangschedule)进行二次开发，针对 NUIST 教务系统和统一门户进行了深度的功能适配与架构重构。

> 本项目为社区维护的非官方项目，与南京信息工程大学及其官方信息系统没有隶属或授权关系。

---

## 项目状态

- 当前版本为 **1.0.2（versionCode 3）**，仅支持 Android。
- 课程表、课程管理、提醒、小组件和数据导入导出能力来自上游项目并持续维护。
- 已加入 NUIST 教务导入、成绩中心、学业概览、统一门户绑定和宿舍电费查询等功能。
- 已加入劳动积分查询：首次进入且没有本地缓存时获取，成功结果会加密保存；之后仅通过页面刷新按钮手动更新。
- 已加入校园公交实时地图：内置校园底图、站点和车辆标记，支持 Material 与 Miuix 两套服务页入口。
- 成绩、学业概览、劳动积分和电费功能依赖学校服务器、登录状态及接口可用性；学校服务不可用时，相关功能可能无法加载。
- 成绩中心和劳动积分页面同时提供 Material 3 与 Miuix 两套界面，劳动积分详情按官方核算、实时活动和同步状态分组展示。
- Release 由项目专用证书签名；GitHub Actions 提供手动触发的 Android 发布构建。

### v1.0.2 发布内容

- 新增劳动积分查询与本地缓存。
- 新增校园公交实时地图，支持站点和车辆位置查看。
- 优化 Material 3 与 Miuix 服务页面体验。

---

## 项目结构

- `app/`：唯一 Android 应用模块。
- `app/src/main/`：`AndroidManifest.xml`、Android 原生资源与 assets。
- `app/src/commonMain/`：跨平台核心业务逻辑、Proto 协议定义、`ui/viewmodel` 共享 ViewModel/UiState 以及 `ui/material` 基础 UI 界面。
- `app/src/androidMain/`：Android 平台专属实现、系统组件集成（桌面小组件、通知 Worker、精确闹钟）与 `ui/miuix` / HyperOS 视觉组件。
- `app/src/commonTest/`：业务逻辑单元测试。
- `app/schemas/`：Room 数据库 schema 迁移文件。
- `docs/images/`：项目文档展示素材。
- `fastlane/metadata/`：Android 发布商店元数据。
- `gradle/`：Gradle Wrapper 与版本目录 (`libs.versions.toml`)。

项目架构已全面对齐 **双 UI 体系与 ViewModel 域结构**：
- 视图层：Material 3 视图包 `ui/material/<domain>` 与 MIUIX 视图包 `ui/miuix/<domain>`。
- 业务层：统一收拢至 `ui/viewmodel/<domain>` 领域子包。

本项目仓库：[wild0408/nanxinshiguang](https://github.com/wild0408/nanxinshiguang)。

---

## 主要功能

### 课程表

- 今日课表与周课表展示
- 多课表与作息方案管理
- 课程增删改、周次调整与课程颜色自定义
- 深色模式、配色主题与课表布局个性化
- 课程提醒、勿扰模式与 Android 桌面小组件

### NUIST 教务适配

- 通过 NUIST 教务系统 Web 导入课程数据
- 支持当前学期课程数据处理
- 支持 NUIST 成绩数据导入与本地持久化保存
- 成绩中心默认显示当前学期，支持切换学期并查看课程成绩详情

### 学业与校园服务

- 学业概览卡片：平均绩点、GPA、平均成绩等指标
- 成绩查询详情页
- 劳动积分总览与详情：总积分、理论劳动、生活劳动、服务劳动、专业劳动、竞赛积分及确认/归档状态
- 劳动积分区分官方核算结果和活动页实时累计数据；实时数据可能早于官方核算，详情页会分别标注来源
- 统一门户账号绑定
- 宿舍电费查询与历史趋势图表

### 校园公交

- 在“服务”页进入校园公交地图，查看校园底图、站点和公交车辆实时位置。
- 支持地图缩放拖拽、车辆状态显示和手动刷新；数据依赖校园公交平台接口。

### 数据能力

- JSON 课表导入与导出
- ICS 日历导出与系统日历同步
- WebDAV 备份与恢复
- 本地数据库与设置持久化

### 数据获取与缓存

- 成绩数据写入本地 Room 数据库，应用重启后仍可查看；成绩详情页通过顶栏刷新按钮手动同步。
- 学业概览和劳动积分使用加密 DataStore 缓存，已有缓存时进入页面不会自动重复请求。
- 劳动积分首次查询成功后（包括平台返回暂无记录的空结果）会保存当前账号的状态；切换账号或解绑后不会复用其他账号的数据。
- 查询失败时优先保留已有数据，并在页面内显示错误提示；未绑定账号、暂无数据和服务异常分别提供对应操作入口。

---

## 构建与安装

需要 JDK 21 和 Android SDK。可使用 Android Studio，或在命令行运行 Gradle Wrapper。

调试构建与单元测试：

```powershell
./gradlew.bat :app:assembleDebug --no-daemon --console=plain
./gradlew.bat :app:testDebugUnitTest --no-daemon --console=plain
```

APK 按 `arm64-v8a`、`armeabi-v7a` 和 `x86_64` 分别生成，不提供通用 APK，输出目录为：

```text
app/build/outputs/apk/debug/
app/build/outputs/apk/release/
```

选择与设备 ABI 对应的 APK 安装。例如，在 arm64 设备上安装本项目签名的 1.0.2 Release：

```powershell
adb install -r app/build/outputs/apk/release/nanxinshiguang-v1.0.2-arm64-v8a-release.apk
```

`-r` 仅适用于同包名且签名兼容的已安装版本。Debug 与 Release 使用不同证书，不能直接互相覆盖；如需切换签名，先评估和备份应用数据，**不要为了安装而直接卸载现有应用**。旧版拾光课程表与本项目的 applicationId 不同，可分别安装。

### Release 签名

本项目使用 alias `nanxinshiguang` 的专用发布证书。证书 SHA-256 指纹为：

```text
C6:23:57:85:44:DF:D6:12:2E:8C:37:1E:D4:55:5A:CF:90:41:1A:FD:C0:4B:92:67:CC:D7:91:6B:76:3B:AA:3D
```

本地 Release 构建需要在当前进程中提供以下环境变量，缺失时构建会失败：

| 环境变量 | 内容 |
|---|---|
| `NANXINSHIGUANG_KEYSTORE_FILE` | 本地密钥库文件的绝对路径 |
| `NANXINSHIGUANG_KEYSTORE_PASSWORD` | 密钥库密码 |
| `NANXINSHIGUANG_KEY_ALIAS` | `nanxinshiguang` |
| `NANXINSHIGUANG_KEY_PASSWORD` | 私钥密码 |

```powershell
./gradlew.bat :app:assembleRelease --no-configuration-cache
```

不要把密钥库、密码或带密码的 Gradle 参数写入仓库与构建日志。密码不要硬编码在脚本中；密钥库与可恢复的密码应分别离线备份。安装或分发前，使用 Android SDK 的 `apksigner verify --print-certs` 核对 APK 证书指纹。

### GitHub Actions

`Android CI Build` 工作流由 Actions 页面手动触发，使用 `Release-Signing` 环境中的 `KEYSTORE_BASE64`、`KEYSTORE_PASSWORD`、`KEY_ALIAS` 和 `KEY_PASSWORD` Secrets 构建 Release。构建成功后可从该次运行下载 `app-release-apk` 产物，包含三个 ABI 的 APK，保留 7 天。Fork 仓库不会继承这些 Secrets，需自行配置签名后才能运行发布构建。

---

## 开源来源与许可证

本项目是拾光课程表的派生项目。上游项目及本项目的主要代码使用 Apache License 2.0，许可证文本见 [`LICENSE`](LICENSE)，完整的来源与修改声明见 [`NOTICE`](NOTICE)。

- 上游主仓库：[XingHeYuZhuan/shiguangschedule](https://github.com/XingHeYuZhuan/shiguangschedule)

### 修改声明

本仓库中源自上游项目的文件均已针对南信拾光专版做过修改（包与应用标识、单模块工程结构、Material 3 与 Miuix/Hyper 双 UI 层、NUIST 各项服务接入等），修改自 2025 年起由本仓库维护者进行。该声明对全仓库生效并随 `NOTICE` 一并保留；上游版权行与 Apache-2.0 许可证文本不得移除或替换。

### 移植的第三方源码

`app/src/androidMain/kotlin/com/kyant/` 下的源码移植自 Kyant 的 Backdrop / 连续圆角实现（[Kyant0/AndroidLiquidGlass](https://github.com/Kyant0/AndroidLiquidGlass)，`io.github.kyant0:backdrop`、`io.github.kyant0:shapes`，Apache-2.0，Copyright 2025 Kyant）。这些文件保留了上游版权与许可证头。

### 接口参考的边界

NUIST 学业概览、成绩明细、电费与劳动积分的请求协议参考了公开项目 [DuoHuo/nuist-sta-app](https://github.com/DuoHuo/nuist-sta-app)（核对至提交 `6e9f464`）。**该项目未声明任何开源许可证**，因此不得从其复制、翻译或改写源码；本项目仅基于可公开观察到的接口行为独立实现，后续如需引用其代码必须先解决许可问题。

重新分发本项目或其衍生版本时，请保留 Apache-2.0 许可证、原作者版权和归属声明，一并附带 `NOTICE`，并在修改文件中说明修改内容。项目中使用的第三方依赖许可证可在应用内“开源许可证”页面查看，该页面由构建期生成的 AboutLibraries 元数据离线渲染。

---

## 隐私与安全

- 账号凭据仅用于访问用户主动使用的学校服务。
- 成绩、课表和电费数据主要保存在本地设备。
- 请勿提交真实账号、密码、通行密钥、Cookie、Token 或个人成绩数据。
- 使用学校统一门户和教务系统时，请遵守学校信息系统的使用规则。

---

## 参与开发

欢迎提交 Issue、功能建议和代码改进。提交代码前请确认：

1. 不包含真实账号、密码、Cookie、Token 或个人信息。
2. 新增的第三方代码具有明确许可证，并保留必要的版权和归属信息。
3. NUIST 接口相关改动经过脱敏，且不会绕过学校系统的安全校验。
4. 提交前完成必要的构建和测试。

---

## 免责声明

本项目按现状提供，不保证学校接口持续可用，也不保证教务系统、统一门户或电费系统的响应格式长期不变。使用本项目产生的账号、数据和系统风险由使用者自行承担。
