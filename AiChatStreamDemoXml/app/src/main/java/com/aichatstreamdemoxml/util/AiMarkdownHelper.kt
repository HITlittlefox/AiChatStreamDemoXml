package com.aichatstreamdemoxml.util

import android.content.Context
import android.widget.TextView
import com.archeanx.libx.util.toDefault
import io.noties.markwon.Markwon
import io.noties.markwon.core.CorePlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tables.TableTheme
import io.noties.markwon.movement.MovementMethodPlugin

class AiMarkdownHelper(context: Context) {
    private val markwon: Markwon = Markwon.builder(context)
        .usePlugin(CorePlugin.create())
        .usePlugin(TablePlugin.create(TableTheme.create(context).apply {
            tableMaxWidth(AppKtx.getPhoneWidth(context) - 32.dpToPx)
        }))
        .usePlugin(MovementMethodPlugin.create())
        .build()

    fun setMarkdown(textView: TextView, content: String?) {
        markwon.setMarkdown(textView, content.toDefault(""))
    }
}
