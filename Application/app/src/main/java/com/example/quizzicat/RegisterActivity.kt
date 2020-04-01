package com.example.quizzicat

import android.app.Activity
import android.content.Intent
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.ActionMode
import android.view.View
import android.webkit.WebView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import com.example.quizzicat.Exceptions.AbstractException
import com.example.quizzicat.Exceptions.EmptyFieldsException
import com.example.quizzicat.Exceptions.UnmatchedPasswordsException
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {

    private var mWebView: WebView? = null
    private var mFirebaseAuth: FirebaseAuth? = null

    private var password: String? = null
    private var repeatedPassword: String? = null
    private var email: String? = null
    private var displayName: String? = null

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
            loadTermsAndConditions()
        }

        register_button.setOnClickListener {
            try {
                bindData()
                checkFieldsEmpty()
                checkPasswordsSame()
                registerUserWithEmailPassword()
            } catch (ex: AbstractException) {
                ex.displayMessageWithSnackbar(window.decorView.rootView, this)
            }
        }

//        register_select_photo_button.setOnClickListener {
//            val intent = Intent(Intent.ACTION_PICK)
//            intent.type = "image/*"
//            startActivityForResult(intent, 0)
//        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && requestCode == Activity.RESULT_OK && data != null) {
            val selectedPhotoUri = data.data
            try {
                selectedPhotoUri?.let {
                    if(Build.VERSION.SDK_INT < 28) {
                        val selectedPhotoBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, selectedPhotoUri)
                        val bitmapDrawable = BitmapDrawable()
//                        register_select_photo_button.icon = bitmapDrawable
                    } else {
                        val source = ImageDecoder.createSource(this.contentResolver, selectedPhotoUri)
                        val bitmap = ImageDecoder.decodeBitmap(source)
                        val bitmapDrawable = BitmapDrawable(resources, bitmap)
//                        register_select_photo_button.setBackgroundDrawable(bitmapDrawable)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun bindData() {
        password = register_password.text.toString()
        repeatedPassword = register_r_password.text.toString()
        email = register_email.text.toString()
        displayName = register_username.text.toString()
    }

    private fun loadTermsAndConditions() {
        setContentView(mWebView)
        mWebView?.loadUrl("https://www.websitepolicies.com/policies/view/FVj4pExJ")
    }

    private fun checkFieldsEmpty() {
        if (password!!.isEmpty() || repeatedPassword!!.isEmpty() || email!!.isEmpty() || displayName!!.isEmpty())
            throw EmptyFieldsException()
    }

    private fun checkPasswordsSame() {
        if (password != repeatedPassword)
            throw UnmatchedPasswordsException()
    }

    private fun registerUserWithEmailPassword() {
        register_progress_bar.visibility = View.VISIBLE

        mFirebaseAuth!!.createUserWithEmailAndPassword(email!!, password!!)
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
