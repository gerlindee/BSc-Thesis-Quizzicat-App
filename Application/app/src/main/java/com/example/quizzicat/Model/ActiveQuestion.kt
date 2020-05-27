package com.example.quizzicat.Model

class ActiveQuestion(val qid: Long,
                     val tid: Long,
                     val question_text: String,
                     val difficulty: Long,
                     val submittedBy: String)