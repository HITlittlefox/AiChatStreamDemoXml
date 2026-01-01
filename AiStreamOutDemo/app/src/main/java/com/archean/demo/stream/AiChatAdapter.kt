package com.archean.demo.stream

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import com.archean.demo.stream.databinding.ItemMainBinding
import com.archean.demo.stream.dto.AiChatMessageDto
import com.archean.demo.stream.util.AiMarkdownHelper
import com.archeanx.libx.adapter.binding.XRvBindingHolder
import com.archeanx.libx.adapter.binding.XRvBindingPureDataAdapter


/**
 * 聊天记录适配器
 */
class AiChatAdapter(val context: Context, val owner: LifecycleOwner) : XRvBindingPureDataAdapter<AiChatMessageDto>(R.layout.item_main) {
    companion object {
        /**
         * 刷新 思考内容
         */
        const val REFRESH_CONTENT_REASONING = "refresh_content_reasoning"

        /**
         * 刷新 回复内容
         */
        const val REFRESH_CONTENT_REPLY = "refresh_content_reply"

        /**
         * 重试 回复
         */
        const val CLICK_RESET = 11111111

        /**
         * 复制内容
         */
        const val CLICK_COPY = 11111112

        var copyContent = ""
    }


    private val mAiMarkdownHelper = AiMarkdownHelper(context)

    override fun onBindViewHolder(holder: XRvBindingHolder, position: Int, data: AiChatMessageDto, payloads: MutableList<Any>) {
        if (payloads.size > 0) {
            val binding = holder.getBinding<ItemMainBinding>()
            when (payloads.getOrNull(0)) {
                REFRESH_CONTENT_REASONING -> {
                    mAiMarkdownHelper.setMarkdown(binding.reasoningContentTv, data.reasoning_content)
                }

                REFRESH_CONTENT_REPLY -> {
                    mAiMarkdownHelper.setMarkdown(binding.replyContentTv, data.content)
                }
            }
        }
    }

    override fun onBindViewHolder(holder: XRvBindingHolder, position: Int, data: AiChatMessageDto) {
        val binding = holder.getBinding<ItemMainBinding>()
        val viewHolder = AiChatCommonViewHolder(binding, mAiMarkdownHelper)
        viewHolder.bind(data)
    }

}
