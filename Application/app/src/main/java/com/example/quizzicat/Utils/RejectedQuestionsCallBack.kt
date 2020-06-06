package com.example.quizzicat.Utils

import com.example.quizzicat.Model.RejectedQuestion

interface RejectedQuestionsCallBack {
    fun onCallback(value: ArrayList<RejectedQuestion>)
}