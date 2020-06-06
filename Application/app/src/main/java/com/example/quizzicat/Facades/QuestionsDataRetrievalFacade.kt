package com.example.quizzicat.Facades

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.quizzicat.Model.ActiveQuestion
import com.example.quizzicat.Model.ActiveQuestionAnswer
import com.example.quizzicat.Utils.AnswersCallBack
import com.example.quizzicat.Utils.QuestionsCallBack
import com.google.firebase.auth.FirebaseAuth
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

    fun getAcceptedQuestionsForUser(callback: QuestionsCallBack) {
        firebaseFirestore!!.collection("Active_Questions")
            .whereEqualTo("submitted_by", FirebaseAuth.getInstance().currentUser!!.uid)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val activeQuestions = ArrayList<ActiveQuestion>()
                    for (document in task.result!!) {
                        val quizQuestionDifficulty = document.get("difficulty") as Long
                        val quizQuestionQID = document.get("qid") as Long
                        val quizQuestionText = document.get("question_text") as String
                        val quizQuestionTID = document.get("tid") as Long
                        val quizSubmittedBy = document.get("submitted_by") as String
                        val quizQuestion = ActiveQuestion(quizQuestionQID, quizQuestionTID, quizQuestionText, quizQuestionDifficulty, quizSubmittedBy)
                        activeQuestions.add(quizQuestion)
                    }
                    callback.onCallback(activeQuestions)
                } else {
                    Toast.makeText(context, "Unable to retrieve active questions! Please try again.", Toast.LENGTH_LONG).show()
                }

            }
    }
}