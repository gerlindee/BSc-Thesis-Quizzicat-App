package com.example.quizzicat.Utils

import com.example.quizzicat.Model.ActiveQuestionAnswer

interface AnswersCallBack {
    fun onCallback(value: ArrayList<ActiveQuestionAnswer>)
}