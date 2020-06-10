package com.example.quizzicat.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.contains
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quizzicat.Adapters.RecommendedTopicsAdapter
import com.example.quizzicat.Facades.TopicsDataRetrievalFacade
import com.example.quizzicat.Model.AbstractTopic
import com.example.quizzicat.Model.Topic
import com.example.quizzicat.Model.TopicPlayed

import com.example.quizzicat.R
import com.example.quizzicat.Utils.CustomCallBack
import com.example.quizzicat.Utils.TopicsPlayedCallBack
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RecommendationsFragment : Fragment() {

    private var mFirestoreDatabase: FirebaseFirestore? = null

    private var playedTopics: RecyclerView? = null
    private var notPlayedTopics: RecyclerView? = null
    private var topicsPlayedData = ArrayList<Topic>()
    private var topicsNotPlayedData = ArrayList<Topic>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_recommendations, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mFirestoreDatabase = Firebase.firestore

        setupLayoutElements()

        getTopicsPlayed()
        getTopicsNotPlayed()

    }

    private fun setupLayoutElements() {
        playedTopics = view?.findViewById(R.id.recommend_again_topics)
        notPlayedTopics = view?.findViewById(R.id.recommend_different_topics)
    }

    private fun getTopicsPlayed() {
        val topicsDataRetrievalFacade = TopicsDataRetrievalFacade(mFirestoreDatabase!!, context!!)

        topicsDataRetrievalFacade.getUserPlayedHistory(object : TopicsPlayedCallBack {
            override fun onCallback(value: List<TopicPlayed>) {
                val topicsHistory = value as ArrayList<TopicPlayed>
                if (topicsHistory.isEmpty()) {
                    // todo later
                    // display some stuff
                } else {
                    topicsDataRetrievalFacade.getTopicsPlayedData(object : CustomCallBack {
                        override fun onCallback(value: List<AbstractTopic>) {
                            topicsPlayedData = value as ArrayList<Topic>
                            playedTopics!!.apply {
                                layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
                                adapter = RecommendedTopicsAdapter(context!!, topicsPlayedData)
                            }
                        }
                    }, topicsHistory)
                }
            }
        })
    }

    private fun getTopicsNotPlayed() {
        val topicsDataRetrievalFacade = TopicsDataRetrievalFacade(mFirestoreDatabase!!, context!!)

        topicsDataRetrievalFacade.getUserPlayedHistory(object : TopicsPlayedCallBack {
            override fun onCallback(value: List<TopicPlayed>) {
                val topicsHistory = value as ArrayList<TopicPlayed>
                topicsDataRetrievalFacade.getTopicsNotPlayed(object : CustomCallBack {
                    override fun onCallback(value: List<AbstractTopic>) {
                        topicsNotPlayedData = value as ArrayList<Topic>
                        notPlayedTopics!!.apply {
                            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
                            adapter = RecommendedTopicsAdapter(context!!, topicsNotPlayedData)
                        }
                    }
                }, topicsHistory)
            }
        })
    }
}
