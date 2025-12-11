# 哈基课程表

一款简洁美观的 Android 课程表应用，使用 Kotlin + Jetpack Compose 开发。

[![GitHub release](https://img.shields.io/github/v/release/MOAKIEE/haji-timetable)](https://github.com/MOAKIEE/haji-timetable/releases)
[![License](https://img.shields.io/github/license/MOAKIEE/haji-timetable)](LICENSE)
[![Android](https://img.shields.io/badge/Android-7.0%2B-green.svg)](https://developer.android.com/about/versions/nougat)

## ✨ 功能特性

- 📅 **多课表管理** - 创建、切换、重命名和删除多个课表
- 📆 **周视图滑动** - 左右滑动切换周数，直观查看每周课程
- ⚠️ **智能冲突检测** - 自动检测时间冲突，支持部分重叠显示
- 🎨 **自定义主题** - 自由定制背景色、字体色和课程颜色
- ⏰ **作息时间** - 灵活设置每节课的上下课时间
- 📝 **课程管理** - 完善的课程增删改功能，支持设置名称、地点、教师、时间、周数
- 🔗 **连堂课** - 支持跨多节的连堂课程设置
- 📲 **日历同步** - 一键同步到系统日历，支持提醒设置
- 💾 **导入/导出** - JSON 格式导出/导入课表，支持二维码分享
- 📷 **二维码扫描** - 扫描或从相册识别二维码快速导入课表
- 📱 **多设备适配** - 完美支持手机和平板
- 🔄 **自动更新** - 每日首次启动自动检查更新
- 💾 **数据持久化** - 本地自动保存所有数据

## 🛠️ 技术栈

- **语言**: Kotlin 2.0.21
- **架构**: MVVM + Repository Pattern
- **UI**: Jetpack Compose + Material3
- **数据**: Room Database 2.6.1
- **依赖注入**: 原生 Kotlin (未来计划 Hilt)
- **网络**: OkHttp 4.12.0
- **JSON**: Gson 2.10.1
- **二维码**: ZXing 3.5.3
- **图片**: Coil 2.5.0
- **构建**: Gradle 8.7 + KSP
- **最低版本**: Android 7.0 (API 24)
- **目标版本**: Android 15 (API 36)

## 📦 安装

### 方式一：直接下载

前往 [Releases](https://github.com/MOAKIEE/haji-timetable/releases) 下载最新版 APK 安装即可。

### 方式二：源码构建

```bash
# 克隆仓库
git clone https://github.com/MOAKIEE/haji-timetable.git

# 使用 Android Studio 打开项目

# 构建
./gradlew assembleDebug
```

## 📱 快速上手

1. **添加课程** → 点击右下角 ➕ 按钮
2. **查看详情** → 点击课程卡片
3. **编辑/删除** → 课程详情页操作
4. **处理冲突** → 点击冲突课程，选择查看
5. **切换周数** → 左右滑动课表
6. **管理课表** → 左上角菜单
7. **导入/导出** → 右上角 ⋮ → 导入/导出
8. **同步日历** → 右上角 ⋮ → 同步至日历
9. **个性化** → 右上角 ⋮ → 设置

## 📝 版本历史

当前版本: **v0.9beta** | [完整更新日志](CHANGELOG.md)

### v0.9beta (2025-12-11)
- 📥 新增：完整的导入/导出功能
- 📤 支持 JSON 文件和二维码两种分享方式
- 📷 支持相机扫描和相册识别二维码
- ✅ 导入时可选择覆盖或新建课表
- 🎨 优化导入导出界面和用户体验

### v0.8beta (2025-12-10)
- 🚀 重大更新：Room Database 正式启用
- ⚡ 数据读写性能提升 50%+
- 🔒 更强的数据安全性和类型安全
- 🔄 自动数据迁移，用户无感知升级

### v0.7beta (2025-12-09)
- 🏗️ 重构：引入 MVVM 架构，代码减少 60%+
- ✨ 新增常量管理和基础日志系统
- 📦 集成 Room Database 基础设施
- ⚡ 性能优化：启动速度提升

### v0.6beta (2025-12-06)
- ✨ 自动更新检测（每日首次启动）
- ✨ 手动检查更新功能
- ✨ 支持忽略指定版本
- 🐛 修复侧边栏闪现问题

### v0.5beta (2025-12-05)
- ✨ 日历同步功能
- ✨ 闹钟提醒支持
- 🎨 UI 优化和彩蛋
- 🐛 修复起始周问题

## 🗂️ 项目结构

```
app/src/main/java/com/example/timetable/
├── MainActivity.kt
├── data/
│   ├── model/                      # 数据模型
│   │   ├── Course.kt
│   │   ├── Schedule.kt
│   │   ├── SectionTime.kt
│   │   └── AppSettings.kt
│   └── repository/
│       ├── DataManager.kt          # SharedPreferences 管理
│       └── TimetableRepository.kt  # Room Repository
├── ui/
│   ├── screens/                    # 页面
│   │   ├── MainScreen.kt
│   │   ├── MainViewModelRoom.kt    # MVVM
│   │   ├── SettingsScreen.kt
│   │   └── QRCodeScanActivity.kt   # 二维码扫描
│   ├── components/                 # 可复用组件
│   │   ├── CourseGrid.kt
│   │   ├── WeekHeader.kt
│   │   ├── ColorPicker.kt
│   │   ├── TimePickerButton.kt
│   │   └── UpdateDialog.kt
│   ├── dialogs/                    # 对话框
│   │   ├── CourseEditorDialog.kt
│   │   ├── CalendarSyncDialog.kt
│   │   ├── InputNameDialog.kt
│   │   └── ImportExportDialog.kt   # 导入导出相关
│   └── theme/                      # 主题
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
└── utils/                          # 工具类
    ├── DateUtils.kt
    ├── CalendarHelper.kt
    ├── UpdateChecker.kt
    ├── ImportExportHelper.kt       # 导入导出工具
    └── ScreenUtils.kt
```

## 🔐 权限说明

| 权限 | 用途 |
|------|------|
| `INTERNET` | 检查应用更新 |
| `CAMERA` | 扫描二维码导入课表 |
| `READ_CALENDAR` | 读取日历账户 |
| `WRITE_CALENDAR` | 同步课程到日历 |

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

参与贡献前请阅读：[贡献指南](CONTRIBUTING.md)

## 👤 作者

**MOAKIEE**

- GitHub: [@MOAKIEE](https://github.com/MOAKIEE)

## 📄 许可证

本项目基于 [MIT License](LICENSE) 开源。

---

⭐ 如果觉得不错，请给个 Star 支持一下！
