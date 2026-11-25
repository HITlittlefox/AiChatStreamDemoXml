# AiChatStreamDemoXml

一个基于 Android XML + Data Binding 的 AI 流式聊天演示项目。聊天消息使用 RecyclerView 展示，网络层通过 OkHttp 接收并解析 SSE 流。

## 已支持的富交互

- **流式回复与打字机效果**：逐段接收回复并实时更新界面；思考内容与回答正文分别展示。
- **可折叠思考区**：点击思考标题展开或收起内容；生成期间显示“思考中…”，完成后显示“已深度思考”并自动折叠。
- **Markdown 富文本**：支持常见 Markdown 内容、代码块和表格；表格宽度按屏幕尺寸约束。
- **卡片消息**：回答包含 `<CardView>...</CardView>` 时，切换为独立卡片样式。
- **高交互设备消息**：支持 `streamType=HighTouchInteraction`，从 `choices[].attachdata.deviceIds` 读取关联设备。每个携带新 `attachdata` 的高交互片段独立成组；打字机输出完该片段后，设备卡片紧跟在对应文本后显示。当前设备名称和在线状态为本地假数据。
- **文本选择**：提问和回答文本可以长按选择、复制。
- **新消息定位与滚动**：发送后将最新提问定位到聊天区域顶部；动态 footer 随回复内容增长而收缩，为短回复保留屏幕空间。用户手动滚动时解除自动顶部锚定。
- **发送动效**：发送时聊天列表整体向上推入新一轮对话。
- **滚动到底部入口**：离开最新内容后显示快捷滚动按钮。
- **Debug 本地模拟**：Debug 构建中，长按标题可播放一组本地问答；输入脚本关键词可触发 Markdown、表格及卡片演示。输入“查一下我的智能家居设备”测试单段高交互流；输入“复杂”会随机生成多段普通文本流与高交互设备流，并重复多轮，便于检查打字机和设备分组。不请求真实 AI 服务。

## 构建

需要 Android SDK（`compileSdk 35`）和 Java 8 兼容工具链。

```powershell
./gradlew.bat assembleDebug
```

APK 输出路径：`app/build/outputs/apk/debug/app-debug.apk`。

## 配置真实 API

高交互流片段示例：

```text
data: {"id":"demo-1","streamType":"HighTouchInteraction","choices":[{"delta":{"content":"找到以下设备，可以继续查看或控制："},"attachdata":{"deviceIds":["device_light_living_room","device_ac_bedroom"]}}]}
```

同一高交互文本段的后续分片可以继续发送 `streamType=HighTouchInteraction`；下一组设备由新的 `attachdata` 标记。客户端会在该段文字打字机输出完成后显示这一组设备。

在项目根目录的 `local.properties` 添加：

```properties
ai.api.key=YOUR_API_KEY
```

该密钥通过本地 BuildConfig 字段读取，请勿提交真实密钥。Debug 本地 Mock 演示不需要配置 API Key。
