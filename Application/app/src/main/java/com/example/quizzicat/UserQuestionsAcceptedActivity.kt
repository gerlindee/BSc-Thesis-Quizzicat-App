package com.example.quizzicat

import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quizzicat.Adapters.ActiveQuestionsAdapter
import com.example.quizzicat.Adapters.PendingQuestionsAdapter
import com.example.quizzicat.Model.ActiveQuestion
import com.example.quizzicat.Utils.QuestionsCallBack
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class UserQuestionsAcceptedActivity: AppCompatActivity() {
    private var mFirestoreDatabase: FirebaseFirestore? = null
    private var acceptedQuestions: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_questions_pending)

        mFirestoreDatabase = Firebase.firestore

        acceptedQuestions = findViewById(R.id.pending_questions_user_list)

        getAcceptedQuestionsForUser(object: QuestionsCallBack {
            override fun onCallback(value: ArrayList<ActiveQuestion>) {
                acceptedQuestions!!.apply {
                    layoutManager = LinearLayoutManager(this@UserQuestionsAcceptedActivity)
                    adapter = ActiveQuestionsAdapter(context, mFirestoreDatabase!!, value)
                    addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
                }
            }
        })

    }

    private fun getAcceptedQuestionsForUser(callback: QuestionsCallBack) {
        mFirestoreDatabase!!.collection("Active_Questions")
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
                    Toast.makeText(this, "Unable to retrieve active questions! Please try again.", Toast.LENGTH_LONG).show()
                }

            }
    }
}