package com.example.quizzicat.Utils

import com.example.quizzicat.Model.MultiPlayerUserJoined

interface MultiPlayerUsersCallBack {
    fun onCallback(value: ArrayList<MultiPlayerUserJoined>)
}