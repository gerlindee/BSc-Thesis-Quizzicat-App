package com.example.quizzicat.Model

class Topic(
    val tid: Long,
    override val cid: Long,
    override val icon_url: String,
    override val name: String) : AbstractTopic