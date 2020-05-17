package com.example.quizzicat.Model

class Topic(
    val TID: Long,
    override val CID: Long,
    override val iconURL: String,
    override val name: String) : AbstractTopic