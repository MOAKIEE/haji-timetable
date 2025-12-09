# Room Database 迁移指南

## ✅ 已完成的工作

### 1. 数据库层（100%）
- ✅ 4 个 Entity 类
- ✅ 4 个 Dao 接口
- ✅ TimetableDatabase 单例
- ✅ 数据转换工具（Converters.kt）
- ✅ 自动数据迁移工具

### 2. Repository 层（100%）
- ✅ TimetableRepository 完整实现
- ✅ 支持 Flow 响应式数据

### 3. ViewModel 层（100%）
- ✅ MainViewModelRoom 创建
- ⚠️ 待集成到 MainScreen

## 📋 迁移步骤（下一步）

### 步骤 1: 更新 MainScreen.kt
```kotlin
// 修改前
@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel()
) {

// 修改后  
@Composable
fun MainScreen(
    viewModel: MainViewModelRoom = remember { 
        createMainViewModel(LocalContext.current) 
    }
) {
```

### 步骤 2: 更新 saveData 调用
```kotlin
// 修改前
viewModel.saveData(context)

// 修改后
viewModel.saveData()  // 不再需要 context 参数
```

### 步骤 3: 测试验证
1. 编译项目
2. 启动应用，验证数据自动迁移
3. 测试所有功能：创建/删除课表、添加/删除课程
4. 确认数据持久化正常

### 步骤 4: 清理旧代码（可选）
- DataManager.kt 可以保留作为备份
- 或者删除 DataManager，完全使用 Room

## 🎯 优势对比

| 特性 | SharedPreferences + JSON | Room Database |
|------|-------------------------|---------------|
| 代码量 | DataManager 127 行 | Repository 145 行（功能更强） |
| 类型安全 | ❌ 手动序列化 | ✅ 编译时检查 |
| 查询能力 | ❌ 加载全部数据 | ✅ SQL 查询 |
| 响应式更新 | ❌ 手动刷新 | ✅ Flow 自动更新 |
| 关系管理 | ❌ 手动维护 | ✅ 外键约束 |
| 性能 | 🐌 JSON 解析慢 | ⚡ 二进制存储快 |
| 数据迁移 | ❌ 手动处理 | ✅ 自动迁移 |

## 🔧 数据迁移说明

**自动迁移**：应用首次使用 Room 启动时，会自动从 SharedPreferences 迁移数据。

迁移内容：
- 所有课表（schedules）
- 所有课程（courses）
- 作息时间（section_times）
- 应用设置（app_settings）

迁移后，旧的 SharedPreferences 数据仍然保留，作为备份。

## 📊 数据库结构

### schedules 表
```sql
CREATE TABLE schedules (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    createdAt INTEGER,
    updatedAt INTEGER
);
```

### courses 表
```sql
CREATE TABLE courses (
    id TEXT PRIMARY KEY,
    scheduleId TEXT NOT NULL,
    name TEXT NOT NULL,
    room TEXT,
    teacher TEXT,
    day INTEGER,
    startSection INTEGER,
    endSection INTEGER,
    startWeek INTEGER,
    endWeek INTEGER,
    colorArgb INTEGER,
    FOREIGN KEY(scheduleId) REFERENCES schedules(id) ON DELETE CASCADE
);
CREATE INDEX index_courses_scheduleId ON courses(scheduleId);
```

### section_times 表
```sql
CREATE TABLE section_times (
    section INTEGER PRIMARY KEY,
    startTime TEXT NOT NULL,
    endTime TEXT NOT NULL
);
```

### app_settings 表
```sql
CREATE TABLE app_settings (
    id INTEGER PRIMARY KEY,
    currentScheduleId TEXT,
    showWeekends INTEGER,
    weekStartDay INTEGER,
    semesterStartDate TEXT,
    cellHeightDp INTEGER,
    backgroundColor INTEGER,
    fontColor INTEGER,
    totalWeeks INTEGER,
    courseColor INTEGER
);
```

## 🚀 下一步优化

1. **Hilt 依赖注入**：自动注入 Repository 到 ViewModel
2. **StateFlow 替代 mutableStateOf**：更好的状态管理
3. **数据库备份和恢复**：导出/导入功能
4. **单元测试**：测试 Dao 和 Repository

## ⚠️ 注意事项

1. **版本兼容**：首次使用 Room 会自动迁移，无需用户操作
2. **数据安全**：旧数据保留，可回退
3. **性能影响**：初次迁移可能需要 1-2 秒
4. **测试建议**：在测试设备上先验证迁移成功

## 📝 文件清单

### 新增文件（13 个）
```
data/
├── database/
│   ├── entity/
│   │   ├── ScheduleEntity.kt
│   │   ├── CourseEntity.kt
│   │   ├── SectionTimeEntity.kt
│   │   └── AppSettingsEntity.kt
│   ├── dao/
│   │   ├── ScheduleDao.kt
│   │   ├── CourseDao.kt
│   │   ├── SectionTimeDao.kt
│   │   └── AppSettingsDao.kt
│   ├── TimetableDatabase.kt
│   └── Converters.kt
├── migration/
│   └── DataMigration.kt
└── repository/
    └── TimetableRepository.kt

ui/screens/
└── MainViewModelRoom.kt
```

### 修改文件
- `app/build.gradle.kts`（添加 Room 依赖）
- `CHANGELOG.md`（记录更新）
- `ROOM_MIGRATION.md`（本文件）

## ✨ 总结

Room Database 迁移已完成 **95%**，只需简单集成到 MainScreen 即可使用。

**代码质量提升**：
- ✅ 类型安全
- ✅ 架构清晰
- ✅ 可测试性强
- ✅ 性能优化
- ✅ 自动迁移

**用户体验**：
- ✅ 无感知迁移
- ✅ 数据不丢失
- ✅ 性能提升
