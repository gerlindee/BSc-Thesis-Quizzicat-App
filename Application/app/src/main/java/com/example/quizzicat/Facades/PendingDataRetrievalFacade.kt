package com.example.quizzicat.Facades

import android.content.Context
import android.widget.Toast
import com.example.quizzicat.Model.PendingQuestionAnswer
import com.example.quizzicat.Utils.PendingAnswersCallback
import com.google.firebase.firestore.FirebaseFirestore

class PendingDataRetrievalFacade(private val firebaseFirestore: FirebaseFirestore, private val context: Context) {
    fun getAnswersForAQuestion(callback: PendingAnswersCallback, pqid: String) {
        firebaseFirestore.collection("Pending_Question_Answers")
            .whereEqualTo("pqid", pqid)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val pendingAnswers = ArrayList<PendingQuestionAnswer>()
                    for (document in task.result!!) {
                        val paid = document.get("paid") as String
                        val pqid_a = document.get("pqid") as String
                        val answer_text = document.get("answer_text") as String
                        val correct = document.get("correct") as Boolean
                        val answer = PendingQuestionAnswer(paid, pqid_a, answer_text, correct)
                        pendingAnswers.add(answer)
                    }
                    callback.onCallback(pendingAnswers)
                } else {
                    Toast.makeText(context, task.exception!!.message.toString(), Toast.LENGTH_LONG).show()
                }
            }
    }
}