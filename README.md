# QuickLedger 极速记账

> 3秒完成记账的 Android 原生应用

## 核心理念

用户消费后无需打开 App，通过桌面小组件选择分类 → 输入金额 → 自动保存，全程 ≤3 秒。

## 技术栈

| 类别 | 技术 |
|------|------|
| 语言 | Kotlin |
| UI | Jetpack Compose + Material 3 |
| 架构 | MVVM（Presentation / Domain / Data 三层） |
| 依赖注入 | Hilt |
| 数据库 | Room |
| 异步 | Coroutines + Flow |
| 桌面小组件 | AppWidget + RemoteViews |
| 最低 SDK | Android 8.0 (API 26) |

## 功能

- **快捷记账** — 点击分类 → 输入金额 + 备注 → 回车自动保存
- **桌面小组件** — 无需打开 App，桌面直接记账
- **首页仪表盘** — 周期收支汇总 + 实时支出占比饼图 + 最近账单
- **账单管理** — 搜索、筛选、编辑、单选/批量删除
- **分类管理** — 支出/收入分类的增删改、图标与颜色自定义
- **自定义周期** — 每月起始日 1~28 天可配
- **深色模式** — 浅色 / 深色 / 跟随系统
- **数据导出** — 一键复制账单 CSV 到剪贴板
- **预算管理** — 总预算 / 分类预算（P1）

## 项目结构

```
app/src/main/java/com/quickledger/app/
├── data/local/          # Room 数据库、DAO、Entity
├── data/repository/     # Repository 实现
├── domain/model/        # 领域模型
├── domain/repository/   # Repository 接口
├── domain/usecase/      # 业务用例
├── presentation/        # MVVM 表现层
│   ├── home/            # 首页
│   ├── bills/           # 账单
│   ├── profile/         # 我的
│   ├── navigation/      # 底部导航
│   └── theme/           # Material 3 主题
├── di/                  # Hilt 依赖注入模块
└── widget/              # 桌面小组件
```

## 构建

Android Studio Hedgehog (2023.1+) 打开项目目录，Gradle Sync 后：

```bash
./gradlew assembleDebug
```

APK 输出路径：`app/build/outputs/apk/debug/app-debug.apk`

## 版本

v2.0.0

## 所有者

zm
