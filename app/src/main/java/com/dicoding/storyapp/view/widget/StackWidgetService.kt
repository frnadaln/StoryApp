package com.dicoding.storyapp.view.widget

import android.content.Intent
import android.widget.RemoteViewsService
import com.dicoding.storyapp.di.Injection

class StackWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
        val repository = Injection.provideRepository(this.applicationContext)
        return StackRemoteViewsFactory(this.applicationContext, repository)
    }
}
