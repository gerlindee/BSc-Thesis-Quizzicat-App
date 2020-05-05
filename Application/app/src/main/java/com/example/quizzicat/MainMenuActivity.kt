package com.example.quizzicat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import com.example.quizzicat.Adapters.MainMenuViewPagerAdapter
import com.facebook.login.LoginManager
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main_menu.*

class MainMenuActivity : AppCompatActivity() {

    private val mTabsTitles = ArrayList<Int>()
    private var mFirebaseAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        mFirebaseAuth = FirebaseAuth.getInstance()

        setTitlesForTabs()

        val menuViewPager = findViewById<ViewPager2>(R.id.main_menu_viewpager)
        menuViewPager.adapter = MainMenuViewPagerAdapter(this)

        val menuTabLayout = findViewById<TabLayout>(R.id.main_menu_tabs_layout)
        TabLayoutMediator(menuTabLayout, menuViewPager) { tab, position ->
            tab.icon = getDrawable(mTabsTitles[position])
            menuViewPager.setCurrentItem(tab.position, true)
        }.attach()

        sign_out_button.setOnClickListener {
            mFirebaseAuth!!.signOut()
            LoginManager.getInstance().logOut();
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
        }
    }

    private fun setTitlesForTabs() {
        mTabsTitles.add(R.drawable.single_player_tab)
        mTabsTitles.add(R.drawable.multiplayer_tab)
        mTabsTitles.add(R.drawable.questions_tab)
        mTabsTitles.add(R.drawable.profile_tab)
        mTabsTitles.add(R.drawable.settings_tab)
    }
}
