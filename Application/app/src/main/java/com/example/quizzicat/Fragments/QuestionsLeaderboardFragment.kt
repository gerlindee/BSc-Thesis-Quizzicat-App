package com.example.quizzicat.Fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quizzicat.Adapters.PendingQuestionsAdapter
import com.example.quizzicat.Model.PendingQuestion
import com.example.quizzicat.NoInternetConnectionActivity
import com.example.quizzicat.QuestionsFactoryActivity
import com.example.quizzicat.R
import com.example.quizzicat.Utils.PendingQuestionsCallBack
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class QuestionsLeaderboardFragment : Fragment() {

    private var mFirestoreDatabase: FirebaseFirestore? = null

    private var questionsFactoryNavigation: MaterialButton? = null
    private var pendingQuestions: RecyclerView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_questions_leaderboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mFirestoreDatabase = Firebase.firestore

        initializeLayoutElements()

        getPendingQuestions(object: PendingQuestionsCallBack {
            override fun onCallback(value: ArrayList<PendingQuestion>) {
                pendingQuestions!!.apply {
                    layoutManager = LinearLayoutManager(activity)
                    adapter = PendingQuestionsAdapter("LEADERBOARD", context, mFirestoreDatabase!!, value)
                    addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
                }
            }
        })

        questionsFactoryNavigation!!.setOnClickListener {
            val questionsFactoryIntent = Intent(activity, QuestionsFactoryActivity::class.java)
            startActivity(questionsFactoryIntent)
        }
    }

    override fun onResume() {
        super.onResume()
        getPendingQuestions(object: PendingQuestionsCallBack {
            override fun onCallback(value: ArrayList<PendingQuestion>) {
                pendingQuestions!!.apply {
                    layoutManager = LinearLayoutManager(activity)
                    adapter = PendingQuestionsAdapter("LEADERBOARD", context, mFirestoreDatabase!!, value)
                    addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
                }
            }
        })
    }

    private fun initializeLayoutElements() {
        questionsFactoryNavigation = view?.findViewById(R.id.button_user_questions)
        pendingQuestions = view?.findViewById(R.id.pending_questions_list)
    }

    private fun getPendingQuestions(callback: PendingQuestionsCallBack) {
        mFirestoreDatabase!!.collection("Pending_Questions")
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
                    Toast.makeText(context, "Unable to retrieve pending questions! Please try again.", Toast.LENGTH_LONG).show()
                }
            }
    }
}
