package com.quickledger.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.quickledger.app.MainActivity
import com.quickledger.app.R

class QuickRecordWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        // 应用更新后刷新小组件的点击意图，否则旧意图仍指向旧的记账界面
        if (intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, QuickRecordWidget::class.java))
            if (ids.isNotEmpty()) onUpdate(context, manager, ids)
        }
    }

    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_quick_record)

            // 分类按钮 → 桌面上的悬浮记账卡片（不启动 Activity）
            val quickButtons = listOf(
                Triple(R.id.widget_btn_food, "餐饮", false),
                Triple(R.id.widget_btn_shopping, "购物", false),
                Triple(R.id.widget_btn_transport, "交通", false),
                Triple(R.id.widget_btn_entertainment, "娱乐", false),
                Triple(R.id.widget_btn_housing, "住房", false)
            )
            for ((btnId, name, isIncome) in quickButtons) {
                val icon = iconFor(name)
                val intent = Intent(context, QuickRecordOverlayReceiver::class.java).apply {
                    action = QuickRecordOverlayReceiver.ACTION_QUICK_RECORD
                    putExtra(QuickRecordOverlayReceiver.EXTRA_CATEGORY, name)
                    putExtra(QuickRecordOverlayReceiver.EXTRA_ICON, icon)
                    putExtra(QuickRecordOverlayReceiver.EXTRA_IS_INCOME, isIncome)
                }
                val pi = PendingIntent.getBroadcast(context, btnId, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                views.setOnClickPendingIntent(btnId, pi)
                views.setTextViewText(btnId, "$icon\n$name")
            }

            // "更多" button → MainActivity (open full app)
            val moreIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val morePi = PendingIntent.getActivity(context, R.id.widget_btn_more, moreIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.widget_btn_more, morePi)
            views.setTextViewText(R.id.widget_btn_more, "➕\n更多")

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun iconFor(name: String): String = when (name) {
            "餐饮" -> "🍔"; "购物" -> "🛒"; "交通" -> "🚗"; "娱乐" -> "🎮"; "住房" -> "🏠"; else -> "📦"
        }
    }
}
