package com.example.quizzicat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main_menu.*

class MainMenuActivity : AppCompatActivity() {

    private var mFirebaseAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        mFirebaseAuth = FirebaseAuth.getInstance()

        sign_out_button.setOnClickListener {
            mFirebaseAuth!!.signOut()
            LoginManager.getInstance().logOut();
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
        }
    }
}
