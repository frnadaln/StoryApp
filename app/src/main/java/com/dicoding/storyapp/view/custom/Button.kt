package com.dicoding.storyapp.view.custom

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.Gravity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.dicoding.storyapp.R

class Button : AppCompatButton {
    private var txtColor: Int = 0

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        txtColor = ContextCompat.getColor(context, android.R.color.background_light)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        setTextColor(txtColor)
        textSize = 12f
        gravity = Gravity.CENTER
        text = if(isEnabled) resources.getString(R.string.submit) else resources.getString(R.string.not_valid_data)
    }
}
