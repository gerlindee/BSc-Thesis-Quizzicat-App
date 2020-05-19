package com.example.quizzicat.Utils

import com.example.quizzicat.Model.ActiveQuestion

interface QuestionsCallBack {
    fun onCallback(value: ArrayList<ActiveQuestion>)
}