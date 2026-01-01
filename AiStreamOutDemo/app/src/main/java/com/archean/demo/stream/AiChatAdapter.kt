package com.archean.demo.stream

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.archean.demo.stream.databinding.ItemChatCardViewBinding
import com.archean.demo.stream.databinding.ItemMainBinding
import com.archean.demo.stream.dto.AiChatMessageDto
import com.archean.demo.stream.util.AiMarkdownHelper

/**
 * 聊天记录适配器
 */
class AiChatAdapter(val context: Context, val owner: LifecycleOwner) : RecyclerView.Adapter<AiChatAdapter.BaseBindingViewHolder>() {

    val listData = mutableListOf<AiChatMessageDto>()

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

        const val TYPE_COMMON = 0
        const val TYPE_CARD_VIEW = 1
    }


    private val mAiMarkdownHelper = AiMarkdownHelper(context)

    class BaseBindingViewHolder(val binding: ViewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun getItemViewType(position: Int): Int {
        val data = listData[position]
        return if (data.content?.contains("<CardView>") == true && data.content?.contains("</CardView>") == true) {
            TYPE_CARD_VIEW
        } else {
            TYPE_COMMON
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseBindingViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_CARD_VIEW) {
            BaseBindingViewHolder(ItemChatCardViewBinding.inflate(inflater, parent, false))
        } else {
            BaseBindingViewHolder(ItemMainBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: BaseBindingViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNotEmpty()) {
            if (getItemViewType(position) == TYPE_COMMON) {
                val binding = holder.binding as ItemMainBinding
                val data = listData[position]
                when (payloads.getOrNull(0)) {
                    REFRESH_CONTENT_REASONING -> {
                        mAiMarkdownHelper.setMarkdown(binding.reasoningContentTv, data.reasoning_content)
                    }

                    REFRESH_CONTENT_REPLY -> {
                        mAiMarkdownHelper.setMarkdown(binding.replyContentTv, data.content)
                    }
                }
            }
        } else {
            onBindViewHolder(holder, position)
        }
    }

    override fun onBindViewHolder(holder: BaseBindingViewHolder, position: Int) {
        val data = listData[position]
        if (getItemViewType(position) == TYPE_CARD_VIEW) {
            val binding = holder.binding as ItemChatCardViewBinding
            val viewHolder = AiChatCardViewViewHolder(binding)
            viewHolder.bind(data)
        } else {
            val binding = holder.binding as ItemMainBinding
            val viewHolder = AiChatCommonViewHolder(binding, mAiMarkdownHelper)
            viewHolder.bind(data)
        }
    }

    override fun getItemCount(): Int {
        return listData.size
    }

    fun addData(data: AiChatMessageDto) {
        listData.add(data)
        notifyItemInserted(listData.size - 1)
    }

}
