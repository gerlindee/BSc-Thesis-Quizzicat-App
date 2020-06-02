package com.example.quizzicat.Utils

import com.example.quizzicat.Adapters.PendingQuestionsAdapter
import com.example.quizzicat.Model.PendingQuestionAnswer

interface PendingAnswersCallback {
    fun onCallback(value: ArrayList<PendingQuestionAnswer>)
}