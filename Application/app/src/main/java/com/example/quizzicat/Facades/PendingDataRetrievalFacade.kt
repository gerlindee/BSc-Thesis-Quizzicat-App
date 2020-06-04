package com.example.quizzicat.Facades

import android.content.Context
import android.widget.Toast
import com.example.quizzicat.Model.PendingQuestion
import com.example.quizzicat.Model.PendingQuestionAnswer
import com.example.quizzicat.Model.User
import com.example.quizzicat.Model.UserReports
import com.example.quizzicat.Utils.PendingAnswersCallback
import com.example.quizzicat.Utils.UserReportsCallBack
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

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

    fun reportPendingQuestion(question: PendingQuestion) {
        question.nr_reports += 1
        firebaseFirestore.collection("Pending_Questions")
            .document(question.pqid)
            .set(question)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userReport = UserReports(FirebaseAuth.getInstance().currentUser!!.uid, question.pqid)
                    firebaseFirestore.collection("User_Reports")
                        .add(userReport)
                        .addOnCompleteListener { task1 ->
                            if (task1.isSuccessful) {
                                Toast.makeText(context, "Question has been reported!", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, task1.exception!!.message.toString(), Toast.LENGTH_LONG).show()
                            }
                        }
                } else {
                    Toast.makeText(context, task.exception!!.message.toString(), Toast.LENGTH_LONG).show()
                }
            }
    }

    fun getReportedQuestionsForUser(callback: UserReportsCallBack) {
        firebaseFirestore.collection("User_Reports")
            .whereEqualTo("uid", FirebaseAuth.getInstance().currentUser!!.uid)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val reportedActivity = ArrayList<UserReports>()
                    for (document in task.result!!) {
                        val uid = document.get("uid") as String
                        val pqid = document.get("pqid") as String
                        reportedActivity.add(UserReports(uid, pqid))
                    }
                    callback.onCallback(reportedActivity)
                } else {
                    Toast.makeText(context, task.exception!!.message.toString(), Toast.LENGTH_LONG).show()
                }
            }
    }
}