package com.example.quizzicat.Model

class MultiPlayerUserJoined(
    val gid: String,
    val uid: String,
    val score: Long,
    val role: String,
    var finished_playing: Boolean
)