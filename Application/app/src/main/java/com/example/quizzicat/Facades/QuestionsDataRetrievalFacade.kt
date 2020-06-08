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
    fun getAnswersForQuestion(callback: AnswersCallBack, qid: String) {
        firebaseFirestore.collection("Active_Question_Answers")
            .whereEqualTo("qid", qid)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val quizAnswers = ArrayList<ActiveQuestionAnswer>()
                    for (document in task.result!!) {
                        val answerAID = document.get("aid") as String
                        val answerText = document.get("answer_text") as String
                        val answerCorrect = document.get("correct") as Boolean
                        val answerQID = document.get("qid") as String
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
        firebaseFirestore.collection("Active_Questions")
            .whereEqualTo("submitted_by", FirebaseAuth.getInstance().currentUser!!.uid)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val activeQuestions = ArrayList<ActiveQuestion>()
                    for (document in task.result!!) {
                        val quizQuestionDifficulty = document.get("difficulty") as Long
                        val quizQuestionQID = document.get("qid") as String
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

    fun getQuestionsForQuiz(callback: QuestionsCallBack, questionsDifficulty: String, questionsTopic: Long) {
        var difficultyKey: Int? = null
        when (questionsDifficulty) {
            "Random" -> difficultyKey = 0
            "Easy" -> difficultyKey = 1
            "Medium" -> difficultyKey = 2
            "Hard" -> difficultyKey = 3
        }
        if (difficultyKey == 0) {
            firebaseFirestore.collection("Active_Questions")
                .whereEqualTo("tid", questionsTopic)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val quizQuestions = ArrayList<ActiveQuestion>()
                        for (document in task.result!!) {
                            val quizQuestionDifficulty = document.get("difficulty") as Long
                            val quizQuestionQID = document.get("qid") as String
                            val quizQuestionText = document.get("question_text") as String
                            val quizQuestionTID = document.get("tid") as Long
                            val quizSubmittedBy = document.get("submitted_by") as String
                            val quizQuestion = ActiveQuestion(quizQuestionQID, quizQuestionTID, quizQuestionText, quizQuestionDifficulty, quizSubmittedBy)
                            quizQuestions.add(quizQuestion)
                        }
                        callback.onCallback(quizQuestions)
                    } else {
                        Log.d("QuestionsQuery", task.exception.toString())
                    }
                }
        } else {
            firebaseFirestore.collection("Active_Questions")
                .whereEqualTo("tid", questionsTopic)
                .whereEqualTo("difficulty", difficultyKey)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val quizQuestions = ArrayList<ActiveQuestion>()
                        for (document in task.result!!) {
                            val quizQuestionDifficulty = document.get("difficulty") as Long
                            val quizQuestionQID = document.get("qid") as String
                            val quizQuestionText = document.get("question_text") as String
                            val quizQuestionTID = document.get("tid") as Long
                            val quizSubmittedBy = document.get("submitted_by") as String
                            val quizQuestion = ActiveQuestion(quizQuestionQID, quizQuestionTID, quizQuestionText, quizQuestionDifficulty, quizSubmittedBy)
                            quizQuestions.add(quizQuestion)
                        }
                        callback.onCallback(quizQuestions)
                    } else {
                        Log.d("QuestionsQuery", task.exception.toString())
                    }
                }
        }
    }

    fun getAnswers(callback: AnswersCallBack, QIDList: ArrayList<String>) {
        firebaseFirestore.collection("Active_Question_Answers")
            .whereIn("qid", QIDList)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val quizAnswers = ArrayList<ActiveQuestionAnswer>()
                    for (document in task.result!!) {
                        val answerAID = document.get("aid") as String
                        val answerText = document.get("answer_text") as String
                        val answerCorrect = document.get("correct") as Boolean
                        val answerQID = document.get("qid") as String
                        val quizAnswer = ActiveQuestionAnswer(answerAID, answerQID, answerText, answerCorrect)
                        quizAnswers.add(quizAnswer)
                    }
                    callback.onCallback(quizAnswers)
                } else {
                    Log.d("AnswersQuery", task.exception.toString())
                }
            }
    }

    fun getQuestionsData(QIDList: ArrayList<String>, callback: QuestionsCallBack) {
        firebaseFirestore.collection("Active_Questions")
            .whereIn("qid", QIDList)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val questions = ArrayList<ActiveQuestion>()
                    for (document in task.result!!) {
                        val quizQuestionDifficulty = document.get("difficulty") as Long
                        val quizQuestionQID = document.get("qid") as String
                        val quizQuestionText = document.get("question_text") as String
                        val quizQuestionTID = document.get("tid") as Long
                        val quizSubmittedBy = document.get("submitted_by") as String
                        val quizQuestion = ActiveQuestion(quizQuestionQID, quizQuestionTID, quizQuestionText, quizQuestionDifficulty, quizSubmittedBy)
                        questions.add(quizQuestion)
                    }
                    callback.onCallback(questions)
                } else {
                    Toast.makeText(context, task.exception!!.message, Toast.LENGTH_LONG).show()
                }
            }
    }

}