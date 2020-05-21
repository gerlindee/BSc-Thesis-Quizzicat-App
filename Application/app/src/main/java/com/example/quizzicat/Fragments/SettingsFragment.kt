package com.example.quizzicat.Fragments

import androidx.appcompat.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.quizzicat.LoginActivity

import com.example.quizzicat.R
import com.example.quizzicat.Utils.DesignUtils
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.android.synthetic.main.transaction_password_reset.view.*

class SettingsFragment : Fragment() {

    private var mFirebaseAuth: FirebaseAuth? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mFirebaseAuth = FirebaseAuth.getInstance()

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
                        if (resetEmail.isEmpty()) {
                            Toast.makeText(context, "You must provide the e-mail address associated with your account in order to confirm change the password!", Toast.LENGTH_LONG).show()
                        } else if (resetEmail != actualEmail) {
                            Toast.makeText(context, "Provided confirmation e-mail does not match account e-mail address!", Toast.LENGTH_LONG).show()
                        } else {
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
                .setNegativeButton("Cancel", null)
                .create()
                .show()
        }
    }

}
