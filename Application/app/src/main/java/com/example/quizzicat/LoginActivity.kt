package com.example.quizzicat

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.example.quizzicat.Exceptions.AbstractException
import com.example.quizzicat.Exceptions.EmptyFieldsException
import com.example.quizzicat.Utils.DesignUtils
import com.example.quizzicat.Utils.NetworkUtils
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.transaction_password_reset.view.*

class LoginActivity : AppCompatActivity() {

    private var mFirebaseAuth: FirebaseAuth? = null
    private var mFirebaseStorage: FirebaseStorage? = null
    private var mFirebaseDatabase: FirebaseDatabase? = null

    private var mGoogleSignInClient: GoogleSignInClient? = null

    private var password: String? = null
    private var email: String? = null

    private lateinit var mCallbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mFirebaseAuth = FirebaseAuth.getInstance()
        mFirebaseStorage = FirebaseStorage.getInstance()
        mFirebaseDatabase = FirebaseDatabase.getInstance()

        isOnline()

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
                firebaseAuthWithEmailAndPassword()
            } catch (ex : AbstractException) {
                ex.displayMessageWithSnackbar(window.decorView.rootView, this)
            }
        }

        login_forgot_password.setOnClickListener {
            val inflater = LayoutInflater.from(this)
            val dialogView = inflater.inflate(R.layout.transaction_password_reset, null)

            AlertDialog.Builder(this)
                .setTitle("Reset password")
                .setView(dialogView)
                .setPositiveButton("Confirm", DialogInterface.OnClickListener{
                        _, _ ->
                    run {
                        val resetEmail = dialogView.reset_email.text.toString()
                        if (resetEmail.isEmpty()) {
                            DesignUtils.showSnackbar(window.decorView.rootView, "Please provide the e-mail address associated with your account", this)
                        } else {
                            login_progress_bar.visibility = View.VISIBLE
                            mFirebaseAuth!!.sendPasswordResetEmail(resetEmail)
                                .addOnCompleteListener {
                                    login_progress_bar.visibility = View.GONE
                                    if (it.isSuccessful) {
                                        DesignUtils.showSnackbar(window.decorView.rootView, "Password reset e-mail was sent", this)
                                    } else {
                                        DesignUtils.showSnackbar(window.decorView.rootView, it.exception!!.message.toString(), this)
                                    }
                                }
                        }
                    }
                })
                .setNegativeButton("Cancel", DialogInterface.OnClickListener{ _, _ -> })
                .create()
                .show()
        }

        login_facebook_icon.setOnClickListener {
            // forces the onClick handler of the button to execute, even though the button itself is hidden
            login_facebook_button.performClick()
        }

        login_google_icon.setOnClickListener {
            signInWithGoogle()
        }

        initializeFacebookLogin()

        initializeGoogleLogin()
    }

    // ------------------------------ Google Login Methods ----------------------------- //

    private fun signInWithGoogle() {
        val googleSignInIntent = mGoogleSignInClient!!.signInIntent
        // TODO: for code cleanup, put the request code into a local constant
        startActivityForResult(googleSignInIntent, 1)
    }

    private fun initializeGoogleLogin() {
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)
    }

    private fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account!!)
        } catch (e: ApiException) {
            login_progress_bar.visibility = View.GONE
            DesignUtils.showSnackbar(window.decorView.rootView, "Google authentication failed!", this)
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        mFirebaseAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(this) {
                login_progress_bar.visibility = View.GONE
                if (it.isSuccessful) {
                    checkUserSession()
                } else {
                    DesignUtils.showSnackbar(window.decorView.rootView, it.exception!!.message.toString(), this)
                }
            }
    }

    // ------------------------------ Facebook Login Methods ----------------------------- //

    private fun initializeFacebookLogin() {
        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create()

        login_facebook_button.setPermissions("email", "public_profile")
        login_facebook_button.registerCallback(mCallbackManager, object :
            FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                login_progress_bar.visibility = View.VISIBLE
                handleFacebookAccessToken(loginResult.accessToken)
            }

            override fun onCancel() {

            }

            override fun onError(error: FacebookException) {

            }
        })
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        mFirebaseAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    checkUserSession()
                    login_progress_bar.visibility = View.GONE
                } else {
                    // If sign in fails, display a message to the user.
                    DesignUtils.showSnackbar(window.decorView.rootView, task.exception!!.message.toString(), this)
                }
            }
    }

    // -------------------------------------------------------------------------------- //

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1) { // signing in with google
            login_progress_bar.visibility = View.VISIBLE
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleGoogleSignInResult(task)
        } else { // signing in with facebook
            // Pass the activity result back to the Facebook SDK
            mCallbackManager.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun checkUserSession() {
        val currentUser = mFirebaseAuth!!.currentUser
        if (currentUser != null) {
            if ( ( currentUser.providerData[1].providerId == "password" && currentUser.isEmailVerified ) || // only allow the user to log in with e-mail and password if the account is verified
                   currentUser.providerData[1].providerId != "password") { // for facebook + google sign in
                val mainMenuIntent = Intent(this, MainMenuActivity::class.java)
                startActivity(mainMenuIntent)
            }
        }
    }

    private fun firebaseAuthWithEmailAndPassword() {
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

    // retrieves the values from the input fields
    private fun bindData() {
        password = login_password.text.toString()
        email = login_email.text.toString()
    }

    // checks if the user has filled all the input fields on the screen
    private fun checkFieldsEmpty() {
        if (password!!.isEmpty() || email!!.isEmpty())
            throw EmptyFieldsException()
    }

    private fun isOnline() {
        val connection = NetworkUtils.getConnectivityStatus(this)
        if (connection == NetworkUtils.TYPE_NO_CONNECTION) {
            val noInternetIntent = Intent(this, NoInternetConnectionActivity::class.java)
            startActivity(noInternetIntent)
        }
    }
}