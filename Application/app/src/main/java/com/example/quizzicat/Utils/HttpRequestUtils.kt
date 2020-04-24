package com.example.quizzicat.Utils

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

// singleton pattern
class HttpRequestUtils constructor(context: Context) {
    companion object { // static
        @Volatile // annotation that signifies the fact that writes to this field are immediately made visible to other threads
        private var INSTANCE: HttpRequestUtils? = null

        fun getInstance(context: Context) =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: HttpRequestUtils(context).also {
                    INSTANCE = it
                }
            }
    }

    val requestQueue: RequestQueue by lazy {
        // application context is mandatory, since it keeps you from leaking the Activity if someone passes one in
        Volley.newRequestQueue(context.applicationContext)
    }

    fun <T> addToRequestQueue(request: Request<T>) {
        requestQueue.add(request)
    }
}