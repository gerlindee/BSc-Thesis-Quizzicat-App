package com.example.quizzicat.Facades

import android.content.Context
import android.service.autofill.UserData
import android.util.Log
import android.widget.Toast
import com.example.quizzicat.Model.*
import com.example.quizzicat.Utils.*
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

    fun ratePendingQuestion(callback: PendingQuestionsCallBack, question: PendingQuestion, rating: Float) {
        UserDataRetrievalFacade(firebaseFirestore, FirebaseAuth.getInstance().currentUser!!.uid)
            .getNumberOfUsers(object: CounterCallBack {
                override fun onCallback(value: Int) {
                    question.nr_votes += 1
                    question.avg_rating = ( question.avg_rating + rating ).toLong() / question.nr_votes
                    firebaseFirestore.collection("Pending_Questions")
                        .document(question.pqid)
                        .set(question)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val userRating = UserRatings(FirebaseAuth.getInstance().currentUser!!.uid, question.pqid, rating.toLong())
                                firebaseFirestore.collection("User_Ratings")
                                    .add(userRating)
                                    .addOnCompleteListener { task1 ->
                                        if (task1.isSuccessful) {
                                            val result = ArrayList<PendingQuestion>()
                                            result.add(question)
                                            callback.onCallback(result)
                                        } else {
                                            Toast.makeText(context, task1.exception!!.message.toString(), Toast.LENGTH_LONG).show()
                                        }
                                    }
                            } else {
                                Toast.makeText(context, task.exception!!.message.toString(), Toast.LENGTH_LONG).show()
                            }
                        }
                }
            })
    }

    fun hasUserRatedTheQuestion(callback: CounterCallBack, question: PendingQuestion) {
        firebaseFirestore.collection("User_Ratings")
            .whereEqualTo("uid", FirebaseAuth.getInstance().currentUser!!.uid)
            .whereEqualTo("pqid", question.pqid)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback.onCallback(task.result!!.size())
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
                                UserDataRetrievalFacade(firebaseFirestore, FirebaseAuth.getInstance().currentUser!!.uid)
                                    .getNumberOfUsers(object: CounterCallBack {
                                        override fun onCallback(value: Int) {
                                            if (question.nr_reports >= (0.65 * value) ||
                                               (question.nr_votes >= (0.65 * value) && question.avg_rating <= 2)) {
                                                removeReportedQuestion(question)
                                            }
                                        }
                                    })
                            } else {
                                Toast.makeText(context, task1.exception!!.message.toString(), Toast.LENGTH_LONG).show()
                            }
                        }
                } else {
                    Toast.makeText(context, task.exception!!.message.toString(), Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun insertRejectedQuestions(question: RejectedQuestion) {
        firebaseFirestore.collection("Rejected_Questions")
            .document(question.rqid)
            .set(question)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Toast.makeText(context, task.exception!!.message.toString(), Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun insertRejectedAnswers(answer: RejectedQuestionAnswer) {
        firebaseFirestore.collection("Rejected_Question_Answers")
            .document(answer.raid)
            .set(answer)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Toast.makeText(context, task.exception!!.message.toString(), Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun removeReportedFromUserReports(pqid: String) {
        val userReportedQuestions = firebaseFirestore.collection("User_Reports")
        firebaseFirestore.collection("User_Reports")
            .whereEqualTo("pqid", pqid)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        userReportedQuestions.document(document.id).delete()
                    }
                }
            }
    }

    fun removeReportedQuestion(question: PendingQuestion) {
        val pendingQuestionsCollection = firebaseFirestore.collection("Pending_Questions")
        firebaseFirestore.collection("Pending_Questions")
            .whereEqualTo("pqid", question.pqid)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    insertRejectedQuestions(RejectedQuestion(question.pqid, question.tid, question.question_text, question.difficulty, question.submitted_by))
                    for (document in task.result!!) {
                        pendingQuestionsCollection.document(document.id).delete()
                    }
                    removeReportedFromUserReports(question.pqid)
                    val pendingAnswersCollection = firebaseFirestore.collection("Pending_Question_Answers")
                    firebaseFirestore.collection("Pending_Question_Answers")
                        .whereEqualTo("pqid", question.pqid)
                        .get()
                        .addOnCompleteListener { task1 ->
                            if (task1.isSuccessful) {
                                for (document in task1.result!!) {
                                    val raid = document.get("paid") as String
                                    val rqid = document.get("pqid") as String
                                    val answer_text = document.get("answer_text") as String
                                    val is_correct = document.get("correct") as Boolean
                                    insertRejectedAnswers(RejectedQuestionAnswer(raid, rqid, answer_text, is_correct))
                                    pendingAnswersCollection.document(document.id).delete()
                                }
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

    fun getRejectedQuestionsForUser(callback: RejectedQuestionsCallBack) {
        firebaseFirestore.collection("Rejected_Questions")
            .whereEqualTo("submitted_by", FirebaseAuth.getInstance().currentUser!!.uid)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val rejectedQuestions = ArrayList<RejectedQuestion>()
                    for (document in task.result!!) {
                        val questionDifficulty = document.get("difficulty") as Long
                        val questionQID = document.get("rqid") as String
                        val questionText = document.get("question_text") as String
                        val questionTID = document.get("tid") as Long
                        val questionSubmittedBy = document.get("submitted_by") as String
                        rejectedQuestions.add(RejectedQuestion(questionQID, questionTID, questionText, questionDifficulty, questionSubmittedBy))
                    }
                    callback.onCallback(rejectedQuestions)
                } else {
                    Toast.makeText(context, task.exception!!.message.toString(), Toast.LENGTH_LONG).show()
                }
            }
    }

    fun getAnswersForRejectedQuestion(callback: RejectedAnswerCallBack, rqid: String) {
        firebaseFirestore.collection("Rejected_Question_Answers")
            .whereEqualTo("rqid", rqid)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val rejectedAnswers = ArrayList<RejectedQuestionAnswer>()
                    for (document in task.result!!) {
                        val answerRAID = document.get("raid") as String
                        val answerText = document.get("answer_text") as String
                        val answerCorrect = document.get("correct") as Boolean
                        val answerRQID = document.get("rqid") as String
                        val quizAnswer = RejectedQuestionAnswer(answerRAID, answerRQID, answerText, answerCorrect)
                        rejectedAnswers.add(quizAnswer)
                    }
                    callback.onCallback(rejectedAnswers)
                } else {
                    Toast.makeText(context, task.exception!!.message, Toast.LENGTH_LONG).show()
                }
            }
    }
}