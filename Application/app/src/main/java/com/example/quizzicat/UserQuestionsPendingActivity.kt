package com.example.quizzicat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quizzicat.Adapters.PendingQuestionsAdapter
import com.example.quizzicat.Model.PendingQuestion
import com.example.quizzicat.Utils.PendingQuestionsCallBack
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class UserQuestionsPendingActivity : AppCompatActivity() {

    private var mFirestoreDatabase: FirebaseFirestore? = null
    private var pendingQuestions: RecyclerView? = null
    private var progressBar: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_questions_pending)

        mFirestoreDatabase = Firebase.firestore

        pendingQuestions = findViewById(R.id.pending_questions_user_list)
        progressBar = findViewById(R.id.pending_user_progress_bar)

        getPendingQuestionsForUser(object: PendingQuestionsCallBack {
            override fun onCallback(value: ArrayList<PendingQuestion>) {
                pendingQuestions!!.apply {
                    layoutManager = LinearLayoutManager(this@UserQuestionsPendingActivity)
                    adapter = PendingQuestionsAdapter("USER_PENDING", this@UserQuestionsPendingActivity, mFirestoreDatabase!!, value)
                    addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
                }
                pendingQuestions!!.visibility = View.VISIBLE
                progressBar!!.visibility = View.GONE
            }
        })

    }

    private fun getPendingQuestionsForUser(callback: PendingQuestionsCallBack) {
        progressBar!!.visibility = View.VISIBLE
        pendingQuestions!!.visibility = View.GONE
        mFirestoreDatabase!!.collection("Pending_Questions")
            .whereEqualTo("submitted_by", FirebaseAuth.getInstance().currentUser!!.uid)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val pendingQuestions = ArrayList<PendingQuestion>()
                    for (document in task.result!!) {
                        val pqid = document.get("pqid") as String
                        val tid = document.get("tid") as Long
                        val difficulty = document.get("difficulty") as Long
                        val question_text = document.get("question_text") as String
                        val submitted_by = document.get("submitted_by") as String
                        val nr_votes = document.get("nr_votes") as Long
                        val avg_rating = document.get("avg_rating") as Long
                        val nr_reports = document.get("nr_reports") as Long
                        val pendingQuestion = PendingQuestion(pqid, tid, difficulty, question_text, submitted_by, nr_votes, avg_rating, nr_reports)
                        pendingQuestions.add(pendingQuestion)
                    }
                    callback.onCallback(pendingQuestions)
                } else {
                    Toast.makeText(this, "Unable to retrieve pending questions! Please try again.", Toast.LENGTH_LONG).show()
                }
            }
    }
}
