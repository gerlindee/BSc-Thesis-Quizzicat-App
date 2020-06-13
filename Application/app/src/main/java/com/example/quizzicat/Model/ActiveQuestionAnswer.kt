package com.example.quizzicat.Model

class ActiveQuestionAnswer(
    val aid: String,
    val qid: String,
    val answer_text: String,
    val correct: Boolean
): ModelEntity