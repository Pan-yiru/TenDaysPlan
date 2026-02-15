# 十日谈 (TenDaysPlan)

<div align="center">

![Android](https://img.shields.io/badge/Android-34%2B-green)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-blue)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-1.5+-purple)
![License](https://img.shields.io/badge/License-MIT-yellow)

一款基于十天周期制的Android任务管理与时间追踪应用

</div>

---

## 项目简介

「十日谈」是一款帮助用户通过十天周期系统进行自我管理的Android应用。将一年划分为36个周期，每个周期专注3个目标，每天记录最多6项任务，通过可视化统计帮助用户追踪时间分配与任务完成情况。

> 核心理念：将大目标拆解为小周期，通过持续记录与反思，实现自我提升。

---

## 功能特性

### 📅 年度总览页
- 36个周期一目了然
- 支持点击周期快速跳转
- 直观展示年度规划进度

### 📊 执行进度页
- 可视化展示每个周期的执行情况
- 任务完成率统计
- 支持周期切换与历史回顾

### ✅ 今日任务页
- 每天最多记录6项任务
- 显示任务详细信息（任务名称、任务细节、任务时长）
- 一键勾选完成，支持任务编辑与删除
- 显示当前周期天数与目标

### 📈 统计分析页
- 智能统计任务出现频率
- 自动累计任务时长（支持"h"、"min"等格式）
- 按频率和时长排序，帮助发现时间分配规律

### ⚙️ 设置与备份
- 数据导出为Base64编码的JSON文件
- 支持从备份文件完整恢复数据
- 支持清除所有数据
- 自动适配Android存储权限

---

## 技术架构

### 技术栈
- **语言**: Kotlin
- **UI框架**: Jetpack Compose + Material Design 3
- **架构**: MVVM + Repository Pattern
- **依赖注入**: Hilt
- **本地存储**: Room Database
- **序列化**: Kotlinx Serialization
- **导航**: Compose Navigation
- **异步处理**: Kotlin Coroutines + Flow

### 数据模型
- **CycleEntity**: 周期实体（包含3个目标）
- **DayRecordEntity**: 每日记录（包含最多6项任务）

---

## 项目结构

```
app/src/main/java/com/example/tendaysplan/
├── data/                           # 数据层
│   ├── dao/                       # Room数据访问对象
│   │   ├── CycleDao.kt
│   │   └── DayRecordDao.kt
│   ├── model/                     # 数据模型
│   │   ├── CycleEntity.kt
│   │   └── DayRecordEntity.kt
│   ├── repository/                # 数据仓库
│   │   ├── CycleRepository.kt
│   │   └── DayRecordRepository.kt
│   ├── TenDaysPlanDatabase.kt     # Room数据库
│   └── Converters.kt              # 类型转换器
├── di/                            # 依赖注入模块
│   └── AppModule.kt
├── ui/                            # UI层
│   ├── screens/                   # 页面
│   │   ├── YearOverviewScreen.kt
│   │   ├── ProgressScreen.kt
│   │   ├── TodayScreen.kt
│   │   ├── StatisticsScreen.kt
│   │   └── SettingsScreen.kt
│   ├── viewmodel/                 # 视图模型
│   │   ├── YearOverviewViewModel.kt
│   │   ├── ProgressViewModel.kt
│   │   ├── TodayViewModel.kt
│   │   ├── StatisticsViewModel.kt
│   │   └── SettingsViewModel.kt
│   └── theme/                     # 主题配置
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
├── utils/                         # 工具类
│   ├── DateUtils.kt              # 日期工具
│   ├── StatisticsAnalyzer.kt     # 统计分析
│   └── DataExportHelper.kt       # 数据导入导出
├── MainActivity.kt               # 主Activity
└── TenDaysPlanApplication.kt     # Application类
```

---

## 系统要求

- **最低SDK**: Android 14 (API 34)
- **目标SDK**: Android 16 (API 36)
- **编译SDK**: 36

---

## 安装与运行

### 前置要求
- Android Studio Hedgehog | 2023.1.1 或更高版本
- JDK 11 或更高版本
- Android SDK 34 或更高版本

### 构建步骤

1. **克隆仓库**
```bash
git clone https://github.com/your-username/TenDaysPlan.git
cd TenDaysPlan
```

2. **打开项目**
- 使用 Android Studio 打开项目根目录

3. **同步Gradle**
- 等待 Gradle 自动同步依赖

4. **运行应用**
- 连接Android设备或启动模拟器
- 点击 Run 按钮或执行: `./gradlew installDebug`

---

## 数据说明

### 周期划分
- 一年划分为 **36个周期**
- 每个周期固定 **10天**
- 每个周期可设置 **最多3个目标**

### 任务记录
- 每天最多记录 **6项任务**
- 每项任务包含：
  - 任务名称（必填）
  - 任务细节（可选）
  - 任务时长（可选，支持格式：`2小时`、`30分钟`、`1.5h`等）

### 数据导入导出
- 导出格式：Base64编码的JSON文本文件
- 存储位置：设备Documents目录
- 文件命名：`tendaysplan_backup_[时间戳].txt`

---

## 贡献指南

欢迎提交Issue和Pull Request！

1. Fork本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启Pull Request

---

## 开源协议

本项目采用 MIT 协议 - 查看 [LICENSE](LICENSE) 文件了解详情

---

## 作者

TenDaysPlan - 让时间管理变得简单高效

---

## 路线图

- [ ] 支持自定义周期时长
- [ ] 添加桌面小组件
- [ ] 支持深色模式切换
- [ ] 添加任务提醒功能
- [ ] 支持数据云端同步
- [ ] 添加数据可视化图表
- [ ] 支持多语言

---

<div align="center">

**如果这个项目对你有帮助，请给个 ⭐️ Star 支持一下！**

</div>
