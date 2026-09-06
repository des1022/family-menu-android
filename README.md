# 家庭菜单（Android）

> 让家庭吃得更好，生活更有味

纯本地、零联网的家庭点菜/菜单 App（Kotlin + Jetpack Compose + Room）。
三主 Tab：**首页（菜品库）· 日历（历史菜单）· 我的（管理）**，另有：今日点单清单、分享海报、饮食统计等。
所有数据（菜品、图片、点单记录）只存本机，卸载即清。

---

## 一、功能全景（阶段 P0/P1/P2 已全部完成）

| 模块 | 能力 |
| --- | --- |
| 菜品 | 上传/拍照 · 方形裁切压缩(≤200KB) · 分类 · 价格 · 食材 · 多维标签 · 上/下架 · 编辑/删除 |
| 批量管理 | 长按或按钮进入多选：批量上架 / 下架 / 改分类 / 删除（连图与历史记录） |
| 首页 | 搜索 · 分类 Tab · 常吃 ⭐ 分组 · 标签组合筛选 · 按时间/常点排序 · 列表/两列网格切换 |
| 点单 | 卡片 ±份数 · 悬浮球实时合计 · 清单页（备注/删除/清空） · 确认点单后锁定进日历（清空不误删） · 一键生成食材买菜清单（复制导出） |
| 日历 | 月视图（当日高亮/有记录徽标） · 历史详情（份数/备注/时间） · 一键复用到今日 / 单菜复制 |
| 海报 | 生成分享海报：温馨/简约/节日/可爱 四模板 · 标题与底部文案可编辑 · 保存相册(Android10+) / 系统分享 |
| 统计 | 本周/本月：最爱排行(条形) · 分类占比(圆环) · 点单天数/总份数 · 搭配小建议 |
| 我的 | 昵称设置 · 上传/管理/分类入口 · 外观（跟随系统/浅/深） · 使用说明 |

## 二、安装与更新

- **下载**（公开仓库 Release 附件，无需登录，手机浏览器直开）：
  `https://github.com/des1022/family-menu-android/releases/download/v0.1-alpha/family-menu-v0.1-alpha.apk`
- 版本间**同签名**（仓库内置固定 `debug.keystore`），升级直接覆盖安装；数据库带平滑迁移，历史数据保留。
- 首次安装需允许「未知来源」。
- 线上包为 Debug 构建（自用分发）；如需上架商店需换正式签名（见「发布说明」）。

## 三、技术栈

| 组件 | 版本 |
| --- | --- |
| Kotlin（`org.jetbrains.kotlin.plugin.compose`） | 2.0.21 |
| KSP | 2.0.21-1.0.28 |
| Compose BOM | 2025.05.00（Compose 1.8.1） |
| Material3 + material-icons-extended | BOM 同管 |
| Room | 2.6.1（含迁移链 v1→v2→v3） |
| DataStore Preferences | 1.1.x |
| Coil | 2.7.0 |
| Navigation Compose | 2.8.x |
| AGP / Gradle | 8.7.3 / 8.9 |
| minSdk / target | 26（Android 8.0+）/ 35 |

## 四、目录结构

```
family-menu-android/
├── app/src/main/java/com/family/menu/
│   ├── ui/
│   │   ├── theme/      # 浅/深两套暖色主题、圆角、字体
│   │   ├── components/ # 通用组件（按钮/弹窗/空态/标题栏/底部导航…）
│   │   ├── screen/     # 全部页面（Home/Order/Calendar/Stats/Manage…）
│   │   └── navigation/ # 路由表
│   ├── viewmodel/      # 状态管理（VM + 手写工厂）
│   ├── data/
│   │   ├── local/      # Room：dishes/categories/records 实体与 DAO
│   │   ├── repository/ # 数据仓库 + DataStore 设置
│   │   └── model/      # 跨层模型（OrderLine 等）
│   └── util/           # ImageStore（图片裁剪压缩）、PosterGenerator、格式化
├── app/src/main/res/   # 颜色/图标/file_paths（相机、分享）
├── debug.keystore      # 固定 debug 签名（保证可覆盖安装升级）
└── .github/workflows/build-apk.yml
```

