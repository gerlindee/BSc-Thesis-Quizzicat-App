package com.example.quizzicat.Utils

import com.example.quizzicat.Model.TopicCategory

interface CustomCallBack {
    fun onCallback(value: List<TopicCategory>)
}