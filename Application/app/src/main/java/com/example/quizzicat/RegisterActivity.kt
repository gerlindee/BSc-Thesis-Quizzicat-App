package com.example.quizzicat

import android.os.Bundle
import android.view.View
import android.webkit.WebView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {

    private var mWebView: WebView? = null
    private var mFirebaseAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        mFirebaseAuth = FirebaseAuth.getInstance()
        mWebView = WebView(this)

        val termsOfServiceMessage = "I have read and therefore agree with the " + "<u>" + "Terms of Service" + "</u>" + "."
        text_terms_service.text = HtmlCompat.fromHtml(termsOfServiceMessage, HtmlCompat.FROM_HTML_MODE_LEGACY)

        checkbox_terms_service.setOnCheckedChangeListener { _, isChecked ->
            register_button.isEnabled = isChecked
        }

        text_terms_service.setOnClickListener {
            setContentView(mWebView)
            mWebView?.loadUrl("https://www.websitepolicies.com/policies/view/FVj4pExJ")
        }

        register_button.setOnClickListener {
            val password = register_password.text.toString()
            val repeatedPassword = register_r_password.text.toString()
            val email = register_email.text.toString()
            val displayname = register_username.text.toString()

            if (password.isEmpty() || repeatedPassword.isEmpty() || email.isEmpty() || displayname.isEmpty()) {
                DesignUtils.showSnackbar(window.decorView.rootView, "Please fill in all the fields!", this)
            } else {
                if (password != repeatedPassword) {
                    DesignUtils.showSnackbar(window.decorView.rootView, "Passwords must match!", this)
                } else {
                    register_progress_bar.visibility = View.VISIBLE

                    mFirebaseAuth!!.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this) {
                            if (it.isSuccessful) {
                                mFirebaseAuth!!.currentUser?.sendEmailVerification()
                                    ?.addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            register_progress_bar.visibility = View.GONE
                                            AlertDialog.Builder(this)
                                                .setTitle("Success")
                                                .setMessage("Account created successfully! To complete the registration process, please verify your e-mail address. Otherwise, you will not be able to access your Quizzicat account! If you do not receive a verification e-mail, please contact the support team.")
                                                .setPositiveButton("Confirm", null)
                                                .show()
                                        } else {
                                            DesignUtils.showSnackbar(window.decorView.rootView, it.exception?.message.toString(), this)
                                        }
                                    }
                            } else {
                                register_progress_bar.visibility = View.GONE
                                DesignUtils.showSnackbar(window.decorView.rootView, it.exception?.message.toString(), this)
                            }
                        }
                }
            }
        }
    }
}