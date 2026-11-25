package com.aichatstreamdemoxml

import android.view.View
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.widget.TextView
import android.widget.Toast
import android.util.TypedValue
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
                renderInlineReply(data)
            }
        }
        data.deviceChange = { renderInlineReply(data) }

        renderReasoning(data)
        renderInlineReply(data)
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

    private fun renderInlineReply(data: AiChatMessageDto) {
        val container = binding.inlineReplyContentLl
        container.removeAllViews()
        binding.replyContentTv.visibility = View.GONE
        binding.deviceChipsLl.visibility = View.GONE
        val reply = data.content.orEmpty()
        var cursor = 0
        data.deviceGroups.forEachIndexed { groupIndex, deviceIds ->
            val end = data.deviceGroupEndOffsets.getOrNull(groupIndex)?.coerceIn(cursor, reply.length) ?: cursor
            if (end > cursor) addMarkdownPart(container, reply.substring(cursor, end))
            addDeviceGroup(container, groupIndex, deviceIds)
            cursor = end
        }
        if (cursor < reply.length) addMarkdownPart(container, reply.substring(cursor))
        container.visibility = if (reply.isNotEmpty() || data.deviceGroups.isNotEmpty()) View.VISIBLE else View.GONE
    }

    private fun addMarkdownPart(container: android.widget.LinearLayout, text: String) {
        val part = TextView(container.context).apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            setTextColor(Color.BLACK)
            setLineSpacing(dp(6).toFloat(), 1f)
        }
        container.addView(part, android.widget.LinearLayout.LayoutParams(-1, -2))
        aiMarkdownHelper.setMarkdown(part, text)
    }

    private fun addDeviceGroup(container: android.widget.LinearLayout, groupIndex: Int, deviceIds: List<String>) {
        val group = android.widget.LinearLayout(container.context).apply { orientation = android.widget.LinearLayout.VERTICAL }
        container.addView(group, android.widget.LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(4) })
        val groupLabel = TextView(group.context).apply {
            text = "关联设备 ${groupIndex + 1}"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            setTextColor(Color.GRAY)
        }
        group.addView(groupLabel)
        deviceIds.forEach { id ->
            val title = when (id) {
                "device_light_living_room" -> "客厅灯"
                "device_ac_bedroom" -> "卧室空调"
                "device_curtain_living_room" -> "客厅窗帘"
                else -> "设备 ${id.takeLast(6)}"
            }
            val chip = TextView(group.context).apply {
                text = "$title  ·  在线"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                setTextColor(Color.rgb(45, 70, 105))
                setPadding(dp(14), dp(10), dp(14), dp(10))
                background = GradientDrawable().apply {
                    cornerRadius = dp(16).toFloat()
                    setColor(Color.rgb(239, 245, 255))
                }
                setOnClickListener { Toast.makeText(context, "$title（$id）", Toast.LENGTH_SHORT).show() }
            }
            group.addView(chip, android.widget.LinearLayout.LayoutParams(-2, -2).apply { topMargin = dp(6) })
        }
    }

    private fun dp(value: Int): Int = (value * binding.root.resources.displayMetrics.density).toInt()
}
