package com.example.quizzicat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.quizzicat.Exceptions.AbstractException
import com.example.quizzicat.Exceptions.EmptyFieldsException
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private var mFirebaseAuth: FirebaseAuth? = null

    private var password: String? = null
    private var email: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mFirebaseAuth = FirebaseAuth.getInstance()

        checkUserSession()

        redirect_register_link.setOnClickListener {
            val registerIntent = Intent(this, RegisterActivity::class.java)
            startActivity(registerIntent)
        }

        login_button.setOnClickListener {
            try {
                login_progress_bar.visibility = View.VISIBLE
                bindData()
                checkFieldsEmpty()
                loginUserWithEmailAndPassword()
            } catch (ex : AbstractException) {
                ex.displayMessageWithSnackbar(window.decorView.rootView, this)
            }
        }
    }

    private fun checkUserSession() {
        val currentUser = mFirebaseAuth!!.currentUser
        if (currentUser != null && currentUser.isEmailVerified) {
            val mainMenuIntent = Intent(this, MainMenuActivity::class.java)
            startActivity(mainMenuIntent)
        }
    }

    private fun loginUserWithEmailAndPassword() {
        mFirebaseAuth!!.signInWithEmailAndPassword(email!!, password!!)
            .addOnCompleteListener {
                login_progress_bar.visibility = View.GONE
                if (it.isSuccessful) {
                    if (mFirebaseAuth!!.currentUser!!.isEmailVerified) {
                        val mainMenuIntent = Intent(this, MainMenuActivity::class.java)
                        startActivity(mainMenuIntent)
                    } else {
                        DesignUtils.showSnackbar(window.decorView, "Please verify your e-mail address!", this)
                    }
                } else {
                    DesignUtils.showSnackbar(window.decorView.rootView, it.exception?.message.toString(), this)
                }
            }
    }

    private fun bindData() {
        password = login_password.text.toString()
        email = login_email.text.toString()
    }

    private fun checkFieldsEmpty() {
        if (password!!.isEmpty() || email!!.isEmpty())
            throw EmptyFieldsException()
    }
}
