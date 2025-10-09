<h1 align="center">
<img src="https://raw.githubusercontent.com/WhiteVermouth/intellij-investor-dashboard/master/screenshots/Logo.png" width="100" alt="icon"><br>
Stocker
</h1>

<p align="center">
<b>专为程序员投资者打造的 JetBrains IDE 股票行情仪表盘插件</b>
</p>

<p align="center">
<img src="https://dev.azure.com/nszihan/Stocker/_apis/build/status%2FWhiteVermouth.intellij-investor-dashboard?branchName=master" alt="Build Status" />
<img src="https://img.shields.io/github/v/release/WhiteVermouth/intellij-investor-dashboard" alt="GitHub Release" />
<img src="https://img.shields.io/jetbrains/plugin/v/com.vermouthx.intellij-investor-dashboard" alt="Marketplace Release" />
<img src="https://img.shields.io/jetbrains/plugin/d/com.vermouthx.intellij-investor-dashboard" alt="Marketplace Downloads" />
</p>

<p align="center">
<img src="https://raw.githubusercontent.com/WhiteVermouth/intellij-investor-dashboard/master/screenshots/Dashboard.png" alt="Dashboard"/>
</p>

## 🎯 项目简介

Stocker 是一款强大的 IntelliJ IDEA / JetBrains 全家桶插件，专为需要在编码时关注股票市场的开发者设计。无需离开IDE，即可实时监控您的股票投资组合，让您在专注开发的同时轻松掌握市场动态。

### 为什么选择 Stocker？

- 🚀 **无缝集成**：直接在 IDE 中查看行情，不干扰编码工作流
- 💰 **投资组合管理**：跟踪持仓、成本、盈亏，一目了然
- 🌍 **多市场支持**：A股、港股、美股，全球市场尽在掌握
- 🎨 **灵活配置**：自定义刷新频率、颜色方案、数据源
- ⚡ **性能优化**：低资源占用，不影响 IDE 性能
- 📊 **智能排序**：强大的表格排序功能，快速找到关键信息

---

## ✨ 核心功能

### 📈 实时行情监控

- **多市场支持**：A股（沪深）、港股、美股
- **实时刷新**：可配置刷新间隔（5秒-60秒）
- **市场指数**：上证指数、深证成指、创业板指、恒生指数、道琼斯、纳斯达克等
- **详细数据**：当前价、开盘价、最高价、最低价、涨跌幅等

### 💼 投资组合管理

- **持仓管理**：记录成本价和持仓数量
- **盈亏计算**：自动计算总盈亏和日盈亏
- **持仓市值**：实时显示总持仓市值
- **盈亏汇总**：汇总行显示所有持仓的总体情况
- **数据持久化**：成本价和持仓数量自动保存

### 🔧 高级功能

#### 智能表格排序
- **点击表头排序**：支持所有列的升序/降序排序
- **0值智能排序**：0值自动排在最后，突出有价值的数据
- **汇总行固定**：汇总行始终在第一行，不受排序影响
- **数字/文本识别**：自动识别列类型，选择最佳排序方式
- **排序指示器**：▲▼箭头清晰显示当前排序状态

#### 批量股票管理
- **手动添加**：支持搜索功能，快速查找并添加股票
- **批量添加**：一键添加成交量TOP10热门股票
- **实时验证**：添加前自动验证股票代码有效性
- **智能去重**：自动过滤重复股票

#### 视觉定制
- **涨跌颜色**：
  - 红涨绿跌（中国习惯）
  - 绿涨红跌（欧美习惯）
  - 无颜色模式
- **盈亏颜色**：汇总行的盈亏数据也支持红绿标识
- **表格样式**：清晰的表头、居中对齐、合理的间距

### 🔄 数据源支持

- **新浪财经**：数据更新快，延迟低
- **腾讯财经**：数据稳定可靠
- **一键切换**：在设置中轻松切换数据源

---

## 📦 安装方法

### 方式一：从 JetBrains Marketplace 安装（推荐）

1. 打开 IntelliJ IDEA
2. 进入 `Settings/Preferences` → `Plugins`
3. 点击 `Marketplace` 标签
4. 搜索 `Stocker`
5. 点击 `Install` 安装
6. 重启 IDE

