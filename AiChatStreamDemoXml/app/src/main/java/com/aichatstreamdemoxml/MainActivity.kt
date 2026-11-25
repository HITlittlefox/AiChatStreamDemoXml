package com.aichatstreamdemoxml


import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.aichatstreamdemoxml.databinding.ActivityMainBinding
import com.aichatstreamdemoxml.dto.AiChatMessageDto
import com.aichatstreamdemoxml.http.AiHttpRequest
import com.aichatstreamdemoxml.http.AiMockHttpRequest
import com.aichatstreamdemoxml.http.AiMockScripts
import com.aichatstreamdemoxml.util.AiChatStateType
import com.aichatstreamdemoxml.util.AiStreamOutHelper
import com.aichatstreamdemoxml.util.AppKtx
import com.aichatstreamdemoxml.util.KeyBoardUtils
import com.aichatstreamdemoxml.util.LogHelper
import com.aichatstreamdemoxml.util.ScrollBoundaryDeciderAdapter
import com.aichatstreamdemoxml.util.TouchEmptyCloseKeyBoardUtils
import com.aichatstreamdemoxml.util.initAdapter
import com.aichatstreamdemoxml.util.keepTopOrFollowLastItem
import com.aichatstreamdemoxml.util.linear
import com.aichatstreamdemoxml.util.onVisibility
import com.aichatstreamdemoxml.util.scrollToBottom
import com.aichatstreamdemoxml.util.uiDelayShow
import com.aichatstreamdemoxml.util.updateWidth
import com.aichatstreamdemoxml.util.verDivider
import com.archeanx.libx.adapter.binding.OnItemClickListener
import com.archeanx.libx.adapter.binding.XRvBindingHolder
import com.archeanx.libx.base.XBaseBindingActivity
import com.archeanx.libx.util.ToastUtil
import com.archeanx.libx.util.isTrue
import com.archeanx.libx.util.setOnClickNoDouble
import com.archeanx.libx.util.toDefault
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.gyf.immersionbar.ImmersionBar
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import java.util.UUID

class MainActivity : XBaseBindingActivity<ActivityMainBinding>(R.layout.activity_main) {

//    /**
//     * 会话列表 适配器
//     */
//    private val mConversationListAdapter by lazy { AiConversationListAdapter() }


    /**
     * 聊天记录 适配器
     */
    private val mChatAdapter by lazy { AiChatAdapter(this, this) }

    /**
     * 是否需要自动滚动到底部
     */
    private var isAutoScrollBottom = true

    /** 新问题定位在顶部时，为回答内容预留一屏滚动空间。 */
    private var anchoredQuestionPosition: Int? = null


    /**
     * 点击非输入框区域，关闭软键盘
     */
    private val mTouchEmptyCloseKeyBoardUtils by lazy { TouchEmptyCloseKeyBoardUtils() }

    /**
     *  提问请求
     */
    private val mAiHttpRequest by lazy { AiHttpRequest() }

    /**
     * 流式输出辅助类
     */
    private val mAiStreamOutHelper by lazy { AiStreamOutHelper(this) }

    /**
     * 模拟请求，仅用于调试演示
     */
    private val mMockHttpRequest by lazy { AiMockHttpRequest() }

    private val uiHandler by lazy { Handler(Looper.getMainLooper()) }

