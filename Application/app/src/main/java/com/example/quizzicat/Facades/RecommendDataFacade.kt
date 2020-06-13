package com.example.quizzicat.Facades

import android.content.Context
import android.widget.Toast
import com.example.quizzicat.Model.ModelEntity
import com.example.quizzicat.Model.Topic
import com.example.quizzicat.Model.TopicsComparisonValue
import com.example.quizzicat.Utils.ModelArrayCallback
import com.google.firebase.firestore.FirebaseFirestore

class RecommendDataFacade(private val firebaseFirestore: FirebaseFirestore, private val context: Context)  {
    private fun getComparisonValues(callback: ModelArrayCallback) {
        firebaseFirestore.collection("Topics_Comparison_Values")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val topicComparisonValues = ArrayList<TopicsComparisonValue>()
                    for (document in task.result!!) {
                        val tid = document.get("tid") as Long
                        val fiction = document.get("fiction") as Boolean
                        val general_knowledge = document.get("general_knowledge") as Boolean
                        val humanities = document.get("humanities") as Boolean
                        val pop_culture = document.get("pop_culture") as Boolean
                        val real_life = document.get("real_life") as Boolean
                        val science = document.get("science") as Boolean
                        val comparisonValue = TopicsComparisonValue(tid, fiction, general_knowledge, humanities, pop_culture, real_life, science)
                        topicComparisonValues.add(comparisonValue)
                    }
                    callback.onCallback(topicComparisonValues)
                } else {
                    Toast.makeText(context, task.exception!!.message.toString(), Toast.LENGTH_LONG).show()
                }
            }
    }

    fun getRelatedTopics(tid: Long, topicsPlayed: ArrayList<Topic>, callback: ModelArrayCallback) {
        getComparisonValues(object: ModelArrayCallback {
            override fun onCallback(value: List<ModelEntity>) {
                val topics = value as ArrayList<TopicsComparisonValue>
                val comparingTopic = topics.filter { topic -> topic.tid == tid }[0]
                val topicSimilarity = HashMap<TopicsComparisonValue, Int>()
                for (topic in topics) {
                    var similarityIndex = 0
                    val topicPlayedMatch = topicsPlayed.filter { topicPlayed -> topicPlayed.tid == topic.tid }
                    if (topic.tid != comparingTopic.tid && topicPlayedMatch.isEmpty()) {
                        if (topic.fiction == comparingTopic.fiction)
                            similarityIndex += 1
                        if (topic.general_knowledge == comparingTopic.general_knowledge)
                            similarityIndex += 1
                        if (topic.humanities == comparingTopic.humanities)
                            similarityIndex += 1
                        if (topic.pop_culture == comparingTopic.pop_culture)
                            similarityIndex += 1
                        if (topic.real_life == comparingTopic.real_life)
                            similarityIndex += 1
                        if (topic.science == comparingTopic.science)
                            similarityIndex += 1
                        if (similarityIndex != 0)
                            topicSimilarity[topic] = similarityIndex
                    }
                }
                val mostSimilarTopics = ArrayList<Long>()
                for (topic in topicSimilarity.keys) {
                    if (topicSimilarity[topic]!! > 3)
                        mostSimilarTopics.add(topic.tid)
                }
                TopicsDataRetrievalFacade(firebaseFirestore, context)
                    .getTopicsDetails(object: ModelArrayCallback {
                        override fun onCallback(value: List<ModelEntity>) {
                            callback.onCallback(value)
                        }
                    }, mostSimilarTopics)
            }
        })
    }
}