# 家庭菜单（Android）

> 让家庭吃得更好，生活更有味

按 UI 设计稿打造的纯本地家庭菜单 App：菜品库 · 今日点单 · 菜单日历。
全部数据存本机，不联网。

---

## 一、当前状态

**阶段一 P0（基础架构）刚完成**：项目脚手架、路由、Room 数据层、主题组件、全局状态、空壳页面。
**阶段二 P0（核心功能）待做**：分类增删改、菜品上传与图片裁剪压缩、首页筛选与搜索排序、点单清单与悬浮球、菜单日历与复用、3:4 海报等。

具体任务清单见任务面板（TaskList）。

## 二、技术栈

| 组件 | 版本 |
| --- | --- |
| Kotlin（含 `org.jetbrains.kotlin.plugin.compose`） | 2.0.21 |
| KSP | 2.0.21-1.0.28 |
| Compose BOM | 2025.05.00（Compose 1.8.1） |
| Room | 2.6.1 |
| AGP | 8.7.3 |
| Gradle | 8.9 |
| minSdk | 26（Android 8.0+） |

## 三、目录结构

```
family-menu-android/
├── app/src/main/java/com/family/menu/
│   ├── ui/        # 主题、通用组件、各页面
│   ├── viewmodel/ # 状态管理
│   ├── data/      # Room 实体/DAO/Repository、DataStore
│   └── util/      # 图片处理、格式化
├── app/src/main/res/    # 颜色、字符串、图标
└── .github/workflows/build-apk.yml  # 云端 CI
```

## 四、云端出包（无需本地安装 Android 环境）

仓库已配 `.github/workflows/build-apk.yml`：推送到 `main` 即由 GitHub runner 编译出 Debug APK，
**无需本机安装 Android Studio / JDK / Android SDK**。release 下载链接见 Actions Artifacts 或 Release 附件。

### 推送
```bash
git -c http.proxy=http://127.0.0.1:7897 -c https.proxy=http://127.0.0.1:7897 push https://des1022:<TOKEN>@github.com/des1022/family-menu-android.git main
```

### 下载
- Actions Artifacts：`family-menu-debug-apk`（需登录 GitHub）
- **Release 附件**（公开仓库可匿名直下，手机浏览器直接打开链接即可）

## 五、调试

阶段一 P0 完成后启动即看到一个三 Tab（首页 / 日历 / 我的）的空壳工程。完整功能随阶段二、三逐步迭代。

## 六、本机网络

直连 github.com 会超时。环境自带 `58208` 对 git 返回 502，**只有 `127.0.0.1:7897` 可用**，所有 push / curl 都需显式加 `--proxy http://127.0.0.1:7897`。