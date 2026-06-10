package com.quickledger.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
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

    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_quick_record)

            // Category buttons → QuickRecordActivity (direct recording dialog)
            val quickButtons = listOf(
                Triple(R.id.widget_btn_food, "餐饮", false),
                Triple(R.id.widget_btn_shopping, "购物", false),
                Triple(R.id.widget_btn_transport, "交通", false),
                Triple(R.id.widget_btn_entertainment, "娱乐", false),
                Triple(R.id.widget_btn_housing, "住房", false)
            )
            for ((btnId, name, isIncome) in quickButtons) {
                val icon = iconFor(name)
                val intent = Intent(context, QuickRecordActivity::class.java).apply {
                    putExtra("category", name)
                    putExtra("icon", icon)
                    putExtra("isIncome", isIncome)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION
                }
                val pi = PendingIntent.getActivity(context, btnId, intent,
                    PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)
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
