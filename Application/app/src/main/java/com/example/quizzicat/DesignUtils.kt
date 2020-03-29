package com.example.quizzicat

import android.content.Context
import android.util.DisplayMetrics
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar

public class DesignUtils {
    // customized snackbar
    companion object { // static method in Kotlin
        fun showSnackbar(view: View, text: String, context: Context) {
            val snackbar = Snackbar.make(view, text, Snackbar.LENGTH_LONG)
            snackbar.view.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent))
            val layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
//            layoutParams.setMargins(0, 1280, 0, 0)
//            snackbar.view.layoutParams = layoutParams
            snackbar.show()
        }
    }
}