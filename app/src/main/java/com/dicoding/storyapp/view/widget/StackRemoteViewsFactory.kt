package com.dicoding.storyapp.view.widget


import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.os.bundleOf
import com.dicoding.storyapp.R
import com.dicoding.storyapp.data.ListStory
import com.dicoding.storyapp.repository.Repository
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking

internal class StackRemoteViewsFactory(
    private val mContext : Context,
    private val repository: Repository
) : RemoteViewsService.RemoteViewsFactory{

    private val mWidgetItems = ArrayList<Bitmap>()
    override fun onCreate() {

    }

    override fun onDataSetChanged() {
        mWidgetItems.clear()
        var image : List<ListStory>

        runBlocking {
            image = repository.getListStory()
        }

        runBlocking {
            val thread = image.map {
                async(Dispatchers.IO) {
                    val bitmap =
                        Picasso.get()
                            .load(it.photoUrl)
                            .get()
                    bitmap
                }
            }
            val bitmaps = thread.awaitAll()
            mWidgetItems.addAll(bitmaps)
        }
    }

    override fun onDestroy() {
        mWidgetItems.clear()
    }

    override fun getCount(): Int = mWidgetItems.size

    override fun getViewAt(position: Int): RemoteViews {
        val remoteView = RemoteViews(mContext.packageName, R.layout.widget_layout)
        remoteView.setImageViewBitmap(R.id.imageView, mWidgetItems[position])
        val extras = bundleOf(
            StoryImageBannerWidget.EXTRA_ITEM to position
        )
        val fillInIntent = Intent()
        fillInIntent.putExtras(extras)

        remoteView.setOnClickFillInIntent(R.id.imageView, fillInIntent)
        return remoteView
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = position.toLong()

    override fun hasStableIds(): Boolean = true
}
