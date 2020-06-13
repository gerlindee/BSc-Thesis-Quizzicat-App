package com.example.quizzicat.Model

class PendingQuestionAnswer(
    val paid: String,
    val pqid: String,
    val answer_text: String,
    val correct: Boolean
): ModelEntity