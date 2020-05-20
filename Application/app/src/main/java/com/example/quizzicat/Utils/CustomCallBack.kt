package com.example.quizzicat.Utils

import com.example.quizzicat.Model.AbstractTopic

interface CustomCallBack {
    fun onCallback(value: List<AbstractTopic>)
}