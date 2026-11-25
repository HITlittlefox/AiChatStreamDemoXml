package com.aichatstreamdemoxml

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.widget.FrameLayout
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.aichatstreamdemoxml.databinding.ItemAnswerBinding
import com.aichatstreamdemoxml.databinding.ItemChatCardViewBinding
import com.aichatstreamdemoxml.databinding.ItemQuestionBinding
import com.aichatstreamdemoxml.dto.AiChatMessageDto
import com.aichatstreamdemoxml.util.AiMarkdownHelper

/**
 * 聊天记录适配器
 *
 * 每条会话数据拆成两行：提问（右对齐气泡）与回答（左侧纯文本），
 * 便于按照 Gemini / 豆包的消息流形态排版。
 */
class AiChatAdapter(val context: Context, val owner: LifecycleOwner) : RecyclerView.Adapter<AiChatAdapter.BaseBindingViewHolder>() {

    val listData = mutableListOf<AiChatMessageDto>()

    companion object {
        /**
         * 重试 回复
         */
        const val CLICK_RESET = 11111111

        /**
         * 复制内容
         */
        const val CLICK_COPY = 11111112

        var copyContent = ""

        const val TYPE_QUESTION = 0
        const val TYPE_COMMON = 1
        const val TYPE_CARD_VIEW = 2
        const val TYPE_FOOTER = 3
    }


    private val mAiMarkdownHelper = AiMarkdownHelper(context)
    var footerHeight: Int = 0
        set(value) {
            if (field == value) return
            field = value
            if (itemCount > 0) notifyItemChanged(itemCount - 1)
        }

    class BaseBindingViewHolder(val binding: ViewBinding) : RecyclerView.ViewHolder(binding.root)

    /**
     * 数据下标转提问行下标
     */
    fun questionPosition(index: Int) = index * 2

    /**
     * 数据下标转回答行下标
     */
    fun answerPosition(index: Int) = index * 2 + 1

    override fun getItemViewType(position: Int): Int {
        if (position == listData.size * 2) return TYPE_FOOTER
        if (position % 2 == 0) return TYPE_QUESTION
        val data = listData[position / 2]
        return if (data.content?.contains("<CardView>") == true && data.content?.contains("</CardView>") == true) {
            TYPE_CARD_VIEW
        } else {
            TYPE_COMMON
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseBindingViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_FOOTER -> BaseBindingViewHolder(object : ViewBinding {
                override fun getRoot(): View = FrameLayout(parent.context).apply {
                    layoutParams = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0)
                }
            })
            TYPE_QUESTION -> BaseBindingViewHolder(ItemQuestionBinding.inflate(inflater, parent, false))
            TYPE_CARD_VIEW -> BaseBindingViewHolder(ItemChatCardViewBinding.inflate(inflater, parent, false))
            else -> BaseBindingViewHolder(ItemAnswerBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: BaseBindingViewHolder, position: Int) {
        if (getItemViewType(position) == TYPE_FOOTER) {
            holder.itemView.layoutParams = (holder.itemView.layoutParams ?: RecyclerView.LayoutParams(-1, 0)).apply {
                height = footerHeight
            }
            return
        }
        val data = listData[position / 2]
        when (getItemViewType(position)) {
            TYPE_QUESTION -> {
                AiChatQuestionViewHolder(holder.binding as ItemQuestionBinding).bind(data)
            }

            TYPE_CARD_VIEW -> {
                AiChatCardViewViewHolder(holder.binding as ItemChatCardViewBinding).bind(data)
            }

            else -> {
                AiChatAnswerViewHolder(holder.binding as ItemAnswerBinding, mAiMarkdownHelper).bind(data)
            }
        }
    }

    override fun getItemCount(): Int {
        return listData.size * 2 + 1
    }

    fun addData(data: AiChatMessageDto) {
        listData.add(data)
        notifyItemRangeInserted(listData.size * 2 - 2, 2)
    }

}
