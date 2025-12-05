# 贡献指南

感谢你对 **哈基课程表** 项目的兴趣！

## 如何贡献

### 报告 Bug

如果你发现了 Bug，请通过 [Issues](https://github.com/MOAKIEE/haji-timetable/issues) 提交，使用 Bug 报告模板。

### 提出新功能

如果你有新功能的想法，欢迎通过 [Issues](https://github.com/MOAKIEE/haji-timetable/issues) 提交功能请求。

### 提交代码

1. Fork 这个仓库
2. 创建你的功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交你的更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开一个 Pull Request

## 开发环境

- Android Studio Ladybug 或更高版本
- JDK 11+
- Android SDK 36

## 代码规范

- 使用 Kotlin 官方代码风格
- 保持代码简洁、可读
- 为新功能添加适当的注释

## 项目结构

```
app/src/main/java/com/example/timetable/
├── MainActivity.kt              # Activity 入口
├── data/
│   ├── model/                   # 数据模型
│   └── repository/              # 数据持久化
├── ui/
│   ├── screens/                 # 页面
│   ├── components/              # 组件
│   └── dialogs/                 # 对话框
└── utils/                       # 工具类
```

## 许可证

贡献的代码将遵循 [MIT License](LICENSE)。
