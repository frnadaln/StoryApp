@file:Suppress("DEPRECATION")

package com.dicoding.storyapp.view.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.RemoteViews
import com.dicoding.storyapp.R
import com.dicoding.storyapp.view.main.MainActivity

class StoryImageBannerWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
    }

    override fun onDisabled(context: Context) {
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        if (intent != null && intent.action == ACTION) {
            val clickedItem = intent.getStringExtra(EXTRA_ITEM)
        }
    }

    companion object {
        const val EXTRA_ITEM = "image_banner"
        const val ACTION = "action"
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val intent = Intent(context, StackWidgetService::class.java).apply {
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME).toString()) // Ganti toUri dengan Uri.parse()
    }

    val views = RemoteViews(context.packageName, R.layout.story_image_banner_widget)
    views.setRemoteAdapter(R.id.stack_view, intent)

    val toastIntent = Intent(context, StoryImageBannerWidget::class.java).apply {
        action = StoryImageBannerWidget.ACTION
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
    }

    val toastPendingIntent = PendingIntent.getBroadcast(
        context, 0, toastIntent,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        else PendingIntent.FLAG_UPDATE_CURRENT
    )
    views.setPendingIntentTemplate(R.id.stack_view, toastPendingIntent)

    val openAppIntent = Intent(context, MainActivity::class.java)
    val openAppPendingIntent = PendingIntent.getActivity(
        context, 0, openAppIntent,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        else PendingIntent.FLAG_UPDATE_CURRENT
    )
    views.setOnClickPendingIntent(R.id.widget_root_layout, openAppPendingIntent)

    appWidgetManager.updateAppWidget(appWidgetId, views)
}
