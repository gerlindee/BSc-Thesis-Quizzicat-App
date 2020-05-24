package com.example.quizzicat.Model

class TopicPlayed(
    val pid: String,
    val tid: Long,
    val uid: String,
    // for the solo games
    var correct_answers: Long,
    var incorrect_answers: Long,
    var times_played_solo: Long
)