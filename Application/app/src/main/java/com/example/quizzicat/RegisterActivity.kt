package com.example.quizzicat

import android.app.Activity
import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.HtmlCompat
import com.example.quizzicat.Exceptions.AbstractException
import com.example.quizzicat.Exceptions.EmptyFieldsException
import com.example.quizzicat.Exceptions.UnmatchedPasswordsException
import com.example.quizzicat.Model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*

class RegisterActivity : AppCompatActivity() {

    private var mWebView: WebView? = null

    private var mFirebaseAuth: FirebaseAuth? = null
    private var mFirebaseStorage: FirebaseStorage? = null
    private var mFirebaseDatabase: FirebaseDatabase? = null

    private var password: String? = null
    private var repeatedPassword: String? = null
    private var email: String? = null
    private var displayName: String? = null

    private var selectedPhotoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        mFirebaseAuth = FirebaseAuth.getInstance()
        mFirebaseStorage = FirebaseStorage.getInstance()
        mFirebaseDatabase = FirebaseDatabase.getInstance()

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

        register_avatar_civ.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

        register_avatar_remove.setOnClickListener {
            register_avatar_remove.visibility = View.GONE
            Log.d("RegisterActivity", "Remove button has been pressed")
            selectedPhotoUri = null
            register_avatar_civ.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.default_icon, null))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            register_avatar_remove.visibility = View.VISIBLE
            setSelectedAvatar(data)
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
                                uploadAvatarToFirebaseStorage()
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

    private fun setSelectedAvatar(data: Intent) {
        selectedPhotoUri = data.data
        val source = ImageDecoder.createSource(this.contentResolver, selectedPhotoUri!!)
        val bitmap = ImageDecoder.decodeBitmap(source)
        register_avatar_civ.setImageBitmap(bitmap)
    }

    private fun uploadAvatarToFirebaseStorage() {
        if (selectedPhotoUri == null) {
            // if no profile picture has been uploaded just set the default picture for the user
            saveUserToFirebaseDatabase("https://firebasestorage.googleapis.com/v0/b/quizzicat-af605.appspot.com/o/Avatars%2Fdefault_icon.png?alt=media&token=9c0aaa26-f86e-4f76-a7fc-f2b2c80011c5")
            return
        }
        val filename = UUID.randomUUID().toString()
        val ref = mFirebaseStorage!!.getReference("/Avatars/$filename")
        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener {
                    saveUserToFirebaseDatabase(it.toString())
                }
            }
            .addOnFailureListener {
                DesignUtils.showSnackbar(window.decorView.rootView, it.message!!, this)
            }
    }

    private fun saveUserToFirebaseDatabase(profileImageURL: String) {
        val uid = mFirebaseAuth!!.uid ?: ""
        val ref = mFirebaseDatabase!!.getReference("/Users/$uid")
        val user = User(uid, displayName!!, profileImageURL)
        ref.setValue(user)
    }
}
