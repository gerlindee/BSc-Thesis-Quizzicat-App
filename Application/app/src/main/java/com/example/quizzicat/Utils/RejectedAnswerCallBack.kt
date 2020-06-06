package com.example.quizzicat.Utils

import com.example.quizzicat.Model.RejectedQuestionAnswer

interface RejectedAnswerCallBack {
    fun onCallback(value: ArrayList<RejectedQuestionAnswer>)
}