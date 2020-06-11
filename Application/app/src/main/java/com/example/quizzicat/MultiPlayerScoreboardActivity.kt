package com.example.quizzicat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quizzicat.Adapters.LobbyUsersAdapter
import com.example.quizzicat.Facades.MultiPlayerDataRetrievalFacade
import com.example.quizzicat.Model.MultiPlayerUserJoined
import com.example.quizzicat.Utils.MultiPlayerUsersCallBack
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MultiPlayerScoreboardActivity : AppCompatActivity() {

    private var mFirestoreDatabase: FirebaseFirestore? = null

    private var scoreboardGamePIN: TextView? = null
    private var scoreboardUsers: RecyclerView? = null
    private var scoreboardExitButton: MaterialButton? = null
    private var gamePIN: String? = ""
    private var gid: String? = null
    private var usersJoined = ArrayList<MultiPlayerUserJoined>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multi_player_scoreboard)

        mFirestoreDatabase = Firebase.firestore

        setupLayoutElements()

        gamePIN = intent.extras!!.getString("gamePIN")
        gid = intent.extras!!.getString("gid")

        scoreboardGamePIN!!.text = gamePIN

        MultiPlayerDataRetrievalFacade(mFirestoreDatabase!!, this)
            .getUsersForGame(gid!!, object: MultiPlayerUsersCallBack {
                override fun onCallback(value: ArrayList<MultiPlayerUserJoined>) {
                    for (user in value) {
                        if (user.score != 0.toLong() && user.uid != FirebaseAuth.getInstance().uid)
                            usersJoined.add(user)
                    }
                    usersJoined = ArrayList(usersJoined.sortedWith(compareBy(({ it.score }))))
                    scoreboardUsers!!.apply {
                        layoutManager = LinearLayoutManager(this@MultiPlayerScoreboardActivity)
                        adapter = LobbyUsersAdapter("SCOREBOARD", context, mFirestoreDatabase!!, usersJoined)
                        addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
                    }
                }
            })

        scoreboardExitButton!!.setOnClickListener {
            userLeavesScreen()
            val mainMenuIntent = Intent(this, MainMenuActivity::class.java)
            startActivity(mainMenuIntent)
        }

        listenForUsers()
    }

    private fun listenForUsers() {
        val usersCollection = mFirestoreDatabase!!.collection("Multi_Player_Users_Joined")
        usersCollection.addSnapshotListener(this) { snapshot, e ->
            if (e != null) {
                Toast.makeText(this, "Users could not be fetched! Please try again!", Toast.LENGTH_LONG).show()
                return@addSnapshotListener
            }

            for (changes in snapshot!!.documentChanges) {
                val gameID = changes.document.data.get("gid") as String
                if (gameID == gid) {
                    val uid = changes.document.data.get("uid") as String
                    val score = changes.document.data.get("score") as Long
                    val role = changes.document.data.get("role") as String
                    val finished_playing = changes.document.data.get("finished_playing") as Boolean
                    val changedUser = MultiPlayerUserJoined(gameID, uid, score, role, finished_playing)
                    if (changes.type == DocumentChange.Type.MODIFIED) {
                        usersJoined.add(changedUser)
                        usersJoined = ArrayList(usersJoined.sortedWith(compareBy(({ it.score }))))
                    }
                    if (scoreboardUsers!!.adapter != null) {
                        scoreboardUsers!!.adapter!!.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    private fun userLeavesScreen() {
        var userRemoved: MultiPlayerUserJoined? = null
        for (idx in (0 until usersJoined.size)) {
            if (usersJoined[idx].uid == FirebaseAuth.getInstance().uid) {
                userRemoved = usersJoined[idx]
            }
        }
        usersJoined.remove(userRemoved)
        if (scoreboardUsers!!.adapter != null) {
            scoreboardUsers!!.adapter!!.notifyDataSetChanged()
        }
        if (usersJoined.size == 0) {
            MultiPlayerDataRetrievalFacade(mFirestoreDatabase!!, this)
                .endGame(gamePIN!!)
        }
    }

    private fun setupLayoutElements() {
        scoreboardGamePIN = findViewById(R.id.scoreboard_game_pin)
        scoreboardUsers = findViewById(R.id.scoreboard_users)
        scoreboardExitButton = findViewById(R.id.scoreboard_leave_game)
    }

    override fun onBackPressed() {
        val mainMenuIntent = Intent(this, MainMenuActivity::class.java)
        startActivity(mainMenuIntent)
    }
}