![Install](https://raw.githubusercontent.com/WhiteVermouth/intellij-investor-dashboard/master/screenshots/Install.png)

### 方式二：手动安装

1. 从 [Releases](https://github.com/WhiteVermouth/intellij-investor-dashboard/releases) 下载最新版本的 `.zip` 文件
2. 打开 IntelliJ IDEA
3. 进入 `Settings/Preferences` → `Plugins`
4. 点击齿轮图标 ⚙️ → `Install Plugin from Disk...`
5. 选择下载的 `.zip` 文件
6. 重启 IDE

### 系统要求

- **IntelliJ IDEA**: 2020.3 及以上版本
- **支持的IDE**：IntelliJ IDEA、PyCharm、WebStorm、PhpStorm、GoLand 等所有 JetBrains IDE
- **Java 版本**：JDK 11 及以上

---

## 🚀 快速开始

### 1. 打开工具窗口

安装后，在 IDE 右侧或底部工具栏找到 `Stocker` 按钮，点击打开工具窗口。

### 2. 添加股票

#### 方式一：手动搜索添加
1. 点击工具栏的 **齿轮图标** → `Manage Favorite Stocks`
2. 选择市场标签（CN/HK/US）
3. 点击 `+` 按钮
4. 选择 `手动添加股票`
5. 在搜索框输入股票代码或名称
6. 点击 `Add` 添加到收藏列表

#### 方式二：批量添加TOP10
1. 点击工具栏的 **齿轮图标** → `Manage Favorite Stocks`
2. 选择市场标签（CN/HK/US）
3. 点击 `+` 按钮
4. 选择 `批量添加成交量TOP10`
5. 等待加载完成
6. 查看添加结果

### 3. 编辑持仓信息

1. 双击 `Cost Price` 列，输入您的成本价
2. 双击 `Quantity` 列，输入您的持仓数量
3. 盈亏数据会自动计算并显示

### 4. 查看汇总

表格第一行显示汇总数据：
- **名称列**：显示 "Total"
- **持仓数量列**：显示总持仓市值
- **日盈亏列**：显示今日总盈亏（带颜色标识）
- **总盈亏列**：显示总盈亏（带颜色标识）

### 5. 排序功能

- **点击任意列头**：进行降序排序
- **再次点击**：切换为升序
- **继续点击**：在升序/降序间切换
- **0值处理**：数字列的0值自动排在最后

---

## ⚙️ 配置选项

### 数据源设置

1. 打开工具窗口
2. 点击工具栏的 **齿轮图标** → `Settings`
3. `Quote Provider`：选择数据提供商
   - **Sina**（新浪财经）：更新快
   - **Tencent**（腾讯财经）：稳定可靠

### 颜色方案

在设置中选择 `Quote Color Pattern`：
- **R.U.G.D. Mode**：红涨绿跌（中国习惯）
- **G.U.R.D. Mode**：绿涨红跌（欧美习惯）
- **None**：无颜色

### 刷新间隔

在设置中调整 `Refresh Interval`：
- **推荐值**：10-30秒
- **最小值**：5秒（实时性高，但API压力大）
- **最大值**：60秒（节省资源）

⚠️ **注意**：修改刷新间隔后，点击"刷新"按钮即可生效，无需重启IDE。

---

## 📊 功能详解

### 表格列说明

| 列名 | 说明 | 可编辑 | 数据来源 |
|------|------|--------|----------|
| Symbol | 股票代码 | ❌ | 用户添加 |
| Name | 股票名称 | ❌ | API实时数据 |
| Current | 当前价格 | ❌ | API实时数据 |
| Change% | 涨跌幅 | ❌ | API实时数据 |
| Cost Price | 成本价 | ✅ | 用户输入 |
| Quantity | 持仓数量 | ✅ | 用户输入 |
| Day P&L | 日盈亏 | ❌ | 自动计算 |
| P&L | 总盈亏 | ❌ | 自动计算 |

### 计算公式

```
日盈亏 = 涨跌额 × 持仓数量
总盈亏 = (当前价 - 成本价) × 持仓数量
总持仓市值 = Σ(当前价 × 持仓数量)
```

### 工具栏按钮

- **刷新** 🔄：立即刷新行情数据
- **停止** ⏸️：暂停自动刷新
- **管理** ⚙️：管理收藏股票列表
- **设置** 🔧：打开设置界面

---

## 🏗️ 技术架构

### 核心技术栈

- **语言**：Kotlin + Java
- **框架**：IntelliJ Platform SDK
- **UI**：Swing
- **网络**：Apache HttpClient
- **并发**：ScheduledExecutorService
- **架构模式**：发布-订阅模式（Message Bus）

### 项目结构

```
intellij-investor-dashboard/
├── src/main/
│   ├── kotlin/com/vermouthx/stocker/
│   │   ├── actions/              # 用户动作（刷新、停止等）
│   │   ├── activities/           # 启动活动
│   │   ├── entities/             # 数据实体（StockerQuote等）
│   │   ├── enums/                # 枚举类型（市场、提供商等）
│   │   ├── listeners/            # 事件监听器
│   │   ├── notifications/        # 通知管理
│   │   ├── settings/             # 设置管理
│   │   ├── utils/                # 工具类（HTTP、解析等）
│   │   ├── views/                # UI视图和对话框
│   │   ├── StockerApp.kt         # 应用核心类
│   │   └── StockerAppManager.kt  # 应用管理器
│   └── java/com/vermouthx/stocker/
│       ├── components/           # 表格组件
│       ├── listeners/            # Java监听器
│       ├── utils/                # Java工具类
│       └── views/                # Java视图
├── src/main/resources/
│   ├── META-INF/
│   │   ├── plugin.xml            # 插件配置
│   │   └── pluginIcon.svg        # 插件图标
│   └── icons/                    # 图标资源
└── build.gradle.kts              # Gradle构建配置
```

### 核心组件

#### 1. **StockerApp** - 应用核心
- 管理定时刷新任务
- 调度HTTP请求
- 发布数据更新事件

#### 2. **StockerQuoteParser** - 数据解析
- 解析新浪/腾讯API响应
- 处理不同市场的数据格式
- 边界检查和异常处理

#### 3. **StockerTableView** - 表格视图
- 显示股票行情
- 计算盈亏
- 管理汇总行

#### 4. **StockerTableSorter** - 智能排序
- 识别数字/文本类型
- 0值特殊处理
- 汇总行固定

#### 5. **Message Bus** - 事件通信
- 解耦数据获取和UI更新
- 支持多窗口同步
- 发布-订阅模式

### 数据流

```
定时任务调度
    ↓
HTTP请求行情数据
    ↓
解析响应文本
    ↓
发布消息总线事件
    ↓
监听器接收事件
    ↓
更新表格显示
    ↓
计算盈亏
    ↓
刷新汇总行
```

---

## 🔧 开发指南

### 环境准备

```bash
# 要求
- JDK 11+
- Gradle 7.0+
- IntelliJ IDEA 2020.3+
```

### 构建项目

```bash
# 克隆项目
git clone https://github.com/WhiteVermouth/intellij-investor-dashboard.git

# 进入项目目录
cd intellij-investor-dashboard

# 构建插件
./gradlew buildPlugin

# 运行测试
./gradlew test

# 启动调试IDE
./gradlew runIde
```

### 项目配置

编辑 `gradle.properties` 调整构建配置：

```properties
# IntelliJ Platform 版本
platformVersion=2020.3

# Kotlin 版本
kotlinVersion=1.9.0
```

### 代码结构

#### 添加新功能

1. **新增实体类**：在 `entities/` 目录
2. **新增工具类**：在 `utils/` 目录
3. **新增UI组件**：在 `views/` 或 `components/` 目录
4. **新增监听器**：在 `listeners/` 目录
5. **注册插件扩展**：在 `plugin.xml` 中配置

#### 代码规范

- ✅ 所有公共类和方法都有详细的中文注释
- ✅ 使用Kotlin优先（工具类、业务逻辑）
- ✅ Java用于UI组件（利用Swing）
- ✅ 异常必须处理（不能让异常传播到UI层）
- ✅ 线程安全（UI操作必须在EDT线程）

---

## 🐛 已知问题和解决方案

### 常见问题

#### 1. 数据不刷新
**解决方案**：
- 检查网络连接
- 点击"刷新"按钮手动刷新
- 检查数据提供商API是否可访问
- 切换数据源（新浪↔腾讯）

#### 2. 股票代码无效
**解决方案**：
- A股代码格式：`sh600000`（沪市）或 `sz000001`（深市）
- 港股代码格式：`00700`（不含hk前缀）
- 美股代码格式：`AAPL`（大写）

#### 3. 成本价/持仓数量无法编辑
**解决方案**：
- 双击单元格进入编辑模式
- 确保不是在汇总行编辑
- 输入数字后按回车确认

---

## 🎨 功能亮点

### 🔥 最新优化（2025版）

#### 性能优化
- ✅ 消除重复HTTP请求（减少75%网络请求）
- ✅ 优化线程池配置（从4线程降至1线程）
- ✅ 支持动态调整刷新间隔（无需重启）
- ✅ HTTP连接池复用（提升并发性能）

#### 稳定性提升
- ✅ 完善的异常处理（网络超时、解析错误等）
- ✅ 所有数组访问前的边界检查
- ✅ 所有字符串操作前的索引验证
- ✅ 线程安全的数据结构（ConcurrentHashMap）
- ✅ 优雅的线程池关闭机制

#### 代码质量
- ✅ 消除300+行重复代码
- ✅ 统一的错误处理逻辑
- ✅ 详细的中文注释（覆盖率90%+）
- ✅ 清晰的代码结构和命名

---

## 📖 使用技巧

### 技巧1：快速查看盈亏TOP股票

1. 点击 `P&L` 列表头
2. 自动按总盈亏降序排序
3. 盈亏最高的股票排在前面
4. 0值股票自动排在最后

### 技巧2：关注高涨幅股票

1. 点击 `Change%` 列表头
2. 降序排序查看涨幅榜
3. 升序排序查看跌幅榜

### 技巧3：批量建仓

1. 使用"批量添加成交量TOP10"功能
2. 快速获取当前市场最活跃的股票
3. 适合新手快速了解市场热点

### 技巧4：多项目支持

- 每个IntelliJ项目窗口都有独立的Stocker实例
- 可以为不同项目配置不同的股票列表
- 数据自动隔离，互不干扰

---

## 🤝 贡献指南

欢迎贡献代码！请遵循以下步骤：

1. **Fork** 本仓库
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 **Pull Request**

### 开发建议

- 📝 添加详细的中文注释
- ✅ 确保所有测试通过
- 🐛 修复现有的linter警告
- 📖 更新相关文档
- 🎨 遵循现有代码风格

---

## 📝 更新日志

### v2.0.0 (2025-10-09)

#### 🎉 新增功能
- ✨ 表格列排序（升序/降序）
- ✨ 数字列0值智能排序
- ✨ 汇总行固定在第一行
- ✨ 汇总行显示总持仓市值
- ✨ 汇总行盈亏红绿色标识
- ✨ 批量添加成交量TOP10股票

#### 🚀 性能优化
- ⚡ 消除重复HTTP请求（减少75%）
- ⚡ 优化线程池（4→1线程）
- ⚡ 支持动态刷新间隔
- ⚡ 线程池优雅关闭

#### 🐛 Bug修复
- 🔧 修复StringIndexOutOfBoundsException
- 🔧 修复NullPointerException（排序相关）
- 🔧 修复汇总行重复显示
- 🔧 修复百分号解析错误

#### 📚 代码改进
- 📖 添加详细中文注释（覆盖率90%+）
- 🧹 消除300+行重复代码
- 🛡️ 完善异常处理
- 🔐 提升线程安全性

---

## 📚 教程和文档

- **官方教程**：[https://vermouthx.com/2021/04/11/stocker](https://vermouthx.com/2021/04/11/stocker)
- **API文档**：查看代码中的详细注释
- **问题反馈**：[GitHub Issues](https://github.com/WhiteVermouth/intellij-investor-dashboard/issues)

---

## 🌟 项目贡献者

本项目由两代开发者接力打造，持续为程序员投资者提供优质体验！

### 👨‍💻 初代作者 - VermouthX

<table>
<tr>
<td width="80px">
<img src="https://github.com/WhiteVermouth.png" width="60" height="60" style="border-radius: 50%;" alt="VermouthX"/>
</td>
<td>
<b>核心贡献</b>：
<ul>
<li>🎯 创建项目并开源</li>
<li>🏗️ 设计核心架构和数据流</li>
<li>📡 实现多市场行情数据获取</li>
<li>🎨 设计UI界面和交互逻辑</li>
<li>📦 发布到JetBrains Marketplace</li>
</ul>
</td>
</tr>
</table>

**联系方式**：
- 🔗 GitHub：[@WhiteVermouth](https://github.com/WhiteVermouth)
- 📝 博客：[https://vermouthx.com](https://vermouthx.com)
- 💼 原始项目：[intellij-investor-dashboard](https://github.com/WhiteVermouth/intellij-investor-dashboard)

---

### 👨‍💻 二代作者 - Chimaeras

<table>
<tr>
<td width="80px">
<img src="https://github.com/Chimaeras.png" width="60" height="60" style="border-radius: 50%;" alt="Chimaeras"/>
</td>
<td>
<b>核心贡献</b>：
<ul>
<li>🐛 修复关键Bug（异常处理、排序问题等）</li>
<li>✨ 新增智能表格排序功能</li>
<li>🎯 新增批量添加TOP10热门股票</li>
<li>⚡ 性能优化（减少75%网络请求）</li>
<li>📖 添加详细中文注释（覆盖率90%+）</li>
<li>🛡️ 提升代码质量和稳定性</li>
<li>🎨 优化用户体验（汇总行、0值排序等）</li>
</ul>
</td>
</tr>
</table>

**联系方式**：
- 🔗 GitHub：[@Chimaeras](https://github.com/Chimaeras)
- 📦 V2增强版：[Idea-Stocker-V2-Plugin](https://github.com/Chimaeras/Idea-Stocker-V2-Plugin)
- 💬 问题反馈：[提交Issue](https://github.com/Chimaeras/Idea-Stocker-V2-Plugin/issues)

---

## 📄 许可证

本项目采用 [Apache-2.0 License](https://raw.githubusercontent.com/WhiteVermouth/intellij-investor-dashboard/master/LICENSE)

---

## 💝 支持我们

> 如果这个插件让您的投资更轻松，节省了您的时间，您的支持将是我们持续改进的最大动力！

### 🆓 免费支持（非常感谢！）

- ⭐ **Star本项目** - 让更多开发者发现这个宝藏插件
- 📢 **分享推荐** - 告诉您的程序员朋友
- 🐛 **反馈Bug** - 帮助我们发现并修复问题
- 💡 **建议功能** - 在Issue中提出您的想法
- 📝 **好评支持** - 在JetBrains Marketplace留下五星好评
- 🎨 **贡献代码** - Pull Request让插件更强大

### ☕ 打赏支持（随心而为）

您的打赏是对开发者最直接的鼓励，每一分钱都将用于项目的持续改进！

<table>
<tr>
<td align="center" width="50%">

#### 支持初代作者 VermouthX

<a href="https://www.buymeacoffee.com/nszihan" target="_blank">
<img src="https://img.shields.io/badge/Buy%20Me%20A%20Coffee-FFDD00?style=for-the-badge&logo=buy-me-a-coffee&logoColor=black" alt="Buy Me A Coffee" height="40"/>
</a>

<p><i>🙏 感谢创建这个优秀的开源项目！</i></p>

</td>
<td align="center" width="50%">

#### 支持二代作者 Chimaeras

<p><b>微信赞赏</b></p>

<img src="./screenshots/wechat-donate-chimaeras.png" width="200" alt="微信赞赏码" onerror="this.style.display='none'"/>


<p><i>🙏 感谢持续优化、修复和维护！</i></p>

</td>
</tr>
</table>

### 🎁 其他支持方式

- 💼 **企业合作**：如需定制开发或技术支持，请通过GitHub联系
- 🤝 **成为贡献者**：长期贡献者将在README中展示
- 📢 **推广合作**：欢迎技术博主、UP主分享推广

---

## 📮 联系我们

有任何问题、建议或合作意向，欢迎通过以下方式联系：

### 初代作者 VermouthX
- 🐙 GitHub：[@WhiteVermouth](https://github.com/WhiteVermouth)
- 📝 个人博客：[vermouthx.com](https://vermouthx.com)
- 📦 原始项目：[intellij-investor-dashboard](https://github.com/WhiteVermouth/intellij-investor-dashboard)

### 二代作者 Chimaeras  
- 🐙 GitHub：[@Chimaeras](https://github.com/Chimaeras)
- 🚀 V2增强版：[Idea-Stocker-V2-Plugin](https://github.com/Chimaeras/Idea-Stocker-V2-Plugin)
- 💬 问题反馈：[提交Issue](https://github.com/Chimaeras/Idea-Stocker-V2-Plugin/issues)

---

<p align="center">
Made with ❤️ by developers, for developers
</p>

<p align="center">
<i>边写代码边炒股，程序员的快乐就是这么简单！</i>
</p>
