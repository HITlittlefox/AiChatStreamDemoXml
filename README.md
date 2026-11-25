# AiChatStreamDemoXml

一个用 **XML 布局（不是 Compose）** 实现的 Android AI 流式对话 Demo：把大模型返回的 SSE 数据流，以“打字机”效果逐字渲染到 RecyclerView 中，支持 Markdown、思考过程、卡片消息等形态。

## 技术栈

| 分类 | 选型 |
| --- | --- |
| 语言 | Kotlin 1.8.21（另有少量 Java 工具类） |
| 构建 | Gradle 8.7（Groovy DSL）+ Android Gradle Plugin 8.1.2 |
| SDK | compileSdk 35 / targetSdk 35 / minSdk 23 |
| UI | **XML 布局 + DataBinding**，`buildFeatures.compose = false`，未使用 Jetpack Compose |
| 网络 | OkHttp 4.9.3 + Retrofit 2.9.0 + Gson 2.9.1 |
| 异步 | RxJava2 + RxAndroid + Uber AutoDispose |
| Markdown | Markwon（core + tables） |
| 列表/刷新 | RecyclerView + SmartRefreshLayout 2.0.5 |
| 其他 | Material BottomNavigationView 1.11.0、ImmersionBar 3.2.2、XPopup 2.10.0、archean libx 基础库 |

## 功能拆解

### 1. 流式对话（SSE）
- 请求体按 OpenAI 兼容协议构造：`model` + `messages` + `stream = true`。
- 默认走硅基流动 `https://api.siliconflow.cn/v1/chat/completions`，模型 `Pro/deepseek-ai/DeepSeek-R1`；文件中另留有 DeepSeek 官方接口的注释版本，改两行即可切换。
- 流式解析基于 OkHttp 原始流：逐行 `readUtf8Line()`，去掉 `data:` 前缀，跳过 `: keep-alive` 心跳，遇到 `finish_reason == "stop"` 结束。
- 非流式响应（`Content-Length` 有值）走另一条分支，方便对比。

### 2. 状态机 + 打字机效果
`AiStreamOutHelper` 用状态驱动 UI，状态定义在 `AiChatStateType`：

| 状态 | 含义 |
| --- | --- |
| `wait` | 等待首个数据块 |
| `think` | 正在输出思考内容（`reasoning_content`） |
| `reply_ing` | 正在输出正文 |
| `reqeust_end` | 网络请求已结束，打字机仍在补字 |
| `out_end` | 正文输出完毕 |
| `error` | 请求异常 |

回复正文会先进入 `replyBuilder`，再由 RxJava `Observable.interval(0, 32ms)` 按字符搬到 `replyOutBuilder`，形成打字机效果；请求结束与打字结束分别处理，避免“字还没打完就显示完成”。页面销毁时通过 AutoDispose 自动释放定时器。

### 3. Markdown 渲染
`AiMarkdownHelper` 封装 Markwon，启用 CorePlugin、TablePlugin（表格宽度按屏幕宽度自适应）与 MovementMethodPlugin。思考内容和回复内容都按 Markdown 渲染。

### 4. 多种消息类型
`AiChatAdapter` 根据内容判断 item 类型：正文里出现 `<CardView>...</CardView>` 时使用卡片布局（`item_chat_card_view.xml`），其余走普通文本布局（`item_main.xml`）。

### 5. 会话页交互
- 左侧抽屉（DrawerLayout）宽度为屏幕 76%，滑动时主布局联动位移。
- 发送、节流 500ms、请求中禁止重复提交；点击标题栏可取消当前请求并重置状态。
- 点击空白区域自动收起软键盘；发送按钮不触发收键盘。
- 列表滚动监听：用户上滑时暂停自动滚动并显示“回到底部”按钮，点击回到底部并恢复自动跟随。
- SmartRefreshLayout 支持下拉刷新/上拉加载的边界判断。
- ImmersionBar 实现沉浸式状态栏，并处理 Android 15 下 BottomNavigationView 的系统内边距。
- 底部导航为首页/功能/我的三个占位 tab。

## 运行

1. 在工程根目录的 `local.properties` 中添加你的密钥（该文件已被 gitignore，不会入库）：

```properties
ai.api.key=你的硅基流动APIKey
```

2. 构建/安装：

```bash
./gradlew :app:assembleDebug
```

密钥在构建时通过 `buildConfigField` 注入 `BuildConfig.AI_API_KEY`，源码中不保存明文；换机器时补上这一行即可。

## 模拟数据（调试用）

没有 API Key 也能看完整效果：**长按标题栏**会依次播放一批模拟问答（普通文本、Markdown、表格、CardView），数据会经过 `AiStreamOutHelper`，状态切换和打字机逻辑与真实请求一致。直接发送包含关键词的内容（如「你好」「介绍」「表格」「卡片」）也会命中对应脚本，不会发起真实请求。该入口仅在 Debug 构建生效，脚本与关键词在 `AiMockScripts.batch` 中维护。

## 代码位置

| 关注点 | 文件 |
| --- | --- |
| 请求构造与 SSE 解析 | [AiHttpRequest.kt](app/src/main/java/com/aichatstreamdemoxml/http/AiHttpRequest.kt) |
| 模拟请求与脚本 | [AiMockHttpRequest.kt](app/src/main/java/com/aichatstreamdemoxml/http/AiMockHttpRequest.kt) |
| 流式状态机与打字机 | [AiStreamOutHelper.kt](app/src/main/java/com/aichatstreamdemoxml/util/AiStreamOutHelper.kt) |
| 页面逻辑与交互 | [MainActivity.kt](app/src/main/java/com/aichatstreamdemoxml/MainActivity.kt) |
| 多类型列表 | [AiChatAdapter.kt](app/src/main/java/com/aichatstreamdemoxml/AiChatAdapter.kt) |
| 消息实体 | [AiChatMessageDto.kt](app/src/main/java/com/aichatstreamdemoxml/dto/AiChatMessageDto.kt) |
| Markdown 渲染 | [AiMarkdownHelper.kt](app/src/main/java/com/aichatstreamdemoxml/util/AiMarkdownHelper.kt) |
| 响应数据模型 | [AiResponseDto.kt](app/src/main/java/com/aichatstreamdemoxml/http/AiResponseDto.kt) |
| 布局 | [activity_main.xml](app/src/main/res/layout/activity_main.xml)、[item_main.xml](app/src/main/res/layout/item_main.xml)、[item_chat_card_view.xml](app/src/main/res/layout/item_chat_card_view.xml) |
