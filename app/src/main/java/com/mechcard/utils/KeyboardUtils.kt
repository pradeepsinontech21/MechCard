package com.mechcard.utils

import android.app.Activity
import android.graphics.Rect
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener

object KeyboardUtils {
    fun attachKeyboardVisibilityListener(activity: Activity, listener: KeyboardVisibilityListener) {
        val rootView = activity.findViewById<View>(android.R.id.content)
        rootView.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            private val MIN_KEYBOARD_HEIGHT_PX = 150
            private var previousVisible = false

            override fun onGlobalLayout() {
                val r = Rect()
                rootView.getWindowVisibleDisplayFrame(r)
                val screenHeight = rootView.height
                val keyboardHeight = screenHeight - r.bottom

                val isVisible = keyboardHeight > MIN_KEYBOARD_HEIGHT_PX

                if (isVisible != previousVisible) {
                    listener.onKeyboardVisibilityChanged(isVisible)
                }

                previousVisible = isVisible
            }
        })
    }

    interface KeyboardVisibilityListener {
        fun onKeyboardVisibilityChanged(isVisible: Boolean)
    }
}
