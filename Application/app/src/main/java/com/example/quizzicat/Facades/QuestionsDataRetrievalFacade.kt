package com.example.quizzicat.Facades

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.quizzicat.Model.ActiveQuestionAnswer
import com.example.quizzicat.Utils.AnswersCallBack
import com.google.firebase.firestore.FirebaseFirestore

class QuestionsDataRetrievalFacade(private val firebaseFirestore: FirebaseFirestore, private val context: Context) {
    fun getAnswersForQuestion(callback: AnswersCallBack, qid: Long) {
        firebaseFirestore.collection("Active_Question_Answers")
            .whereEqualTo("qid", qid)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val quizAnswers = ArrayList<ActiveQuestionAnswer>()
                    for (document in task.result!!) {
                        val answerAID = document.get("aid") as Long
                        val answerText = document.get("answer_text") as String
                        val answerCorrect = document.get("is_correct") as Boolean
                        val answerQID = document.get("qid") as Long
                        val quizAnswer = ActiveQuestionAnswer(answerAID, answerQID, answerText, answerCorrect)
                        quizAnswers.add(quizAnswer)
                    }
                    callback.onCallback(quizAnswers)
                } else {
                    Toast.makeText(context, task.exception!!.message, Toast.LENGTH_LONG).show()
                }
            }
    }
}