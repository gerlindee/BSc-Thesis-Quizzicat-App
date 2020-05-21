package com.example.quizzicat.Fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.quizzicat.LoginActivity

import com.example.quizzicat.R
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_settings.*

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
    }

}
