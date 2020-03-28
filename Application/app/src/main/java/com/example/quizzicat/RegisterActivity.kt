package com.example.quizzicat

import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        register_button.setOnClickListener {
            val password = register_password.text.toString()
            val repeatedPassword = register_r_password.text.toString()

            if (password != repeatedPassword) {
                DesignUtils.showSnackbar(window.decorView.rootView, "Passwords must match!", this)
            }
        }
    }
}