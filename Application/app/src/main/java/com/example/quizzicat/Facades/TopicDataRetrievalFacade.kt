package com.example.quizzicat.Facades

import com.example.quizzicat.Model.Topic
import com.example.quizzicat.Utils.TopicCallBack
import com.google.firebase.firestore.FirebaseFirestore

class TopicDataRetrievalFacade(private val firebaseFirestore: FirebaseFirestore, private val tid: Long) {
    fun getTopicDetails(callback: TopicCallBack) {
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
}