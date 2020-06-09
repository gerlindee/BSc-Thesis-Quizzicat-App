package com.example.quizzicat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quizzicat.Adapters.LobbyUsersAdapter
import com.example.quizzicat.Facades.MultiPlayerDataRetrievalFacade
import com.example.quizzicat.Facades.QuestionsDataRetrievalFacade
import com.example.quizzicat.Model.ActiveQuestion
import com.example.quizzicat.Model.MultiPlayerGame
import com.example.quizzicat.Model.MultiPlayerUserJoined
import com.example.quizzicat.Utils.MultiPlayerUsersCallBack
import com.example.quizzicat.Utils.QuestionsCallBack
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MultiPlayerLobbyActivity : AppCompatActivity() {

    private var mFirestoreDatabase: FirebaseFirestore? = null

    private var lobbyGamePIN: TextView? = null
    private var lobbyJoinedUsers: RecyclerView? = null
    private var lobbyStartButton: MaterialButton? = null
    private var playerType: String? = ""
    private var gamePIN: String? = ""
    private var gid: String? = null
    private var usersJoined = ArrayList<MultiPlayerUserJoined>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multi_player_lobby)

        mFirestoreDatabase = Firebase.firestore

        setupLayoutElements()

        playerType = intent.extras!!.getString("userRole")
        gamePIN = intent.extras!!.getString("gamePIN")
        gid = intent.extras!!.getString("gid")

        if (playerType == "CREATOR") {
            lobbyStartButton!!.visibility = View.VISIBLE
        }

        lobbyGamePIN!!.text = gamePIN

        MultiPlayerDataRetrievalFacade(mFirestoreDatabase!!, this)
            .getUsersForGame(gid!!, object: MultiPlayerUsersCallBack {
                override fun onCallback(value: ArrayList<MultiPlayerUserJoined>) {
                    usersJoined = value
                    lobbyJoinedUsers!!.apply {
                        layoutManager = LinearLayoutManager(this@MultiPlayerLobbyActivity)
                        adapter = LobbyUsersAdapter("LOBBY", context, mFirestoreDatabase!!, usersJoined)
                        addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
                    }
                }
            })

        listenForUsers()
        listenForGameStart()

        if (playerType == "CREATOR") {
            setupQuestionsForTheQuiz()
        }

        lobbyStartButton!!.setOnClickListener {
            MultiPlayerDataRetrievalFacade(mFirestoreDatabase!!, this)
                .startMultiPlayerGame(gamePIN!!)
        }
    }

    private fun setupQuestionsForTheQuiz() {
        val quizTopic = intent.extras!!.getLong("questionsTopic")
        val quizDifficulty = intent.extras!!.getString("questionsDifficulty")
        val quizNumberOfQuestions = intent.extras!!.getString("questionsNumber")!!.toInt()

        QuestionsDataRetrievalFacade(mFirestoreDatabase!!, this)
            .getQuestionsForQuiz(object: QuestionsCallBack {
                override fun onCallback(value: ArrayList<ActiveQuestion>) {
                    val randomQuestions = randomizeQuestions(value, quizNumberOfQuestions)
                    for (question in randomQuestions) {
                        MultiPlayerDataRetrievalFacade(mFirestoreDatabase!!, this@MultiPlayerLobbyActivity)
                            .addQuestionToGame(gid!!, question.qid)
                    }
                }
            }, quizDifficulty!!, quizTopic)
    }

    private fun randomizeQuestions(questionBase: ArrayList<ActiveQuestion>, numberOfQuestions: Int): ArrayList<ActiveQuestion> {
        val randomQuestionPositions = ArrayList<Int>()
        var idx = 1
        while (idx <= numberOfQuestions && randomQuestionPositions.size < questionBase.size) {
            var randomValue = (0 until questionBase.size).random()
            while (randomValue in randomQuestionPositions) {
                randomValue = (0 until questionBase.size).random()
            }
            randomQuestionPositions.add(randomValue)
            idx += 1
        }
        val randomizedQuestions = ArrayList<ActiveQuestion>()
        for (i in randomQuestionPositions) {
            randomizedQuestions.add(questionBase[i])
        }
        return randomizedQuestions
    }

    private fun listenForUsers() {
        val usersCollection = mFirestoreDatabase!!.collection("Multi_Player_Users_Joined")
        usersCollection.addSnapshotListener{ snapshot, e ->
            if (e != null) {
                Toast.makeText(this, "Users could not be fetched! Please try again!", Toast.LENGTH_LONG).show()
                return@addSnapshotListener
            }

            for (changes in snapshot!!.documentChanges) {
                    val gameID = changes.document.data.get("gid") as String
                    if (gameID == gid) {
                        val uid = changes.document.data.get("uid") as String
                        val score = changes.document.data.get("score") as Long
                        val winner = changes.document.data.get("winner") as Boolean
                        val role = changes.document.data.get("role") as String
                        val changedUser = MultiPlayerUserJoined(gameID, uid, score, role, winner)
                        if (changes.type == DocumentChange.Type.ADDED && uid != FirebaseAuth.getInstance().uid) {
                            usersJoined.add(changedUser)
                        } else if (changes.type == DocumentChange.Type.REMOVED) {
                            var userRemoved: MultiPlayerUserJoined? = null
                            for (idx in (0 until usersJoined.size)) {
                                if (usersJoined[idx].uid == uid) {
                                    userRemoved = usersJoined[idx]
                                }
                            }
                            usersJoined.remove(userRemoved)
                        }
                        if (lobbyJoinedUsers!!.adapter != null) {
                            lobbyJoinedUsers!!.adapter!!.notifyDataSetChanged()
                        }
                    }
                }
            }
    }

    private fun listenForGameStart() {
        val gamesCollection = mFirestoreDatabase!!.collection("Multi_Player_Games")
        gamesCollection.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Toast.makeText(this, "Users could not be fetched! Please try again!", Toast.LENGTH_LONG).show()
                return@addSnapshotListener
            }

            for (changes in snapshot!!.documentChanges) {
                val gameID = changes.document.data.get("gid") as String
                if (gameID == gid) {
                    if (changes.type == DocumentChange.Type.MODIFIED) {
                        val gameIntent = Intent(this, MultiPlayerQuizActivity::class.java)
                        gameIntent.putExtra("gid", gid)
                        gameIntent.putExtra("gamePIN", gamePIN)
                        startActivity(gameIntent)
                    }
                }
            }
        }
    }

    private fun setupLayoutElements() {
        lobbyGamePIN = findViewById(R.id.lobby_game_pin)
        lobbyJoinedUsers = findViewById(R.id.lobby_joined_users)
        lobbyStartButton = findViewById(R.id.lobby_start_button)
    }

    override fun onBackPressed() {
        if (playerType != "CREATOR") {
            MultiPlayerDataRetrievalFacade(Firebase.firestore, this)
                .userLeavesGame(gid!!)
            super.onBackPressed()
        }
    }
}
