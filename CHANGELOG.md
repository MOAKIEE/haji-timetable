# 更新日志

所有显著的更改都将记录在此文件中。

格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/)。

## [Unreleased]

### 计划中
- [ ] Room Database 完全迁移
- [ ] Hilt 依赖注入
- [ ] 单元测试
- [ ] UI 测试
- [ ] 导入导出功能
- [ ] 课程分享
- [ ] 小组件支持

### 技术债务
- Room Database 基础设施已完成，待集成到 UI 层
- MainViewModelRoom 已创建，需替换当前 MainViewModel
- 需要添加数据迁移的用户提示

## [0.7beta] - 2025-12-09

### 重构
- **架构重大升级**：全面引入 MVVM 架构模式
  - 创建 `MainViewModel` 集中管理 UI 状态和业务逻辑
  - MainScreen 代码量从 885 行优化至 766 行（-13.5%）
  - 所有对话框状态统一管理（13 个状态）
  - 数据操作方法集中到 ViewModel（loadData、saveData、CRUD）
- 使用 `lifecycle-viewmodel-compose:2.7.0` 实现 ViewModel 集成
- 优化状态管理，提升代码可维护性和可测试性

### 新增
- **常量管理**：提取所有魔法数字和字符串为常量
  - `CURRENT_VERSION`、`DEFAULT_SCHEDULE_NAME`、延迟时间常量等
- **日志系统**：添加分级日志功能
  - 支持 DEBUG、INFO、WARNING、ERROR 四个级别
  - 关键操作点日志记录（数据加载/保存、课表操作、更新检查）
- **Room Database 完整基础设施**（准备切换）
  - 4 个 Entity 类（Schedule、Course、SectionTime、AppSettings）
  - 4 个 Dao 接口，支持 Flow 响应式数据流
  - TimetableDatabase 单例模式
  - TimetableRepository 仓库层，统一数据访问
  - DataMigration 自动迁移工具（SharedPreferences → Room）
  - Converter 工具，Entity ↔ Model 双向转换
- 添加 KSP 插件 2.0.21-1.0.25 用于 Room 注解处理
- 添加 Room 2.6.1 依赖（runtime、ktx、compiler）

### 优化
- 数据访问层使用 Repository 模式
- 支持数据库外键约束和级联删除
- 数据库索引优化查询性能
- 错误处理增强（try-catch + 日志记录）

### 变更
- 更新检查逻辑整合到 ViewModel
- 更新对话框显示逻辑优化
- 版本号管理统一到常量

### 文档
- 创建 ROOM_MIGRATION.md 迁移指南
- 更新项目结构文档

## [0.6beta] - 2025-12-06

### 新增
- 自动更新检测功能，每天首次启动自动检查 GitHub Release
- 关于页面添加"检查更新"按钮
- 支持忽略指定版本的更新提醒
- 手动检查更新时自动清除忽略状态

### 修复
- 修复应用启动时侧边栏短暂闪现的问题

### 变更
- "代码仓库"改为按钮样式，与"检查更新"样式统一

## [0.5beta fix] - 2025-12-05

## [0.5beta] - 2025-12-05

### 新增
- 日历同步功能，支持将课程导出到系统日历
- 闹钟提醒选项，可自定义提前提醒时间
- 关于页面添加代码仓库链接
- 彩蛋功能
- 适配平板显示

### 变更
- 右上角设置图标改为三点菜单样式
- 统一对话框按钮样式

### 修复
- 修复课程起始周设置问题
- 修复彩蛋点击即关闭的问题

### 优化
- 优化图标点击动画效果

## [0.4beta] - 2025-12-05

### 新增
- 智能课程冲突检测与显示
- 删除课表二次确认弹窗
- 启动画面 (SplashScreen)

### 重构
- 代码架构重构，采用模块化包结构

### 优化
- 启动性能优化
- 滑动流畅度提升

### 修复
- 修复课程添加/删除延迟显示问题

## [0.3beta] - 2025-12-04

### 新增
- 多课表管理功能
- 设置界面（常规设置、颜色设置、作息时间设置、关于）
- 页面切换动画效果
- 侧滑返回手势支持
- 连堂课支持

### 优化
- 下拉选择器替代文本输入

### 修复
- 修复多个已知问题


### 新增
- 项目初始化
- 基础 UI 框架搭建