    /**
     * 模拟批次：问题列表、当前进度、是否正在执行
     */
    private var mockQuestions: List<String> = emptyList()
    private var mockIndex = 0
    private var isMockRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBottomNavigationForAndroid15()

    }

    @Deprecated("not use, have gone")
    private fun initBottomNavigationForAndroid15(){
        ImmersionBar.with(this).transparentStatusBar().statusBarDarkFont(true).keyboardEnable(true).init()

        // V1 强制消费
        val bottomNavigationView = binding.bottomNavigationView
        // 强制禁用所有系统内边距对 BottomNavigationView 的影响
        // 这将覆盖 Material 库默认设置的监听器
        ViewCompat.setOnApplyWindowInsetsListener(bottomNavigationView) { v, insets ->
            // 确保你的 BottomNavigationView 不受任何系统内边距影响
            // 这里的关键是返回 CONSUMED，告诉系统这个 View 已经处理了内边距，
            // 并且我们在处理中只应用了 0 填充。
            v.setPadding(0, 0, 0, 0)

            // 消费掉内边距，阻止它们向下传递给 BottomNavigationView 的子视图
            WindowInsetsCompat.CONSUMED
        }

        // V2 只移除 BottomNavigationView 自动应用的内边距，然后手动将系统导航栏的内边距重新应用到 padding 上，并加上你想要的 24dp 间距。
//        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
//        val customMargin = 24 // 你想要的底部间距 24dp
//
//        ViewCompat.setOnApplyWindowInsetsListener(bottomNavigationView) { v, insets ->
//            // 1. 获取系统提供的底部内边距 (System Navigation Bar Inset)
//            val systemBottomInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
//
//            // 2. 计算最终的 paddingBottom:
//            //    系统内边距 (systemBottomInset) + 你的自定义间距 (customMargin)
//            val finalPaddingBottom =
//                systemBottomInset + (customMargin * resources.displayMetrics.density).toInt() // 转换为像素
//
//            // 3. 应用新的填充，并保持左右上为 0
//            v.updatePadding(
//                left = 0,
//                top = 0,
//                right = 0,
//                bottom = finalPaddingBottom // 使用计算后的值
//            )
//
//            // 4. 返回 CONSUMED，表示我们已经处理了底部的系统栏内边距。
//            //    注意：为了确保 BottomNavigationView 不处理其他类型（如 IME）的内边距，你可能需要更复杂的逻辑。
//            //    但对于只处理底部导航栏来说，CONSUMED 是最安全的做法。
//            WindowInsetsCompat.CONSUMED
//        }
    }


    override fun initView() {
        binding.drawerIv.setOnClickNoDouble {
            if (!binding.drawerLayout.isDrawerOpen(binding.navigationDrawer)) {
                binding.drawerLayout.openDrawer(binding.navigationDrawer)
            }
        }
        //因为footer 是全局的，默认是null的，所有需要等待一会儿之后，再去设置footer的旋转度
        binding.smartLayout.postDelayed({
            binding.smartLayout.refreshFooter?.view?.scaleY = -1f
        }, 500)
        binding.smartLayout.setScrollBoundaryDecider(object : ScrollBoundaryDeciderAdapter() {
            override fun canLoadMore(content: View?): Boolean {
                return super.canRefresh(content)
            }
        })

        // Keep only a small breathing space between chat rows; large per-row dividers
        // accumulate because each turn is rendered as separate question/answer items.
        binding.recyclerView.linear().initAdapter(mChatAdapter).verDivider(12f, com.archeanx.libx.style.R.color.transparent)


        //监听滑动，显示底部按钮
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                when (newState) {
                    RecyclerView.SCROLL_STATE_DRAGGING -> {
                        clearQuestionAnchor()
                        isAutoScrollBottom = false
                        //  mHelper.onScrollViewVisibility(binding.scrollBottomIv, !isAutoScrollBottom)
                        binding.scrollBottomIv.onVisibility(false)
                    }

                    RecyclerView.SCROLL_STATE_IDLE -> {
                        // 滑动停止
                        isAutoScrollBottom = !binding.recyclerView.canScrollVertically(1)
                        binding.scrollBottomIv.onVisibility(!isAutoScrollBottom)
                    }

                    RecyclerView.SCROLL_STATE_SETTLING -> {
                        // 惯性滑动
                        isAutoScrollBottom = !binding.recyclerView.canScrollVertically(1)
                        binding.scrollBottomIv.onVisibility(!isAutoScrollBottom)
                    }
                }
                LogHelper.e("addOnScrollListener", "newState:" + newState + " isAutoScrollBottom:" + isAutoScrollBottom)
            }
        })

        binding.scrollBottomIv.setOnClickNoDouble {
            binding.recyclerView.post {
                //停止滚动，然后滚动到底部
                binding.recyclerView.stopScroll()
                binding.recyclerView.scrollToBottom()
                isAutoScrollBottom = true
                binding.scrollBottomIv.onVisibility(!isAutoScrollBottom)
            }
        }

        //抽屉宽度
        binding.navigationDrawer.updateWidth((AppKtx.getPhoneWidth(this) * 0.76f).toInt())

        //滑动抽屉时，对主布局进行位移
        binding.drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                // 根据滑动进度计算主布局的偏移量

                // 获取抽屉的 Gravity 方向
                val layoutParams = drawerView.layoutParams as DrawerLayout.LayoutParams
                val gravity = layoutParams.gravity

                // 根据滑动进度和抽屉方向计算主布局的偏移量
                val translationX = if (gravity and Gravity.START == Gravity.START) {
                    // 抽屉在左边，主布局向右偏移
                    drawerView.width * slideOffset
                } else {
                    // 抽屉在右边，主布局向左偏移
                    -drawerView.width * slideOffset
                }
                binding.mainLayout.translationX = translationX
            }

            override fun onDrawerOpened(drawerView: View) {
                // 抽屉打开时的操作
            }

            override fun onDrawerClosed(drawerView: View) {
                // 抽屉关闭时，将主布局复位
                binding.mainLayout.translationX = 0f
            }

            override fun onDrawerStateChanged(newState: Int) {
                // 抽屉状态改变时的操作
            }
        })
        //发送按钮
        binding.sendIv.setOnClickNoDouble {
            if (onSubmitQuestion(binding.inputEt.editableText.toString())) {
                binding.inputEt.setText("")
            }
        }

        //流输出
        mAiStreamOutHelper.apply {
            //type 变更回调
            updateType = { stateType: String ->
                mAiStreamOutHelper.mAiChatMessageDto?.let { msgDto ->
                    updateItem(msgDto, stateType)
                }
            }
            //思考，回复内容输出
            out = { stateTypee: String, content: String ->
                mAiStreamOutHelper.mAiChatMessageDto?.let {
                    // TODO 锚点：实际显示的地方
                        msgDto ->
                    when (stateTypee) {
                        AiChatStateType.think -> {
                            msgDto.reasoning_content = content
//                            LogHelper.e("AiHttpRequest:out","AiChatStateType.think"+msgDto.reasoning_content)
                            updateItem(msgDto, AiChatStateType.think)
                        }

                        AiChatStateType.reply_ing -> {
                            msgDto.content = content
                            updateItem(msgDto, AiChatStateType.reply_ing)
                        }
                    }
                }
            }
            complete = {
                //模拟批次：一条输出结束后稍作停顿，继续下一条
                if (isMockRunning) {
                    uiHandler.postDelayed({ submitNextMock() }, 800)
                }
            }
            error = {
                LogHelper.e("error")
            }
        }


        mTouchEmptyCloseKeyBoardUtils.filterViews.add(binding.sendIv)


        binding.titleTv.setOnClickNoDouble {
            isMockRunning = false
            mMockHttpRequest.cancel()
            mAiHttpRequest.cancel()
            mAiStreamOutHelper.reset()
        }

        //隐藏入口：长按标题播放一批模拟问答（仅 Debug 构建生效）
        binding.titleTv.setOnLongClickListener {
            startMockBatch()
            true
        }
    }

    override fun initData() {
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        initData()
    }


    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        mTouchEmptyCloseKeyBoardUtils.autoClose(this, ev)
        return super.dispatchTouchEvent(ev)
    }

    /**
     * 提交提问
     * @param msgTag 本地请求tag，错误请求时，需要刷新当前这一条
     */
    @Synchronized
    private fun onSubmitQuestion(content: String?, msgTag: String? = null): Boolean {
        //节流处理
        if (!AppKtx.isThrottle(500)) return false
        //请求状态处理
        if (mAiHttpRequest.isRequesting() || mMockHttpRequest.isRequesting() || mAiStreamOutHelper.isRequesting()) return false

        if (content.isNullOrEmpty()) {
            ToastUtil.show("请输入问题")
            return false
        }

        var msgDto: AiChatMessageDto? = null
        if (!msgTag.isNullOrEmpty()) {
            //判断最后一条消息 的唯一标识，是不是参数msgTag,是的话，就用当前这条消息，不是的话，就重新生成一条消息
            mChatAdapter.listData.getOrNull(mChatAdapter.listData.lastIndex)?.let { lastDto ->
                if (lastDto.solevar == msgTag) {
                    msgDto = lastDto
                }
            }
        }
        val isNewQuestion = msgDto == null
        if (isNewQuestion) {
            msgDto = AiChatMessageDto()
            msgDto?.solevar = UUID.randomUUID().toString()
            msgDto?.question = content
        }

        //模拟批次进行中，或 Debug 下命中脚本关键词时走 Mock 请求，其余情况走真实网络
        val hitMockScript = BuildConfig.DEBUG && AiMockScripts.match(content) != null
        val request = if (isMockRunning || hitMockScript) mMockHttpRequest else mAiHttpRequest
        val dto = msgDto!!

        val startRequest = {
            //输出交给 AiStreamOutHelper 统一处理
            mAiStreamOutHelper.onInit(dto, request)
            //关闭软键盘
            KeyBoardUtils.closeKeyboard(binding.inputEt, this)
            uiDelayShow({
                //需要判断有选中的会话，如果没有，则是需要生成新的对话id和对话内容
                request.onRequest(content)
            }, 300)
        }

        if (isNewQuestion) {
            if (isMockRunning) {
                //模拟批次不做动画，直接插入并顶到顶部
                addQuestionItemAndSnap(dto)
                startRequest()
            } else {
                //新提问：气泡从输入框飞向右上角，落位后再插入列表
                playFlyToQuestionAnimation(content ?: "") {
                    addQuestionItemAndSnap(dto)
                    startRequest()
                }
            }
        } else {
            startRequest()
        }
        return true
    }

    /**
     * 插入提问并让这条 item 顶部对齐列表顶部
     */
    private fun addQuestionItemAndSnap(data: AiChatMessageDto) {
        //新提问默认恢复自动跟随，避免之前上滑的状态把气泡顶回底部
        isAutoScrollBottom = true
        binding.scrollBottomIv.onVisibility(false)
        mChatAdapter.addData(data)
        val questionPosition = mChatAdapter.questionPosition(mChatAdapter.listData.size - 1)
        anchoredQuestionPosition = questionPosition
        mChatAdapter.footerHeight = binding.recyclerView.height
        binding.recyclerView.post {
            (binding.recyclerView.layoutManager as? LinearLayoutManager)
                ?.scrollToPositionWithOffset(questionPosition, 0)
            updateQuestionFooterHeight(questionPosition)
        }
    }

    /** Keep the latest question at the top while its answer grows; consume only unused viewport space. */
    private fun updateQuestionFooterHeight(questionPosition: Int) {
        val recycler = binding.recyclerView
        val question = recycler.findViewHolderForAdapterPosition(questionPosition)?.itemView
        val answer = recycler.findViewHolderForAdapterPosition(questionPosition + 1)?.itemView
        if (question == null || answer == null) return
        val usedHeight = question.height + answer.height
        mChatAdapter.footerHeight = (recycler.height - recycler.paddingTop - recycler.paddingBottom - usedHeight)
            .coerceAtLeast(0)
    }

    private fun clearQuestionAnchor() {
        anchoredQuestionPosition = null
    }

    /**
     * 发送动画：气泡从输入框位置飞向列表右上角，落位后回调
     */
    private fun playFlyToQuestionAnimation(text: String, onEnd: () -> Unit) {
        val recycler = binding.recyclerView
        if (text.isEmpty() || recycler.height == 0) {
            onEnd()
            return
        }
        val density = resources.displayMetrics.density
        fun dp(value: Int) = (value * density).toInt()
        // Insert the turn first, then move the whole conversation viewport upward
        // together so existing messages and the new question share one motion.
        onEnd()
        recycler.translationY = dp(42).toFloat()
        recycler.animate()
            .translationY(0f)
            .setDuration(360L)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    /**
     * 隐藏入口：长按标题触发一批模拟问答
     */
    private fun startMockBatch() {
        if (!BuildConfig.DEBUG) return
        if (isMockRunning || mAiHttpRequest.isRequesting() || mAiStreamOutHelper.isRequesting()) return
        mockQuestions = AiMockScripts.batch.map { it.question }
        mockIndex = 0
        isMockRunning = true
        submitNextMock()
    }

    /**
     * 提交模拟批次中的下一条问题
     */
    private fun submitNextMock() {
        if (!isMockRunning) return
        if (mockIndex >= mockQuestions.size) {
            isMockRunning = false
            mockQuestions = emptyList()
            return
        }
        if (!onSubmitQuestion(mockQuestions[mockIndex])) {
            isMockRunning = false
            return
        }
        mockIndex++
    }

    private fun updateItem(item: AiChatMessageDto, @AiChatStateType stateType: String? = null) {
//        LogHelper.e("updateItem", item.toJsonString())
        val oldStateType = item.localReplyState
//        LogHelper.e("AiHttpRequest", "error-item-tag" + item.localRequestTag.toDefault(""))
        val index = mChatAdapter.listData.indexOfFirst { it.solevar == item.solevar }
//        LogHelper.e("AiHttpRequest", "error-item-tag2" + mChatAdapter.getItemOrNull(index)?.localRequestTag.toDefault(""))
//        LogHelper.e("AiHttpRequest", "error-updateItem" + index)

        LogHelper.e("AiHttpRequest:out", "updateItem" + stateType)

        if (index != -1) {
            if (oldStateType != stateType) {
                item.localReplyState = stateType
                if (stateType == AiChatStateType.out_end) {
                    //输出结束，折叠思考过程
                    item.reasoningExpanded = false
                }
                binding.recyclerView.post {
                    mChatAdapter.notifyItemChanged(mChatAdapter.answerPosition(index))
                }
            } else {
                binding.recyclerView.post {
                    when (stateType) {
                        AiChatStateType.think -> {

                            //刷新tv的两外一种方式
                            item.onReasonChange(item.solevar)
                            // mChatAdapter.notifyItemChanged(index, AiChatAdapter.REFRESH_CONTENT_REASONING)
                        }

                        AiChatStateType.reply_ing -> {
                            //刷新tv的两外一种方式
                            item.onReplyChange(item.solevar)
                            // mChatAdapter.notifyItemChanged(index, AiChatAdapter.REFRESH_CONTENT_REPLY)
                        }

                        else -> {
                            mChatAdapter.notifyItemChanged(mChatAdapter.answerPosition(index))
                        }
                    }
                }
            }

            LogHelper.e("updateItem", "isAutoScrollBottom:" + isAutoScrollBottom.toString() + " s-state:" + stateType + " oldStateType:" + oldStateType)
            //提示滚动按钮展示时，不自动滚动到底部
            if (anchoredQuestionPosition != null || isAutoScrollBottom) {
                binding.recyclerView.post {
                    //内容不足一屏时把提问顶在右上角，超过一屏再跟随底部
                    val anchor = anchoredQuestionPosition
                    if (anchor != null) {
                        val layoutManager = binding.recyclerView.layoutManager as? LinearLayoutManager
                        layoutManager?.scrollToPositionWithOffset(anchor, 0)
                        binding.recyclerView.post { updateQuestionFooterHeight(anchor) }
                    } else {
                        binding.recyclerView.keepTopOrFollowLastItem()
                    }
                }
            } else {
                // User scrolling cancels the top lock, but the footer still tracks
                // the latest turn's unused viewport space throughout the response.
                val latestQuestionPosition = mChatAdapter.questionPosition(mChatAdapter.listData.lastIndex)
                binding.recyclerView.post { updateQuestionFooterHeight(latestQuestionPosition) }
                //就算当前不需要自动滑动到底部，当有新的提问时，也需要滑动到底部
                if (stateType == AiChatStateType.wait) {
                    binding.scrollBottomIv.performClick()
                }
            }
        }
    }


    override fun onDestroy() {
        isMockRunning = false
        mMockHttpRequest.cancel()
        uiHandler.removeCallbacksAndMessages(null)
        mAiHttpRequest.cancel()
        super.onDestroy()
    }

}
