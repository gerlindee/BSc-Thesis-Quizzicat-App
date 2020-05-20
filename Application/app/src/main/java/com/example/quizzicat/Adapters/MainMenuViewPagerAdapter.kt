package com.example.quizzicat.Adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.quizzicat.Fragments.*

class MainMenuViewPagerAdapter(fragmentActivity: FragmentActivity): FragmentStateAdapter(fragmentActivity) {
    private val NUMBER_OF_TABS = 5

    override fun getItemCount(): Int {
        return NUMBER_OF_TABS
    }

    override fun createFragment(position: Int): Fragment {
        when(position) {
            0 -> return TopicCategoriesFragment()
            1 -> return MultiPlayerMenuFragment()
            2 -> return QuestionsLeaderboardFragment()
            3 -> return RecommendationsFragment()
            4 -> return SettingsFragment()
        }
        return TopicCategoriesFragment()
    }

}