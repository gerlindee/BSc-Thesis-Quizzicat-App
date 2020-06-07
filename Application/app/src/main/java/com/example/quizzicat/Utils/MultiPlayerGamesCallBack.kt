package com.example.quizzicat.Utils

import com.example.quizzicat.Model.MultiPlayerGame

interface MultiPlayerGamesCallBack {
    fun onCallback(value: ArrayList<MultiPlayerGame>)
}