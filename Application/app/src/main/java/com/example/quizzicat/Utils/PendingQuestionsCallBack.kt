package com.example.quizzicat.Utils

import com.example.quizzicat.Model.PendingQuestion

interface PendingQuestionsCallBack {
    fun onCallback(value: ArrayList<PendingQuestion>)
}