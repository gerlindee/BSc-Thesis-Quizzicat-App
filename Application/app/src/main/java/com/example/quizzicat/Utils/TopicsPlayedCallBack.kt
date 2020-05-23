package com.example.quizzicat.Utils

import com.example.quizzicat.Model.TopicPlayed

interface TopicsPlayedCallBack {
    fun onCallback(value: List<TopicPlayed>)
}