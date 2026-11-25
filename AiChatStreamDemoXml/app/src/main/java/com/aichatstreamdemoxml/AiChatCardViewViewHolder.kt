package com.aichatstreamdemoxml

import com.aichatstreamdemoxml.databinding.ItemChatCardViewBinding
import com.aichatstreamdemoxml.dto.AiChatMessageDto
import com.archeanx.libx.util.toDefault

class AiChatCardViewViewHolder(private val binding: ItemChatCardViewBinding) {

    fun bind(data: AiChatMessageDto) {
        val content = data.content.toDefault("")
        // 解析 <CardView>content</CardView>
        val cardContent = content.substringAfter("<CardView>").substringBefore("</CardView>")
        binding.cardContentTv.text = cardContent
    }
}
