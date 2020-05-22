package com.example.quizzicat.Fragments

import androidx.appcompat.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.quizzicat.ChangeUserProfileActivity
import com.example.quizzicat.Facades.UserDataRetrievalFacade
import com.example.quizzicat.LoginActivity
import com.example.quizzicat.Model.User

import com.example.quizzicat.R
import com.example.quizzicat.Utils.DesignUtils
import com.example.quizzicat.Utils.UserDataCallBack
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.android.synthetic.main.transaction_password_reset.view.*
import kotlinx.android.synthetic.main.view_delete_account.view.*

class SettingsFragment : Fragment() {

    private var mFirebaseAuth: FirebaseAuth? = null
    private var mFirebaseStorage: FirebaseStorage? = null
    private var mFirestoreDatabase: FirebaseFirestore? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mFirebaseAuth = FirebaseAuth.getInstance()
        mFirebaseStorage = FirebaseStorage.getInstance()
        mFirestoreDatabase = Firebase.firestore

        settings_log_out.setOnClickListener {
            mFirebaseAuth!!.signOut()
            LoginManager.getInstance().logOut();
            val loginIntent = Intent(context, LoginActivity::class.java)
            startActivity(loginIntent)
        }

        settings_reset_password.setOnClickListener {
            val inflater = LayoutInflater.from(context)
            val dialogView = inflater.inflate(R.layout.transaction_password_reset, null)

            AlertDialog.Builder(context!!)
                .setTitle("Reset password")
                .setView(dialogView)
                .setPositiveButton("Confirm") { _, _ ->
                    run {
                        val resetEmail = dialogView.reset_email.text.toString()
                        val actualEmail = mFirebaseAuth!!.currentUser!!.email as String
                        when {
                            resetEmail.isEmpty() -> {
                                Toast.makeText(context, "You must provide the e-mail address associated with your account in order to change the password!", Toast.LENGTH_LONG).show()
                            }
                            resetEmail != actualEmail -> {
                                Toast.makeText(context, "Provided confirmation e-mail does not match account e-mail address!", Toast.LENGTH_LONG).show()
                            }
                            else -> {
                                settings_progress_bar.visibility = View.VISIBLE
                                mFirebaseAuth!!.sendPasswordResetEmail(resetEmail)
                                    .addOnCompleteListener {
                                        settings_progress_bar.visibility = View.GONE
                                        if (it.isSuccessful) {
                                            Toast.makeText(context, "Password reset e-mail has been sent. Please check your inbox.", Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, it.exception!!.message.toString(), Toast.LENGTH_LONG).show()
                                        }
                                    }
                            }
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .create()
                .show()
        }

        settings_delete_account.setOnClickListener {
            val inflater = LayoutInflater.from(context)
            val dialogView = inflater.inflate(R.layout.view_delete_account, null)

            AlertDialog.Builder(context!!)
                .setView(dialogView)
                .setPositiveButton("Confirm") { _, _ ->
                    run {
                        val confirmedEmail = dialogView.delete_acc_email.text.toString()
                        val actualEmail = mFirebaseAuth!!.currentUser!!.email as String
                        when {
                            confirmedEmail.isEmpty() -> {
                                Toast.makeText(context,"You must provide the e-mail address associated with your account in order to confirm the account removal!", Toast.LENGTH_LONG).show()
                            }
                            confirmedEmail != actualEmail -> {
                                Toast.makeText(context, "Provided confirmation e-mail does not match account e-mail address!", Toast.LENGTH_LONG).show()
                            }
                            else -> {
                                settings_progress_bar.visibility = View.VISIBLE
                                deletePlayingHistory(mFirebaseAuth!!.currentUser!!.uid)
                                val photoURL = mFirebaseAuth!!.currentUser!!.photoUrl
                                if (photoURL == null) {
                                    UserDataRetrievalFacade(mFirestoreDatabase!!, mFirebaseAuth!!.currentUser!!.uid)
                                        .getUserDetails(object : UserDataCallBack {
                                            override fun onCallback(value: User) {
                                                if (value.profileImageURL == getString(R.string.default_avatar)) {
                                                    deleteDatabaseAuth()
                                                } else {
                                                    mFirebaseStorage!!.getReferenceFromUrl(value.profileImageURL).delete()
                                                        .addOnCompleteListener { task ->
                                                            if (task.isSuccessful) {
                                                                deleteDatabaseAuth()
                                                            } else {
                                                                settings_progress_bar.visibility = View.GONE
                                                                Toast.makeText(context, "Could not delete the profile picture. Please contant the support team.", Toast.LENGTH_LONG).show()
                                                            }
                                                        }
                                                }
                                            }
                                        })
                                } else {
                                    deleteDatabaseAuth()
                                }
                            }
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .create()
                .show()
        }

        settings_change_profile.setOnClickListener {
            val changeProfileIntent = Intent(context, ChangeUserProfileActivity::class.java)
            startActivity(changeProfileIntent)
        }
    }

    private fun deleteDatabaseAuth() {
        mFirestoreDatabase!!.collection("Users").document(mFirebaseAuth!!.currentUser!!.uid)
            .delete()
            .addOnCompleteListener { task1 ->
                if (task1.isSuccessful) {
                    mFirebaseAuth!!.currentUser!!.delete()
                        .addOnCompleteListener { task2 ->
                            if (task2.isSuccessful) {
                                settings_progress_bar.visibility = View.GONE
                                Toast.makeText(context, "Account was successfully deleted", Toast.LENGTH_LONG).show()
                                val loginIntent = Intent(context, LoginActivity::class.java)
                                startActivity(loginIntent)
                            } else {
                                settings_progress_bar.visibility = View.GONE
                                Toast.makeText(context, "Could not delete the account authentication! Please contact the support team.", Toast.LENGTH_LONG).show()
                            }
                        }
                } else {
                    settings_progress_bar.visibility = View.GONE
                    Toast.makeText(context, "Could not delete the account information! Please try again.", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun deletePlayingHistory(UID: String) {

    }

}
