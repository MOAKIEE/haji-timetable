# 贡献指南

感谢你对 **哈基课程表** 项目的关注！我们欢迎所有形式的贡献。

## 🐛 报告 Bug

发现 Bug？请通过 [Issues](https://github.com/MOAKIEE/haji-timetable/issues/new) 提交。

**Bug 报告应包含：**
- 问题描述
- 复现步骤
- 预期行为
- 实际行为
- 设备信息（Android 版本、机型）
- 截图或日志（如有）

## 💡 提出新功能

有好想法？通过 [Issues](https://github.com/MOAKIEE/haji-timetable/issues/new) 提交功能请求。

**功能请求应包含：**
- 功能描述
- 使用场景
- 预期收益
- 参考设计（如有）

## 🔧 提交代码

### 前置要求

- **Android Studio**: Ladybug (2024.2.1) 或更高版本
- **JDK**: 11 或更高版本
- **Android SDK**: API 36 (Android 15)
- **Kotlin**: 2.0.21
- **Gradle**: 8.7

### 开发流程

1. **Fork** 本仓库
2. **Clone** 你的 Fork
   ```bash
   git clone https://github.com/YOUR_USERNAME/haji-timetable.git
   ```
3. **创建分支**
   ```bash
   git checkout -b feature/your-feature-name
   # 或
   git checkout -b fix/your-bug-fix
   ```
4. **开发并测试**
5. **提交代码**
   ```bash
   git add .
   git commit -m "feat: 添加某功能" # 遵循 Conventional Commits
   ```
6. **推送到你的 Fork**
   ```bash
   git push origin feature/your-feature-name
   ```
7. **创建 Pull Request** 到 `main` 分支

### Commit 规范

遵循 [Conventional Commits](https://www.conventionalcommits.org/)：

- `feat:` 新功能
- `fix:` Bug 修复
- `docs:` 文档更新
- `style:` 代码格式（不影响功能）
- `refactor:` 重构
- `perf:` 性能优化
- `test:` 测试相关
- `chore:` 构建/工具配置

**示例：**
```
feat: 添加课程导出功能
fix: 修复日历同步崩溃问题
docs: 更新 README 安装说明
refactor: 重构 MainViewModel 状态管理
```

## 📐 代码规范

- **语言风格**: 遵循 [Kotlin 官方代码风格](https://kotlinlang.org/docs/coding-conventions.html)
- **命名规范**:
  - 类名：PascalCase (`MainViewModel`)
  - 函数/变量：camelCase (`loadData`, `currentSchedule`)
  - 常量：UPPER_SNAKE_CASE (`MAX_WEEKS`, `TAG`)
- **注释**: 为复杂逻辑添加 KDoc 注释
- **格式化**: 使用 Android Studio 自动格式化（Ctrl+Alt+L）

## 🏗️ 项目架构

```
app/src/main/java/com/example/timetable/
├── MainActivity.kt                  # 入口 Activity
├── data/
│   ├── model/                       # 数据模型（Course, Schedule 等）
│   ├── repository/                  # 数据层
│   │   ├── DataManager.kt          # SharedPreferences（当前）
│   │   └── TimetableRepository.kt  # Room（计划中）
│   └── room/                        # Room Database 结构
│       ├── entity/                  # 数据库实体
│       ├── dao/                     # 数据访问对象
│       └── TimetableDatabase.kt
├── ui/
│   ├── screens/                     # 页面级组件
│   │   ├── MainScreen.kt
│   │   ├── MainViewModel.kt        # MVVM ViewModel
│   │   └── SettingsScreen.kt
│   ├── components/                  # 可复用 UI 组件
│   ├── dialogs/                     # 对话框
│   └── theme/                       # Material3 主题
└── utils/                           # 工具类
    ├── DateUtils.kt                # 日期计算
    ├── CalendarHelper.kt           # 日历同步
    └── UpdateChecker.kt            # 更新检查
```

## ✅ Pull Request 检查清单

提交 PR 前请确认：

- [ ] 代码已格式化
- [ ] 遵循项目代码规范
- [ ] 添加了必要的注释
- [ ] 测试功能正常（手动测试）
- [ ] 无编译警告/错误
- [ ] Commit 信息符合规范
- [ ] 更新了相关文档（如有必要）

## 📝 更新日志

重大更改需要在 `CHANGELOG.md` 中记录：

```markdown
## [版本号] - 日期

### 新增
- 功能描述

### 修复
- Bug 描述

### 变更
- 改动描述
```

## 🤝 行为准则

- 尊重所有贡献者
- 保持专业和友善
- 接受建设性批评
- 关注项目最佳利益

## 📄 许可证

贡献的代码将遵循 [MIT License](LICENSE)。
