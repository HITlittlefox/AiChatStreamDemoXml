package com.aichatstreamdemoxml

import com.aichatstreamdemoxml.databinding.ItemQuestionBinding
import com.aichatstreamdemoxml.dto.AiChatMessageDto
import com.archeanx.libx.util.toDefault

/**
 * 提问气泡
 */
class AiChatQuestionViewHolder(private val binding: ItemQuestionBinding) {

    fun bind(data: AiChatMessageDto) {
        binding.questionContentTv.text = data.question.toDefault("")
    }
}
