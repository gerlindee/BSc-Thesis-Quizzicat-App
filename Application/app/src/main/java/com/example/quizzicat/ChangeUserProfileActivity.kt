package com.example.quizzicat

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.quizzicat.Facades.ImageLoadingFacade
import com.example.quizzicat.Facades.UserDataRetrievalFacade
import com.example.quizzicat.Model.User
import com.example.quizzicat.Utils.UserDataCallBack
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_change_user_profile.*
import kotlinx.android.synthetic.main.activity_register.*

class ChangeUserProfileActivity : AppCompatActivity() {

    private var mFirebaseAuth: FirebaseAuth? = null
    private var mFirebaseStorage: FirebaseStorage? = null
    private var mFirestoreDatabase: FirebaseFirestore? = null
    private var originalUserData: User? = null

    private var avatarImageView: CircleImageView? = null
    private var nameTextView: TextInputEditText? = null
    private var emailTextView: TextInputEditText? = null
    private var passwordTextView: TextInputLayout? = null
    private var saveChangesButton: MaterialButton? = null

    private var selectedPhotoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_user_profile)

        mFirebaseAuth = FirebaseAuth.getInstance()
        mFirebaseStorage = FirebaseStorage.getInstance()
        mFirestoreDatabase = Firebase.firestore

        setupLayoutElements()

        UserDataRetrievalFacade(mFirestoreDatabase!!, mFirebaseAuth!!.currentUser!!.uid)
            .getUserDetails(object: UserDataCallBack {
                override fun onCallback(value: User) {
                    originalUserData = value
                    ImageLoadingFacade(applicationContext).loadImageIntoCircleView(value.profileImageURL, avatarImageView!!)
                    nameTextView!!.setText(value.displayName)
                    emailTextView!!.setText(mFirebaseAuth!!.currentUser!!.email)
                }
            })

        button_change_name.setOnClickListener {
            val wasEnabled = nameTextView!!.isEnabled
            nameTextView!!.isEnabled = !wasEnabled
            enableConfirmationAndSaving()
        }

        button_change_email.setOnClickListener {
            val wasEnabled = emailTextView!!.isEnabled
            emailTextView!!.isEnabled = !wasEnabled
            enableConfirmationAndSaving()
        }

        change_icon.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

        change_icon_remove.setOnClickListener {
            change_icon_remove.visibility = View.GONE
            selectedPhotoUri = null
            ImageLoadingFacade(applicationContext).loadImageIntoCircleView(originalUserData!!.profileImageURL, avatarImageView!!)
            enableConfirmationAndSaving()
        }
    }

    private fun setupLayoutElements() {
        avatarImageView = findViewById(R.id.change_icon)
        nameTextView = findViewById(R.id.change_name)
        emailTextView = findViewById(R.id.change_email)
        passwordTextView = findViewById(R.id.input_layout_confirm_password)
        saveChangesButton = findViewById(R.id.change_confirm_button)
    }

    private fun enableConfirmationAndSaving() {
        if (passwordTextView!!.visibility == View.GONE) {
            passwordTextView!!.visibility = View.VISIBLE
            saveChangesButton!!.visibility = View.VISIBLE
        } else {
            if (selectedPhotoUri == null &&
                emailTextView!!.text.toString() == mFirebaseAuth!!.currentUser!!.email &&
                nameTextView!!.text.toString()  == originalUserData!!.displayName ) {
                passwordTextView!!.visibility = View.GONE
                saveChangesButton!!.visibility = View.GONE
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            change_icon_remove.visibility = View.VISIBLE
            setSelectedAvatar(data)
            enableConfirmationAndSaving()
        }
    }

    @SuppressLint("NewApi")
    private fun setSelectedAvatar(data: Intent) {
        selectedPhotoUri = data.data
        val source = ImageDecoder.createSource(this.contentResolver, selectedPhotoUri!!)
        val bitmap = ImageDecoder.decodeBitmap(source)
        change_icon.setImageBitmap(bitmap)
    }
}
