package com.example.quizzicat.Model

class PendingQuestion(
    val pqid: String,
    val tid: Long,
    val difficulty: Long,
    val question_text: String,
    val submitted_by: String,
    val nr_votes: Long,
    val avg_rating: Long,
    val nr_reports: Long
)