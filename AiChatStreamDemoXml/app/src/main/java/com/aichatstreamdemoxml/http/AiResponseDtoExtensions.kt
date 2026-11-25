package com.aichatstreamdemoxml.http

import com.aichatstreamdemoxml.util.stringToObj

fun AiResponseDto.isCardViewAiResponseDto(): Boolean {
    val choice = this.choices?.getOrNull(0) ?: return false
    val content = choice.delta?.content ?: return false
    return content.contains("<CardView>") && content.contains("</CardView>")
}

fun String.isCardViewAiResponseString(): Boolean {
    return this.contains("<CardView>") && this.contains("</CardView>")
}

fun AiResponseDto.toCardViewAiResponseDto(): CardViewAiResponseDto? {
    if (this.isCardViewAiResponseDto()) {
        // 由于 CardViewAiResponseDto 只是继承了 AiResponseDto 并没有新字段，
        // 我们可以直接转换或者创建一个新对象并复制属性。
        // 为了简单起见，这里我们创建一个新对象并浅拷贝属性。
        // 如果使用了 Gson 或其他序列化库，可能需要反序列化。
        
        // 简单方式：新建对象并赋值
        val cardViewDto = CardViewAiResponseDto()
        cardViewDto.choices = this.choices
        cardViewDto.created = this.created
        cardViewDto.id = this.id
        cardViewDto.model = this.model
        cardViewDto.system_fingerprint = this.system_fingerprint
        return cardViewDto
    }
    return null
}
