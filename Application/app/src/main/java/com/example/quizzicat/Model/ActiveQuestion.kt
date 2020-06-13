package com.example.quizzicat.Model

class ActiveQuestion(
    val qid: String,
    val tid: Long,
    val question_text: String,
    val difficulty: Long,
    val submitted_by: String
): ModelEntity