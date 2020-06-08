package com.example.quizzicat.Utils

import com.example.quizzicat.Model.MultiPlayerGameQuestion

interface MultiPlayerQuestionsCallBack {
    fun onCallback(value: ArrayList<MultiPlayerGameQuestion>)
}