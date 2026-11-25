package com.aichatstreamdemoxml.http

import android.os.Handler
import android.os.Looper
import okhttp3.Call

/**
 * 模拟请求：不发起网络请求，按脚本逐块回放流式数据。
 *
 * 数据同样经过 [com.aichatstreamdemoxml.util.AiStreamOutHelper]，
 * 这样可以完整走一遍状态切换、打字机、Markdown 和 CardView 的逻辑，仅用于调试演示。
 */
class AiMockHttpRequest : AiHttpRequest() {

    private val handler = Handler(Looper.getMainLooper())

    private var playing = false

    override fun isRequesting(): Boolean = playing

    override fun cancel() {
        playing = false
        handler.removeCallbacksAndMessages(null)
    }

    override fun onRequest(question: String): Call? {
        val script = AiMockScripts.match(question) ?: return null
        play(script)
        return null
    }

    private fun play(script: MockScript) {
        cancel()
        playing = true

        val steps = mutableListOf<MockStep>()
        script.reasoning.chunked(script.chunkSize).forEach {
            steps.add(MockStep(script.thinkDelay, reasoningDto(it)))
        }
        script.reply.chunked(script.chunkSize).forEach {
            steps.add(MockStep(script.replyDelay, replyDto(it)))
        }

        var index = 0
        val runnable = object : Runnable {
            override fun run() {
                if (!playing) return
                if (index >= steps.size) {
                    playing = false
                    onComplete?.invoke(script.question)
                    return
                }
                val step = steps[index++]
                onNext?.invoke(script.question, true, step.dto)
                handler.postDelayed(this, step.delay)
            }
        }
        // 首次数据稍作延迟，模拟真实请求的首包耗时
        handler.postDelayed(runnable, 400)
    }

    private fun reasoningDto(content: String) = AiResponseDto(
        choices = mutableListOf(AiResponseDto.Choice(delta = AiResponseDto.Delta(reasoning_content = content)))
    )

    private fun replyDto(content: String) = AiResponseDto(
        choices = mutableListOf(AiResponseDto.Choice(delta = AiResponseDto.Delta(content = content)))
    )

    private class MockStep(val delay: Long, val dto: AiResponseDto)
}

/**
 * 一条模拟问答脚本
 */
data class MockScript(
    val question: String,
    val reasoning: String,
    val reply: String,
    /** 输入中包含任一关键词时也会命中该脚本 */
    val keywords: List<String> = emptyList(),
    val chunkSize: Int = 4,
    val thinkDelay: Long = 70L,
    val replyDelay: Long = 60L
)

/**
 * 模拟数据源：一批问答脚本，覆盖普通文本、Markdown、表格和 CardView 四种形态
 */
object AiMockScripts {

    val batch: List<MockScript> = listOf(
        MockScript(
            question = "你好",
            keywords = listOf("你好", "您好", "hi", "hello", "嗨"),
            reasoning = "用户只是在打招呼，先给出简短友好的回应，再说明这个 Demo 能做什么。",
            reply = """
                你好！这里是 AiChatStreamDemoXml 的模拟输出。

                当前演示包含：
                1. 流式返回 + 打字机效果
                2. Markdown 渲染
                3. 思考过程与正文分离

                > 以上内容由本地 Mock 数据生成，不会发起真实网络请求。
            """.trimIndent()
        ),
        MockScript(
            question = "介绍一下这个项目",
            keywords = listOf("介绍", "项目", "是什么"),
            reasoning = "从技术栈、渲染方式和交互三方面概括，控制篇幅。",
            reply = """
                ## 项目简介

                - UI：XML + DataBinding，没有使用 Compose
                - 网络：OkHttp 读取流式响应，逐行解析 SSE
                - 渲染：Markwon 支持表格与代码块

                关键入口是 AiStreamOutHelper，它负责把数据块转换成打字机输出。
            """.trimIndent()
        ),
        MockScript(
            question = "用表格对比 XML 和 Compose",
            keywords = listOf("表格", "对比", "xml", "compose"),
            reasoning = "用户要对比表格，列三个维度就够了：声明方式、数据绑定、本项目是否使用。",
            reply = """
                | 对比项 | XML + DataBinding | Jetpack Compose |
                | --- | --- | --- |
                | 声明方式 | 标签布局 | Kotlin 函数 |
                | 数据驱动 | Binding 对象 | State |
                | 本项目 | 已采用 | 未启用 |

                结论：本项目坚持 XML 方案，兼顾老项目改造和多端复用。
            """.trimIndent(),
            chunkSize = 6
        ),
        MockScript(
            question = "生成一条卡片消息",
            keywords = listOf("卡片", "card"),
            reasoning = "这条回复用 CardView 标签包裹，适配器识别后会自动切换成卡片布局。",
            reply = "<CardView>这是一条卡片消息：AiChatStreamDemoXml 会根据内容标签切换 item 类型。</CardView>",
            chunkSize = 8
        )
    )

    /**
     * 先按完整问题精确匹配，再按关键词模糊匹配
     */
    fun match(input: String): MockScript? {
        val text = input.trim()
        if (text.isEmpty()) return null
        return batch.firstOrNull { it.question == text }
            ?: batch.firstOrNull { script -> script.keywords.any { text.contains(it, ignoreCase = true) } }
    }
}
