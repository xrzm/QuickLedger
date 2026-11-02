package com.quickledger.app.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.provider.Settings
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.quickledger.app.R
import com.quickledger.app.domain.model.Transaction
import com.quickledger.app.domain.model.TransactionType
import com.quickledger.app.domain.repository.CategoryRepository
import com.quickledger.app.domain.repository.TransactionRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class QuickRecordOverlayReceiver : BroadcastReceiver() {

    @Inject lateinit var transactionRepo: TransactionRepository
    @Inject lateinit var categoryRepo: CategoryRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_QUICK_RECORD) return

        val appContext = context.applicationContext
        if (!Settings.canDrawOverlays(appContext)) {
            Toast.makeText(
                appContext,
                "请先允许「极速记账」显示在其他应用上层，之后再点小组件即可直接记账",
                Toast.LENGTH_LONG
            ).show()
            runCatching {
                appContext.startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${appContext.packageName}")
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }.onFailure {
                runCatching {
                    appContext.startActivity(
                        Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:${appContext.packageName}")
                        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
            }
            return
        }

        QuickRecordOverlay.show(
            context = appContext,
            transactionRepo = transactionRepo,
            categoryRepo = categoryRepo,
            categoryName = intent.getStringExtra(EXTRA_CATEGORY) ?: "其他",
            icon = intent.getStringExtra(EXTRA_ICON) ?: "📦",
            isIncome = intent.getBooleanExtra(EXTRA_IS_INCOME, false)
        )
    }

    companion object {
        const val ACTION_QUICK_RECORD = "com.quickledger.app.action.QUICK_RECORD"
        const val EXTRA_CATEGORY = "category"
        const val EXTRA_ICON = "icon"
        const val EXTRA_IS_INCOME = "isIncome"
    }
}

/**
 * 直接挂在桌面上的悬浮记账卡片：不启动 Activity，桌面始终在前台，所以不会被系统的启动遮罩盖住。
 */
private object QuickRecordOverlay {

    private var windowManager: WindowManager? = null
    private var root: View? = null
    private var scope: CoroutineScope? = null
    private var saving = false

    fun show(
        context: Context,
        transactionRepo: TransactionRepository,
        categoryRepo: CategoryRepository,
        categoryName: String,
        icon: String,
        isIncome: Boolean
    ) {
        dismiss()

        val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager ?: return
        val inflater = LayoutInflater.from(ContextThemeWrapper(context, android.R.style.Theme_Material_Light))
        val view = inflater.inflate(R.layout.view_quick_record_overlay, null)

        val amountInput = view.findViewById<EditText>(R.id.overlay_amount)
        val iconView = view.findViewById<TextView>(R.id.overlay_icon)
        val titleView = view.findViewById<TextView>(R.id.overlay_title)
        val saveButton = view.findViewById<Button>(R.id.overlay_save)
        val cancelButton = view.findViewById<Button>(R.id.overlay_cancel)

        iconView.text = icon
        titleView.text = "$categoryName · ${if (isIncome) "收入" else "支出"}"

        val screenHeight = context.resources.displayMetrics.heightPixels
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
            // 略微上移，键盘弹出时不会挡住按钮
            y = -(screenHeight * 0.08f).toInt()
            softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        }

        val overlayScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        windowManager = wm
        root = view
        scope = overlayScope
        saving = false

        view.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_OUTSIDE) {
                dismiss()
                true
            } else false
        }
        view.isFocusableInTouchMode = true
        val backListener = View.OnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                dismiss()
                true
            } else false
        }
        view.setOnKeyListener(backListener)
        amountInput.setOnKeyListener(backListener)

        cancelButton.setOnClickListener { dismiss() }

        saveButton.setOnClickListener {
            val value = amountInput.text.toString().toDoubleOrNull() ?: return@setOnClickListener
            if (value <= 0 || saving) return@setOnClickListener
            saving = true
            saveButton.isEnabled = false
            saveButton.text = "保存中"
            overlayScope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        categoryRepo.initializeDefaultCategoriesIfNeeded()
                        val categories = categoryRepo.getAllCategories().first()
                        val category = categories.find { it.name == categoryName } ?: categories.first()
                        transactionRepo.insertTransaction(
                            Transaction(
                                amount = value,
                                type = if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE,
                                categoryId = category.id
                            )
                        )
                    }
                    Toast.makeText(context, "已记录 ¥${"%.2f".format(value)}", Toast.LENGTH_SHORT).show()
                    dismiss()
                } catch (e: Exception) {
                    Toast.makeText(context, "保存失败: ${e.message}", Toast.LENGTH_SHORT).show()
                    saving = false
                    saveButton.isEnabled = true
                    saveButton.text = "保存"
                }
            }
        }

        amountInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                saveButton.performClick()
                true
            } else false
        }

        try {
            wm.addView(view, params)
        } catch (e: Exception) {
            Toast.makeText(context, "悬浮窗打开失败: ${e.message}", Toast.LENGTH_SHORT).show()
            dismiss()
            return
        }

        view.post {
            amountInput.requestFocus()
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.showSoftInput(amountInput, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    fun dismiss() {
        scope?.cancel()
        scope = null
        val view = root ?: return
        val wm = windowManager
        root = null
        windowManager = null
        saving = false
        runCatching { wm?.removeViewImmediate(view) }
    }
}
