package com.example.quizzicat.Model

class PendingQuestions(
    val pqid: String,
    val cid: Long,
    val tid: Long,
    val difficulty: Long,
    val question_text: String,
    val submitted_by: String,
    val nr_votes: Long,
    val avg_rating: Long,
    val nr_reports: Long
)