package com.example.quizzicat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.quizzicat.Utils.NetworkUtils
import kotlinx.android.synthetic.main.activity_no_internet_connection.*

class NoInternetConnectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_no_internet_connection)

        no_internet_check_connectivity_button.setOnClickListener {
            isOnline()
        }
    }

    private fun isOnline() {
        val connection = NetworkUtils.getConnectivityStatus(this)
        if (connection != NetworkUtils.TYPE_NO_CONNECTION) {
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
        }
    }
}
