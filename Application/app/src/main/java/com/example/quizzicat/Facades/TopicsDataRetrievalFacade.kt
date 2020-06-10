package com.example.quizzicat.Facades

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.quizzicat.Model.AbstractTopic
import com.example.quizzicat.Model.Topic
import com.example.quizzicat.Model.TopicCategory
import com.example.quizzicat.Model.TopicPlayed
import com.example.quizzicat.Utils.CustomCallBack
import com.example.quizzicat.Utils.TopicCallBack
import com.example.quizzicat.Utils.TopicsPlayedCallBack
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TopicsDataRetrievalFacade(private val firebaseFirestore: FirebaseFirestore, private val context: Context) {
    fun getTopicDetails(callback: TopicCallBack, tid: Long) {
        firebaseFirestore.collection("Topics")
            .whereEqualTo("tid", tid)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    var topic: Topic? = null
                    for (document in task.result!!) {
                        val topicCID = document.get("cid") as Long
                        val topicTID = document.get("tid") as Long
                        val topicURL = document.get("icon_url") as String
                        val topicName = document.get("name") as String
                        topic = Topic(topicTID, topicCID, topicURL, topicName)
                    }
                    callback.onCallback(topic!!)
                }
            }
    }

    fun getTopicCategories(callBack: CustomCallBack) {
        firebaseFirestore.collection("Topic_Categories")
            .orderBy("name")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val topicCategories = ArrayList<TopicCategory>()
                    for (document in task.result!!) {
                        val topicCategoryID = document.get("cid") as Long
                        val topicCategoryURL = document.get("icon_url") as String
                        val topicCategoryName = document.get("name") as String
                        val topicCategory = TopicCategory(topicCategoryID, topicCategoryURL, topicCategoryName)
                        topicCategories.add(topicCategory)
                    }
                    callBack.onCallback(topicCategories)
                } else {
                    Log.d("TopicCategoryQuery", task.exception.toString())
                }
            }
    }

    fun getTopicsForACategory(callBack: CustomCallBack, selectedCategory: Long) {
        firebaseFirestore.collection("Topics")
            .whereEqualTo("cid", selectedCategory)
            .orderBy("name")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val topics = ArrayList<Topic>()
                    for (document in task.result!!) {
                        val topicCID = document.get("cid") as Long
                        val topicTID = document.get("tid") as Long
                        val topicURL = document.get("icon_url") as String
                        val topicName = document.get("name") as String
                        val topic = Topic(topicTID, topicCID, topicURL, topicName)
                        topics.add(topic)
                    }
                    callBack.onCallback(topics)
                } else {
                    Log.d("TopicQuery", task.exception.toString())
                }
            }
    }

    fun getCategoriesPlayedData(callback: CustomCallBack, topicList: ArrayList<Topic>) {
        val playedCategories = ArrayList<Long>()
        for (topic in topicList) {
            playedCategories.add(topic.cid)
        }
        firebaseFirestore.collection("Topic_Categories")
            .whereIn("cid", playedCategories)
            .get()
            .addOnCompleteListener { task1 ->
                if (task1.isSuccessful) {
                    val categories = ArrayList<TopicCategory>()
                    for (document in task1.result!!) {
                        val topicCategoryID = document.get("cid") as Long
                        val topicCategoryURL = document.get("icon_url") as String
                        val topicCategoryName = document.get("name") as String
                        val topicCategory = TopicCategory(topicCategoryID, topicCategoryURL, topicCategoryName)
                        categories.add(topicCategory)
                    }
                    callback.onCallback(categories)
                } else {
                    Toast.makeText(context, task1.exception!!.message.toString(), Toast.LENGTH_LONG).show()
                }
            }
    }

    fun getUserPlayedHistory(callback: TopicsPlayedCallBack) {
        val user = FirebaseAuth.getInstance().uid
        if (user != null) {
            firebaseFirestore.collection("Topics_Played")
                .whereEqualTo("uid", user)
                .get()
                .addOnCompleteListener { task ->
                    val topicsPlayed = ArrayList<TopicPlayed>()
                    for (document in task.result!!) {
                        val correct_answers = document.get("correct_answers") as Long
                        val incorrect_answers = document.get("incorrect_answers") as Long
                        val pid = document.get("pid") as String
                        val tid = document.get("tid") as Long
                        val cid = document.get("cid") as Long
                        val times_played_solo = document.get("times_played_solo") as Long
                        val uid = document.get("uid") as String
                        val topicPlayed = TopicPlayed(pid, tid, cid, uid, correct_answers, incorrect_answers, times_played_solo)
                        topicsPlayed.add(topicPlayed)
                    }
                    callback.onCallback(topicsPlayed)
                }
        }
    }

    fun getTopicsPlayedData(callback: CustomCallBack, topicsPlayed: ArrayList<TopicPlayed>) {
        val playedTopics = ArrayList<Long>()
        for (topic in topicsPlayed) {
            playedTopics.add(topic.tid)
        }
        firebaseFirestore.collection("Topics")
            .whereIn("tid", playedTopics)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val topics = ArrayList<Topic>()
                    for (document in task.result!!) {
                        val topicCID = document.get("cid") as Long
                        val topicTID = document.get("tid") as Long
                        val topicURL = document.get("icon_url") as String
                        val topicName = document.get("name") as String
                        val topic = Topic(topicTID, topicCID, topicURL, topicName)
                        topics.add(topic)
                    }
                    callback.onCallback(topics)
                } else {
                    Toast.makeText(context, task.exception!!.message.toString(), Toast.LENGTH_LONG).show()
                }
            }
    }

    fun getTopicsNotPlayed(callback: CustomCallBack, topicsPlayed: ArrayList<TopicPlayed>) {
        getAllTopics(object: CustomCallBack {
            override fun onCallback(value: List<AbstractTopic>) {
                val allTopics = value as ArrayList<Topic>
                val playedIDs = topicsPlayed.map { topic -> topic.tid }
                val topicsNotPlayed = allTopics.filterNot { topic -> topic.tid in playedIDs }
                callback.onCallback(topicsNotPlayed)
            }
        })
    }

    fun getAllTopics(callback: CustomCallBack) {
        firebaseFirestore.collection("Topics")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val topics = ArrayList<Topic>()
                    for (document in task.result!!) {
                        val topicCID = document.get("cid") as Long
                        val topicTID = document.get("tid") as Long
                        val topicURL = document.get("icon_url") as String
                        val topicName = document.get("name") as String
                        val topic = Topic(topicTID, topicCID, topicURL, topicName)
                        topics.add(topic)
                    }
                    callback.onCallback(topics)
                } else {
                    Toast.makeText(context, task.exception!!.message.toString(), Toast.LENGTH_LONG).show()
                }
            }
    }
}