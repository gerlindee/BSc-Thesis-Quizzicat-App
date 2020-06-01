package com.example.quizzicat.Exceptions

import android.content.Context
import android.view.View
import android.widget.Toast
import com.example.quizzicat.Utils.DesignUtils

abstract class AbstractException(message: String): Exception(message) {

    fun displayMessageWithSnackbar(view: View, context: Context) {
        DesignUtils.showSnackbar(view, message!!, context)
    }

    fun displayMessageWithToast(context: Context) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}