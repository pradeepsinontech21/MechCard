package com.mechcard.ui.custom

import android.R
import android.content.Context
import android.util.TypedValue
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog

class MechProgressDialog(@NonNull context: Context) : AlertDialog.Builder(context) {

    init {
        val progressBar = ProgressBar(
            context,
            null,
            R.attr.progressBarStyleHorizontal
        )
        progressBar.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        progressBar.isIndeterminate = true
        val container = LinearLayout(context)
        container.addView(progressBar)
        val padding = getDialogPadding(context)
        container.setPadding(
            padding, 0, padding, 0
        )
        setView(container)
    }

    private fun getDialogPadding(context: Context): Int {
        val sizeAttr = intArrayOf(android.R.attr.dialogPreferredPadding)
        val a = context.obtainStyledAttributes(TypedValue().data, sizeAttr)
        val size = a.getDimensionPixelSize(0, -1)
        a.recycle()
        return size
    }
}