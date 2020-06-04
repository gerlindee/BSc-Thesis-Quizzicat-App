package com.example.quizzicat.Model

class PendingQuestion(
    val pqid: String,
    val tid: Long,
    val difficulty: Long,
    val question_text: String,
    val submitted_by: String,
    var nr_votes: Long,
    var avg_rating: Long,
    var nr_reports: Long
)