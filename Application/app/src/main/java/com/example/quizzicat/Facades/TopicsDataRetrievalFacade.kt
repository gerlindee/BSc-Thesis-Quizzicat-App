package com.example.quizzicat.Facades

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.quizzicat.Model.Topic
import com.example.quizzicat.Model.TopicCategory
import com.example.quizzicat.Utils.CustomCallBack
import com.google.firebase.firestore.FirebaseFirestore

class TopicsDataRetrievalFacade(private val firebaseFirestore: FirebaseFirestore, private val context: Context) {
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
}