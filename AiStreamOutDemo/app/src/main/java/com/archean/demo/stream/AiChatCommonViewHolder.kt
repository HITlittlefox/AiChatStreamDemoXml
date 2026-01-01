package com.archean.demo.stream

import android.widget.TextView
import com.archean.demo.stream.databinding.ItemMainBinding
import com.archean.demo.stream.dto.AiChatMessageDto
import com.archean.demo.stream.util.AiMarkdownHelper
import com.archeanx.libx.util.toDefault

class AiChatCommonViewHolder(private val binding: ItemMainBinding, private val aiMarkdownHelper: AiMarkdownHelper) {

    fun bind(data: AiChatMessageDto) {
        binding.questionContentTv.text = data.question.toDefault("")

        binding.replyContentTv.setOnLongClickListener { _ ->
            //长按处理
            true
        }
//        binding.replyContentTv.setOnTouchListener { view, event ->
//            view.setTag(R.id.ai_motion_event_tag, event)
//            false
//        }

        renderMarkdown(binding.reasoningContentTv, data.reasoning_content)
        renderMarkdown(binding.replyContentTv, data.content)

        //两种实现方式刷新textview 的方式
        data.reasonChange = {
            if (it == data.solevar) {
                renderMarkdown(binding.reasoningContentTv, data.reasoning_content)
            }
        }
        data.replyChange = {
            if (it == data.solevar) {
                renderMarkdown(binding.replyContentTv, data.content)
            }
        }
    }

    private fun renderMarkdown(textView: TextView, content: String?) {
        aiMarkdownHelper.setMarkdown(textView, content)
    }
}
