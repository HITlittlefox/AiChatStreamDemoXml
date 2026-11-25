package com.aichatstreamdemoxml.http

import com.aichatstreamdemoxml.util.AiContentType

/**
 * ai部分 返回码
 */
open class AiResponseDto(
    var choices: MutableList<Choice>? = null,
    var created: Int? = null,
    var id: String? = null,
    var streamType: StreamType = StreamType.Common,
    var model: String? = null,
    var system_fingerprint: String? = null,
) {
    fun getType(): String {
        return if (choices?.getOrNull(0)?.delta?.content.isNullOrEmpty()) AiContentType.reasoning else AiContentType.reply
    }


    data class Choice(
        var delta: Delta? = null,
        var attachdata: AttachData? = null,
        var finish_reason: String? = null,
        var index: Int? = null,
        var logprobs: String? = null
    )

    data class Delta(
        /**
         * 回复内容
         */
        var content: String? = null,
        /**
         * 推导内容
         */
        var reasoning_content: String? = null
    )

    data class AttachData(val deviceIds: List<String> = emptyList())

    enum class StreamType { Common, HighTouchInteraction }

}