## 五、数据库（family_menu.db，当前 v3）

| 表 | 关键字段 | 说明 |
| --- | --- | --- |
| dishes | category/price/status/favorite/**ingredients**/**tags**/imagePath | 菜品；favorite=常吃 |
| categories | name/sort | 默认种子：热菜/主食/汤品 |
| records | date/dishId/num/remark/**confirmed** | 点单记录；(date,dishId) 唯一、同日自动累加 |

迁移历史（全部平滑、保留数据）：
- v1→v2：dishes 加 `favorite`；records 加 `confirmed`（确认点单锁定，清空不误删）
- v2→v3：dishes 加 `ingredients`（食材）、`tags`（标签）

## 六、云端出包（无需本地 Android 环境）

推 `main` 即触发 GitHub Actions 编译 Debug APK。**本机不装任何 Android 工具链。**

### 本机推送（网络）
```bash
git -c http.proxy=http://127.0.0.1:7897 -c https.proxy=http://127.0.0.1:7897 push <remote> main
```
直连 github.com 超时；环境自带 `58208` 代理对 git 返回 502，**只有 `127.0.0.1:7897` 可用**。

### 产物
- Actions Artifacts：`family-menu-debug-apk`（需登录）
- **Release 附件**：公开仓库可匿名直下（手机装这版）

### 发新版流程
1. 本地提交推送 → Actions 编译（约 4~5 分钟）
2. 下载 `family-menu-debug-apk` 的 `app-debug.apk`
3. 替换 Release 同名资产：先 `DELETE /releases/{id}/assets/{assetId}` 再同名上传
   - assetId 需查 `GET /releases/{id}/assets`（**≠** artifact id）
   - 注意：同名覆盖后匿名下载 URL 有 ~1 分钟 CDN 延迟，隔 60s 再 md5 复核

## 七、发布说明

- 分发链接 `v0.1-alpha` 固定，内容随构建持续替换；对外正式版建议换 tag（如 v1.0）并写清楚 change log。
- Debug 包**未开启混淆**、体积较大（约 19MB）；正式发布建议：
  - 配置 release 签名（正式 keystore，妥善保管）并上传至 GitHub Secrets
  - `release` 构建 + R8 混淆，包体可明显减小
- 纯本地数据：无账号、无云同步；如需多设备同步需另行设计（小程序端可参考 `family-order` 项目思路）。

## 八、调试与常见坑（给后续开发者）

- 工程刻意**零外部 DI**：依赖通过 `FamilyMenuApp.container` 手工装配 + 工厂创建 VM。
- VM 写操作函数一律命名 `updateXxx`：`var x by mutableStateOf + private set` 会生成 JVM setter，
  与 `fun setX()` 同名同签名冲突（Platform declaration clash）。
- `Modifier.weight` 是 `RowScope/ColumnScope` 成员扩展：抽出的组件要么声明成 `fun RowScope.X()`，要么在调用处传入布局 Modifier。
- `inJustDecodeBounds=true` 时 `BitmapFactory.decodeStream` 恒返回 null（正常），勿用其返回值判成功。
- Room DAO 内不要写带方法体的默认实现（KSP2 会崩 `unexpected jvm signature`），合并逻辑放 Repository。
- 文件编辑/提交偶发「写盘视图不一致」：以 `git cat-file -p HEAD:<file>` + 远端 raw 为权威，push 后用 `git fetch` 核 `origin/main`。
- 本机 grep 不支持 `\|`、`\b`（静默返回空）——检查一律用 `grep -E`/`grep -F` 或 Python。

## 九、Roadmap（可选增强）

- 分类真·长按拖拽排序（当前为 ↑↓）
- 骨架屏 / 列表加载动画
- 分享海报字号/排版更多模板、海报落款自定义
- 多设备/家庭共享（需服务端方案）
