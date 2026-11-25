package com.aichatstreamdemoxml

import android.view.View
import com.aichatstreamdemoxml.databinding.ItemAnswerBinding
import com.aichatstreamdemoxml.dto.AiChatMessageDto
import com.aichatstreamdemoxml.util.AiChatStateType
import com.aichatstreamdemoxml.util.AiMarkdownHelper

/**
 * 回答内容，思考过程默认展开，输出结束后折叠
 */
class AiChatAnswerViewHolder(
    private val binding: ItemAnswerBinding,
    private val aiMarkdownHelper: AiMarkdownHelper
) {

    fun bind(data: AiChatMessageDto) {
        binding.reasoningHeaderLl.setOnClickListener {
            data.reasoningExpanded = !data.reasoningExpanded
            renderReasoning(data)
        }

        data.reasonChange = {
            if (it == data.solevar) {
                renderReasoning(data)
            }
        }
        data.replyChange = {
            if (it == data.solevar) {
                aiMarkdownHelper.setMarkdown(binding.replyContentTv, data.content)
            }
        }

        renderReasoning(data)
        aiMarkdownHelper.setMarkdown(binding.replyContentTv, data.content)
    }

    private fun renderReasoning(data: AiChatMessageDto) {
        val hasReasoning = !data.reasoning_content.isNullOrEmpty()
        binding.reasoningHeaderLl.visibility = if (hasReasoning) View.VISIBLE else View.GONE

        val thinking = data.localReplyState == AiChatStateType.wait || data.localReplyState == AiChatStateType.think
        binding.reasoningTitleTv.text = if (thinking) "思考中…" else "已深度思考"
        binding.reasoningArrowIv.rotation = if (data.reasoningExpanded) 180f else 0f
        binding.reasoningContentTv.visibility = if (hasReasoning && data.reasoningExpanded) View.VISIBLE else View.GONE

        aiMarkdownHelper.setMarkdown(binding.reasoningContentTv, data.reasoning_content)
    }
}
