package com.example.quizzicat.Fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quizzicat.Adapters.PendingQuestionsAdapter
import com.example.quizzicat.Facades.PendingDataRetrievalFacade
import com.example.quizzicat.Model.ModelEntity
import com.example.quizzicat.Model.PendingQuestion
import com.example.quizzicat.Model.UserReports
import com.example.quizzicat.NoInternetConnectionActivity
import com.example.quizzicat.QuestionsFactoryActivity
import com.example.quizzicat.R
import com.example.quizzicat.Utils.ModelArrayCallback
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class QuestionsLeaderboardFragment : Fragment() {

    private var mFirestoreDatabase: FirebaseFirestore? = null
    private var nonReportedQuestions = ArrayList<PendingQuestion>()
    private var reportedQuestions = ArrayList<UserReports>()
    private var questionsFactoryNavigation: MaterialButton? = null
    private var pendingQuestions: RecyclerView? = null
    private var progressBar: ProgressBar? = null
    private var noQuestionsLayout: LinearLayout? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_questions_leaderboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mFirestoreDatabase = Firebase.firestore

        initializeLayoutElements()

        setupQuestions()

        questionsFactoryNavigation!!.setOnClickListener {
            val questionsFactoryIntent = Intent(activity, QuestionsFactoryActivity::class.java)
            startActivity(questionsFactoryIntent)
        }
    }

    private fun setupQuestions() {
        getPendingQuestions(object: ModelArrayCallback {
            override fun onCallback(value: List<ModelEntity>) {
                // remove questions reported by the user
                val pendingQuestionsLocal = value as ArrayList<PendingQuestion>
                PendingDataRetrievalFacade(mFirestoreDatabase!!, context!!)
                    .getReportedQuestionsForUser(object: ModelArrayCallback {
                        override fun onCallback(value: List<ModelEntity>) {
                            reportedQuestions = value as ArrayList<UserReports>
                            for (question in pendingQuestionsLocal) {
                                if (!isQuestionReported(reportedQuestions, question.pqid)) {
                                    nonReportedQuestions.add(question)
                                }
                            }
                            if (nonReportedQuestions.size == 0) {
                                progressBar!!.visibility = View.GONE
                                noQuestionsLayout!!.visibility = View.VISIBLE
                            } else {
                                pendingQuestions!!.apply {
                                    layoutManager = LinearLayoutManager(activity)
                                    adapter = PendingQuestionsAdapter("LEADERBOARD", context, mFirestoreDatabase!!, nonReportedQuestions)
                                    addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
                                }
                                progressBar!!.visibility = View.GONE
                                pendingQuestions!!.visibility = View.VISIBLE
                                listenForQuestions()
                            }
                        }
                    })
            }
        })
    }

    private fun listenForQuestions() {
        val questionsCollection = mFirestoreDatabase!!.collection("Pending_Questions")
        questionsCollection.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Toast.makeText(context, "Questions could not be fetched! Please try again!", Toast.LENGTH_LONG).show()
                return@addSnapshotListener
            }

            for (changes in snapshot!!.documentChanges) {
                val submitted_by = changes.document.data.get("submitted_by") as String
                if (submitted_by != FirebaseAuth.getInstance().uid) {
                    val pqid = changes.document.data.get("pqid") as String
                    val tid = changes.document.data.get("tid") as Long
                    val difficulty = changes.document.data.get("difficulty") as Long
                    val question_text = changes.document.data.get("question_text") as String
                    val nr_votes = changes.document.data.get("nr_votes") as Long
                    val avg_rating = changes.document.data.get("avg_rating") as Long
                    val nr_reports = changes.document.data.get("nr_reports") as Long
                    val changedQuestion = PendingQuestion(pqid, tid, difficulty, question_text, submitted_by, nr_votes, avg_rating, nr_reports)
                    var question: PendingQuestion? = null
                    for (idx in (0 until nonReportedQuestions.size)) {
                        if (nonReportedQuestions[idx].pqid == pqid) {
                            question = nonReportedQuestions[idx]
                        }
                    }
                    if (changes.type == DocumentChange.Type.ADDED && question == null && !isQuestionReported(reportedQuestions, pqid)) {
                        nonReportedQuestions.add(changedQuestion)
                    } else if (changes.type == DocumentChange.Type.REMOVED) {
                        nonReportedQuestions.remove(question)
                    }
                    if (pendingQuestions!!.adapter != null) {
                        pendingQuestions!!.adapter!!.notifyDataSetChanged()
                    }
                }
            }
        }

    }

    private fun initializeLayoutElements() {
        questionsFactoryNavigation = view?.findViewById(R.id.button_user_questions)
        pendingQuestions = view?.findViewById(R.id.pending_questions_list)
        progressBar = view?.findViewById(R.id.questions_leaderboard_progress_bar)
        noQuestionsLayout = view?.findViewById(R.id.layout_no_leaderboard_questions)
    }

    private fun getPendingQuestions(callback: ModelArrayCallback) {
        progressBar!!.visibility = View.VISIBLE
        mFirestoreDatabase!!.collection("Pending_Questions")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val pendingQuestions = ArrayList<PendingQuestion>()
                    for (document in task.result!!) {
                        val submitted_by = document.get("submitted_by") as String
                        if (submitted_by != FirebaseAuth.getInstance().currentUser!!.uid) { // user can't vote for their own questions
                            val pqid = document.get("pqid") as String
                            val tid = document.get("tid") as Long
                            val difficulty = document.get("difficulty") as Long
                            val question_text = document.get("question_text") as String
                            val nr_votes = document.get("nr_votes") as Long
                            val avg_rating = document.get("avg_rating") as Long
                            val nr_reports = document.get("nr_reports") as Long
                            val pendingQuestion = PendingQuestion(pqid, tid, difficulty, question_text, submitted_by, nr_votes, avg_rating, nr_reports)
                            pendingQuestions.add(pendingQuestion)
                        }
                    }
                    callback.onCallback(pendingQuestions)
                } else {
                    Toast.makeText(context, "Unable to retrieve pending questions! Please try again.", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun isQuestionReported(questions: ArrayList<UserReports>, pqid: String): Boolean {
        for (question in questions) {
            if (question.pqid == pqid)
                return true
        }
        return false
    }
}
