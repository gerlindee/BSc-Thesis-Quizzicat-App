package com.example.quizzicat.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.example.quizzicat.R

class TopicCategoriesFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the categories layout for this fragment
        return inflater.inflate(R.layout.fragment_topic_categories, container, false)
    }

}
