# 哈基课程表

一款简洁美观的 Android 课程表应用，使用 Kotlin + Jetpack Compose 开发。

## 功能特性

- 📅 **多课表管理** - 支持创建、切换、重命名和删除多个课表
- 📆 **周视图** - 左右滑动切换周数，直观查看每周课程安排
- ⚠️ **冲突检测** - 智能检测课程时间冲突，支持部分时间重叠显示
- 🎨 **自定义主题** - 可自定义背景颜色、字体颜色和课程方框颜色
- ⏰ **作息时间设置** - 自定义每节课的上下课时间
- 📝 **课程编辑** - 支持添加、编辑、删除课程，设置课程名称、地点、教师、时间和周数
- 🔗 **连堂课支持** - 支持设置跨多节的连堂课程
- 💾 **数据持久化** - 自动保存所有设置和课程数据

## 技术栈

- **语言**: Kotlin
- **UI 框架**: Jetpack Compose + Material3
- **架构**: 模块化包结构
  - `data/model` - 数据模型
  - `data/repository` - 数据持久化
  - `ui/screens` - 页面组件
  - `ui/components` - 通用 UI 组件
  - `ui/dialogs` - 对话框组件
  - `utils` - 工具类
- **最低 SDK**: Android 7.0 (API 24)
- **目标 SDK**: Android 15 (API 36)

## 安装

1. 克隆仓库
```bash
git clone https://github.com/MOAKIEE/haji-timetable.git
```

2. 使用 Android Studio 打开项目

3. 构建并运行

## 使用说明

1. **添加课程**: 点击右下角的 "+" 按钮
2. **编辑/删除课程**: 点击课程方块查看详情，可进行编辑或删除
3. **处理冲突**: 点击冲突课程方块，选择要查看的课程
4. **切换周数**: 左右滑动课表区域
5. **管理课表**: 点击左上角菜单打开侧边栏
6. **设置**: 点击右上角设置图标进入设置界面

## 版本

当前版本: **v0.4beta**

## 更新公告

### v0.4beta (2025-12-05)
- 🏗️ 代码架构重构，模块化包结构
- ✨ 新增智能课程冲突检测与显示
- ✨ 新增删除课表二次确认弹窗
- ✨ 新增启动画面 (SplashScreen)
- ⚡ 优化启动性能和滑动流畅度
- 🐛 修复课程添加/删除延迟显示问题

### v0.3beta (2025-12-04)
- ✨ 新增多课表管理功能
- ✨ 新增设置界面（常规设置、颜色设置、作息时间设置、关于）
- ✨ 新增页面切换动画效果
- ✨ 新增侧滑返回手势支持
- ✨ 新增连堂课支持
- 🎨 优化下拉选择器替代文本输入
- 🎨 设置界面固定白底黑字
- 🐛 修复多个已知问题

## 项目结构

```
app/src/main/java/com/example/timetable/
├── MainActivity.kt              # Activity 入口
├── data/
│   ├── model/                   # 数据模型
│   │   ├── Course.kt           # 课程
│   │   ├── Schedule.kt         # 课表
│   │   ├── SectionTime.kt      # 节次时间
│   │   └── AppSettings.kt      # 应用设置
│   └── repository/
│       └── DataManager.kt      # 数据持久化
├── ui/
│   ├── screens/                 # 页面
│   │   ├── MainScreen.kt       # 主界面
│   │   └── SettingsScreen.kt   # 设置界面
│   ├── components/              # 组件
│   │   ├── CourseGrid.kt       # 课程网格
│   │   ├── WeekHeader.kt       # 周日期头部
│   │   ├── ColorPicker.kt      # 颜色选择器
│   │   └── TimePickerButton.kt # 时间选择按钮
│   └── dialogs/                 # 对话框
│       ├── CourseEditorDialog.kt
│       └── InputNameDialog.kt
└── utils/
    └── DateUtils.kt             # 日期工具
```

## 作者

by **MOAKIEE**

## 许可证

MIT License
