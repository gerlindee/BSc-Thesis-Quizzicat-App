package com.example.quizzicat.Fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.contains
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quizzicat.Adapters.RecommendedTopicsAdapter
import com.example.quizzicat.Facades.RecommendDataFacade
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
    private var playedTopicsLabel: TextView? = null
    private var similarTopics1: RecyclerView? = null
    private var similarTopics1Label: TextView? = null
    private var similarTopics2: RecyclerView? = null
    private var similarTopics2Label: TextView? = null
    private var similarTopics3: RecyclerView? = null
    private var similarTopics3Label: TextView? = null
    private var topicsPlayedData = ArrayList<Topic>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_recommendations, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mFirestoreDatabase = Firebase.firestore

        setupLayoutElements()

        setupRecommendations()
    }

    private fun setupLayoutElements() {
        playedTopics = view?.findViewById(R.id.recommend_again_topics)
        playedTopicsLabel = view?.findViewById(R.id.recommend_again_text)
        similarTopics1 = view?.findViewById(R.id.recommend_similar_1_topics)
        similarTopics2 = view?.findViewById(R.id.recommend_similar_2_topics)
        similarTopics3 = view?.findViewById(R.id.recommend_similar_3_topics)
        similarTopics1Label = view?.findViewById(R.id.recommend_similar_1_text)
        similarTopics2Label = view?.findViewById(R.id.recommend_similar_2_text)
        similarTopics3Label = view?.findViewById(R.id.recommend_similar_3_text)
    }

    private fun setupRecommendations() {
        val topicsDataRetrievalFacade = TopicsDataRetrievalFacade(mFirestoreDatabase!!, context!!)

        topicsDataRetrievalFacade.getUserPlayedHistory(object : TopicsPlayedCallBack {
            override fun onCallback(value: List<TopicPlayed>) {
                val topicsHistory = value as ArrayList<TopicPlayed>
                if (topicsHistory.isNotEmpty()) {
                    topicsDataRetrievalFacade.getTopicsPlayedData(object : CustomCallBack {
                        override fun onCallback(value: List<AbstractTopic>) {
                            topicsPlayedData = value as ArrayList<Topic>
                            playedTopics!!.apply {
                                layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
                                adapter = RecommendedTopicsAdapter(context!!, topicsPlayedData)
                            }
                            playedTopicsLabel!!.visibility = View.VISIBLE
                            playedTopics!!.visibility = View.VISIBLE
                            getSimilarTopics()
                        }
                    }, topicsHistory)
                }
            }
        })
    }

    private fun setupSimilarLayout1(tid: Long, name: String, recyclerView: RecyclerView?, label: TextView?) {
        RecommendDataFacade(mFirestoreDatabase!!, activity!!)
            .getRelatedTopics(tid, topicsPlayedData, object: CustomCallBack {
                override fun onCallback(value: List<AbstractTopic>) {
                    label!!.apply {
                        text = "You seem to like " + name
                        visibility = View.VISIBLE
                    }
                    recyclerView!!.apply {
                        layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
                        adapter = RecommendedTopicsAdapter(context!!, value as ArrayList<Topic>)
                        visibility = View.VISIBLE
                    }
                }
            })
    }

    private fun getSimilarTopics() {
        val topicsDataRetrievalFacade = TopicsDataRetrievalFacade(mFirestoreDatabase!!, context!!)

        topicsDataRetrievalFacade.getMostPlayedTopics(object: CustomCallBack {
            override fun onCallback(value: List<AbstractTopic>) {
                val mostPlayedTopics = value as ArrayList<Topic>
                when (mostPlayedTopics.size) {
                    1 -> setupSimilarLayout1(mostPlayedTopics[0].tid, mostPlayedTopics[0].name, similarTopics1, similarTopics1Label)
                    2 -> {
                        setupSimilarLayout1(mostPlayedTopics[0].tid, mostPlayedTopics[0].name, similarTopics1, similarTopics1Label)
                        setupSimilarLayout1(mostPlayedTopics[1].tid, mostPlayedTopics[1].name, similarTopics2, similarTopics2Label)
                    }
                    3 -> {
                        setupSimilarLayout1(mostPlayedTopics[0].tid, mostPlayedTopics[0].name, similarTopics1, similarTopics1Label)
                        setupSimilarLayout1(mostPlayedTopics[1].tid, mostPlayedTopics[1].name, similarTopics2, similarTopics2Label)
                        setupSimilarLayout1(mostPlayedTopics[3].tid, mostPlayedTopics[3].name, similarTopics3, similarTopics3Label)
                    }
                }

            }
        })
    }
}
