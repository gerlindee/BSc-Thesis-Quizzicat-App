package com.example.quizzicat

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.quizzicat.Facades.ImageLoadingFacade
import com.example.quizzicat.Facades.UserDataRetrievalFacade
import com.example.quizzicat.Model.User
import com.example.quizzicat.Utils.DesignUtils
import com.example.quizzicat.Utils.UserDataCallBack
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_change_user_profile.*
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*

class ChangeUserProfileActivity : AppCompatActivity() {

    private var mFirebaseAuth: FirebaseAuth? = null
    private var mFirebaseStorage: FirebaseStorage? = null
    private var mFirestoreDatabase: FirebaseFirestore? = null
    private var originalUserData: User? = null

    private var avatarImageView: CircleImageView? = null
    private var nameTextView: TextInputEditText? = null
    private var emailTextView: TextInputEditText? = null
    private var passwordTextLayout: TextInputLayout? = null
    private var passwordTextView: TextInputEditText? = null
    private var saveChangesButton: MaterialButton? = null

    private var selectedPhotoUri: Uri? = null
    private var pictureWasUpdated = false

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
                    ImageLoadingFacade(applicationContext).loadImageIntoCircleView(value.avatar_url, avatarImageView!!)
                    nameTextView!!.setText(value.display_name)
                    emailTextView!!.setText(mFirebaseAuth!!.currentUser!!.email)
                }
            })

        button_change_name.setOnClickListener {
            val wasEnabled = nameTextView!!.isEnabled
            nameTextView!!.isEnabled = !wasEnabled
            enableConfirmationAndSaving()
        }

        button_change_email.setOnClickListener {
            if (mFirebaseAuth!!.currentUser!!.providerData[1].providerId == "facebook.com" ||
                mFirebaseAuth!!.currentUser!!.providerData[1].providerId == "google.com" ) {
                DesignUtils.showSnackbar(window.decorView.rootView, "Can not change e-mail address for Facebook or Google accounts!", this)
            } else {
                val wasEnabled = emailTextView!!.isEnabled
                emailTextView!!.isEnabled = !wasEnabled
                enableConfirmationAndSaving()
            }
        }

        change_icon.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

        change_icon_remove.setOnClickListener {
            change_icon_remove.visibility = View.GONE
            selectedPhotoUri = null
            ImageLoadingFacade(applicationContext).loadImageIntoCircleView(originalUserData!!.avatar_url, avatarImageView!!)
            enableConfirmationAndSaving()
        }

        change_confirm_button.setOnClickListener {
            if(passwordTextView!!.text!!.isEmpty() && wereChangesMade()) {
                DesignUtils.showSnackbar(window.decorView.rootView, "Please provide the password associated with your account in order to confirm the account changes!", this)
            } else {
                update_profile_progress_bar.visibility = View.VISIBLE
                val currentUser = mFirebaseAuth!!.currentUser!!
                val confirmedPassword = passwordTextView!!.text.toString()
                val credential = EmailAuthProvider.getCredential(currentUser.email!!, confirmedPassword)
                currentUser.reauthenticate(credential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            uploadAvatarToFirebaseStorage()
                            if (emailTextView!!.text.toString() != currentUser.email) {
                                updateEmailAddress()
                            }
                        } else {
                            update_profile_progress_bar.visibility = View.GONE
                            DesignUtils.showSnackbar(window.decorView.rootView, task.exception!!.message.toString(), this)
                        }
                    }
            }
        }
    }

    private fun wereChangesMade() : Boolean {
        return selectedPhotoUri != null ||
               emailTextView!!.text.toString() != mFirebaseAuth!!.currentUser!!.email ||
               nameTextView!!.text.toString()  != originalUserData!!.display_name
    }

    private fun setupLayoutElements() {
        avatarImageView = findViewById(R.id.change_icon)
        nameTextView = findViewById(R.id.change_name)
        emailTextView = findViewById(R.id.change_email)
        passwordTextLayout = findViewById(R.id.input_layout_confirm_password)
        passwordTextView = findViewById(R.id.change_confirm_password)
        saveChangesButton = findViewById(R.id.change_confirm_button)
    }

    private fun enableConfirmationAndSaving() {
        if (passwordTextLayout!!.visibility == View.GONE) {
            passwordTextLayout!!.visibility = View.VISIBLE
            saveChangesButton!!.visibility = View.VISIBLE
        } else {
            if (!wereChangesMade()) {
                passwordTextLayout!!.visibility = View.GONE
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

    private fun uploadAvatarToFirebaseStorage() {
        if (selectedPhotoUri == null) {
            updateUserInformation(originalUserData!!.avatar_url)
            return
        }
        val filename = UUID.randomUUID().toString()
        val ref = mFirebaseStorage!!.getReference("/Avatars/$filename")
        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener {
                    updateUserInformation(it.toString())
                }
            }
            .addOnFailureListener {
                update_profile_progress_bar.visibility = View.GONE
                DesignUtils.showSnackbar(window.decorView.rootView, it.message!!, this)
            }
    }

    private fun updateUserInformation(profilePictureURL: String) {
        val updatedName = nameTextView!!.text.toString()
        val updatedUser = User(originalUserData!!.uid, updatedName, profilePictureURL, originalUserData!!.country, originalUserData!!.city)
        mFirestoreDatabase!!.collection("Users")
            .document(originalUserData!!.uid)
            .set(updatedUser)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    update_profile_progress_bar.visibility = View.GONE
                    pictureWasUpdated = true
                    DesignUtils.showSnackbar(window.decorView.rootView, "User profile successfully updated", this)
                } else {
                    update_profile_progress_bar.visibility = View.GONE
                    DesignUtils.showSnackbar(window.decorView.rootView, task.exception!!.message!!, this)
                }
            }
    }

    private fun updateEmailAddress() {
        mFirebaseAuth!!.currentUser!!.updateEmail(emailTextView!!.text.toString())
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    DesignUtils.showSnackbar(window.decorView.rootView, task.exception!!.message!!, this)
                } else {
                    AlertDialog.Builder(this)
                        .setTitle("E-mail changed")
                        .setMessage("The address associated with this account has been changed. Please verify your new e-mail address. If you do not receive an account validation e-mail, please contact the support team.")
                        .setPositiveButton("Ok", null)
                        .create()
                        .show()
                }
            }
    }

    override fun onBackPressed() {
        if (pictureWasUpdated) {
            val mainMenuIntent = Intent(this, MainMenuActivity::class.java)
            startActivity(mainMenuIntent)
        }
        super.onBackPressed()
    }

}
