package com.example.quizzicat.Model

interface AbstractTopic: ModelEntity {
    val cid: Long
    val name: String
    val icon_url: String
}