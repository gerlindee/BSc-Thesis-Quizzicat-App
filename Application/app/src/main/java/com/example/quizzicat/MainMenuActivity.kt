package com.example.quizzicat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import com.example.quizzicat.Adapters.MainMenuViewPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainMenuActivity : AppCompatActivity() {

    private val mTabsTitles = ArrayList<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        setTitlesForTabs()

        val menuViewPager = findViewById<ViewPager2>(R.id.main_menu_viewpager)
        menuViewPager.adapter = MainMenuViewPagerAdapter(this)

        val menuTabLayout = findViewById<TabLayout>(R.id.main_menu_tabs_layout)
        TabLayoutMediator(menuTabLayout, menuViewPager) { tab, position ->
            tab.icon = getDrawable(mTabsTitles[position])
            menuViewPager.setCurrentItem(tab.position, true)
        }.attach()
    }

    fun setTitlesForTabs() {
        mTabsTitles.add(R.drawable.single_player_tab)
        mTabsTitles.add(R.drawable.multiplayer_tab)
        mTabsTitles.add(R.drawable.questions_tab)
        mTabsTitles.add(R.drawable.profile_tab)
        mTabsTitles.add(R.drawable.settings_tab)
    }
}
