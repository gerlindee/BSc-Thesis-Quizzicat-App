package com.example.quizzicat

import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quizzicat.Adapters.ActiveQuestionsAdapter
import com.example.quizzicat.Adapters.PendingQuestionsAdapter
import com.example.quizzicat.Adapters.RejectedQuestionsAdapter
import com.example.quizzicat.Facades.PendingDataRetrievalFacade
import com.example.quizzicat.Facades.QuestionsDataRetrievalFacade
import com.example.quizzicat.Model.ActiveQuestion
import com.example.quizzicat.Model.RejectedQuestion
import com.example.quizzicat.Utils.QuestionsCallBack
import com.example.quizzicat.Utils.RejectedQuestionsCallBack
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class UserQuestionsAcceptedActivity: AppCompatActivity() {
    private var mFirestoreDatabase: FirebaseFirestore? = null

    private var acceptedQuestions: RecyclerView? = null
    private var progressBar: ProgressBar? = null
    private var noQuestionsLayout: LinearLayout? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_questions_pending)

        mFirestoreDatabase = Firebase.firestore

        initializeLayoutElements()

        when (intent.extras!!.getString("TYPE_DISPLAYED")!!) {
            "ACCEPTED" -> {
                progressBar!!.visibility = View.VISIBLE
                QuestionsDataRetrievalFacade(mFirestoreDatabase!!, this)
                    .getAcceptedQuestionsForUser(object : QuestionsCallBack {
                        override fun onCallback(value: ArrayList<ActiveQuestion>) {
                            if (value.size == 0) {
                                noQuestionsLayout!!.visibility = View.VISIBLE
                            } else {
                                acceptedQuestions!!.apply {
                                    layoutManager = LinearLayoutManager(this@UserQuestionsAcceptedActivity)
                                    adapter = ActiveQuestionsAdapter(context, mFirestoreDatabase!!, value)
                                    addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
                                }
                                acceptedQuestions!!.visibility = View.VISIBLE
                            }
                            progressBar!!.visibility = View.GONE
                        }
                    })
            }
            "REJECTED" -> {
                PendingDataRetrievalFacade(mFirestoreDatabase!!, this)
                    .getRejectedQuestionsForUser(object: RejectedQuestionsCallBack {
                        override fun onCallback(value: ArrayList<RejectedQuestion>) {
                            if (value.size == 0) {
                                noQuestionsLayout!!.visibility = View.VISIBLE
                            } else {
                                acceptedQuestions!!.apply {
                                    layoutManager = LinearLayoutManager(this@UserQuestionsAcceptedActivity)
                                    adapter = RejectedQuestionsAdapter(context, mFirestoreDatabase!!, value)
                                    addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
                                    acceptedQuestions!!.visibility = View.VISIBLE
                                }
                            }
                            progressBar!!.visibility = View.GONE
                        }
                    })
            }
        }
    }

    private fun initializeLayoutElements() {
        acceptedQuestions = findViewById(R.id.pending_questions_user_list)
        progressBar = findViewById(R.id.pending_user_progress_bar)
        noQuestionsLayout = findViewById(R.id.layout_no_questions)
    }
}