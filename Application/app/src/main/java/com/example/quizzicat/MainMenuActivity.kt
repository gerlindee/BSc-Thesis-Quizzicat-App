package com.example.quizzicat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.viewpager2.widget.ViewPager2
import com.example.quizzicat.Adapters.MainMenuViewPagerAdapter
import com.example.quizzicat.Facades.ImageLoadingFacade
import com.example.quizzicat.Fragments.TopicCategoriesFragment
import com.example.quizzicat.Model.User
import com.example.quizzicat.Utils.UserDataCallBack
import com.facebook.login.LoginManager
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_main_menu.*

class MainMenuActivity : AppCompatActivity() {

    private val mTabsTitles = ArrayList<Int>()
    private var mFirebaseAuth: FirebaseAuth? = null
    private var mFirestoreDatabase: FirebaseFirestore? = null

    private var menuViewPager: ViewPager2? = null
    private var profilePictureImageView: CircleImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        mFirebaseAuth = FirebaseAuth.getInstance()
        mFirestoreDatabase = Firebase.firestore
        profilePictureImageView = findViewById(R.id.avatar_display)

        setTitlesForTabs()
        setUserAvatar()

        menuViewPager = findViewById(R.id.main_menu_viewpager)
        menuViewPager!!.adapter = MainMenuViewPagerAdapter(this)

        val menuTabLayout = findViewById<TabLayout>(R.id.main_menu_tabs_layout)
        TabLayoutMediator(menuTabLayout, menuViewPager!!) { tab, position ->
            tab.icon = getDrawable(mTabsTitles[position])
            menuViewPager!!.setCurrentItem(tab.position, true)
        }.attach()

        sign_out_button.setOnClickListener {
            mFirebaseAuth!!.signOut()
            LoginManager.getInstance().logOut();
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
        }

        avatar_display.setOnClickListener {
            val statisticsIntent = Intent(this, UserStatisticsActivity::class.java)
            startActivity(statisticsIntent)
        }
    }

    private fun setTitlesForTabs() {
        mTabsTitles.add(R.drawable.single_player_tab)
        mTabsTitles.add(R.drawable.multiplayer_tab)
        mTabsTitles.add(R.drawable.questions_tab)
        mTabsTitles.add(R.drawable.recommendations_tab)
        mTabsTitles.add(R.drawable.settings_tab)
    }

    private fun getUserDetails(callback: UserDataCallBack) {
        mFirestoreDatabase!!.collection("Users")
            .whereEqualTo("uid", mFirebaseAuth!!.currentUser!!.uid)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    var user: User ? = null
                    for (document in task.result!!) {
                        val userUID = document.get("uid") as String
                        val userCity = document.get("city") as String
                        val userCountry = document.get("country") as String
                        val userDisplayName = document.get("displayName") as String
                        val userPicture = document.get("profileImageURL") as String
                        user = User(userUID, userDisplayName, userPicture, userCountry, userCity)
                    }
                    callback.onCallback(user!!)
                }
            }
    }

    private fun setUserAvatar() {
        getUserDetails(object: UserDataCallBack {
            override fun onCallback(value: User) {
                ImageLoadingFacade(this@MainMenuActivity).loadImageIntoCircleView(value.profileImageURL, profilePictureImageView!!)
            }
        })
    }

    override fun onBackPressed() {
        // this is empty so that it does not by default go back to LoginActivity
    }
}
