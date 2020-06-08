package com.example.quizzicat.Facades

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.quizzicat.Model.MultiPlayerGame
import com.example.quizzicat.Model.MultiPlayerGameQuestion
import com.example.quizzicat.Model.MultiPlayerUserJoined
import com.example.quizzicat.Utils.CounterCallBack
import com.example.quizzicat.Utils.MultiPlayerGamesCallBack
import com.example.quizzicat.Utils.MultiPlayerQuestionsCallBack
import com.example.quizzicat.Utils.MultiPlayerUsersCallBack
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList

class MultiPlayerDataRetrievalFacade(val firebaseFirestore: FirebaseFirestore, val context: Context) {

    fun getUserPlayedGames() {
        val uid = FirebaseAuth.getInstance().uid
        if (uid != null) {
            firebaseFirestore.collection("Multi_Player_Users_Joined")
                .whereEqualTo("uid", uid)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val gamesJoined = ArrayList<MultiPlayerUserJoined>()
                        for (document in task.result!!) {
                            val gid = document.get("gid") as String
                            val role = document.get("role") as String
                            val score = document.get("score") as Long
                            val winner = document.get("winner") as Boolean
                            gamesJoined.add(MultiPlayerUserJoined(gid, role, score, uid, winner))
                        }
                    } else {
                        Toast.makeText(context, task.exception!!.message.toString(), Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    fun createMultiPlayerGame(callback: MultiPlayerGamesCallBack) {
        val gid = UUID.randomUUID().toString()
        val active = true
        val progress = false
        val created_on = LocalDateTime.now().toString().split("T")[0]
        val created_by = FirebaseAuth.getInstance().currentUser!!.uid
        getAllActiveGames(object: MultiPlayerGamesCallBack {
            override fun onCallback(value: ArrayList<MultiPlayerGame>) {
                var randomPIN = -1
                if (value.size == 0) {
                    randomPIN = (1000 until 9999).random()
                } else {
                    var foundPIN = false
                    while (!foundPIN) {
                        var isEqual = false
                        randomPIN = (1000 until 9999).random()
                        for (activeGame in value) {
                            if (activeGame.game_pin == randomPIN.toString())
                                isEqual = true
                            if (!isEqual) {
                                foundPIN = true
                            }
                        }
                    }
                }
                val newGame = MultiPlayerGame(gid, active, progress, created_on, created_by, randomPIN.toString())
                firebaseFirestore.collection("Multi_Player_Games")
                    .document(newGame.gid)
                    .set(newGame)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            insertUserJoinedGame(newGame.gid, "CREATOR")
                            val gameList = ArrayList<MultiPlayerGame>()
                            gameList.add(newGame)
                            callback.onCallback(gameList)
                        } else {
                            Toast.makeText(context, task.exception!!.message.toString(), Toast.LENGTH_LONG).show()
                        }
                    }
            }
        })
    }

    fun insertUserJoinedGame(game: String, role: String) {
        if (game.length == 4)  { // gamePIN was given, not GID
            getGamesByPIN(game, object: MultiPlayerGamesCallBack {
                override fun onCallback(value: ArrayList<MultiPlayerGame>) {
                    val userJoined = MultiPlayerUserJoined(value[0].gid, FirebaseAuth.getInstance().uid!!, 0, role, false)
                    firebaseFirestore.collection("Multi_Player_Users_Joined")
                        .add(userJoined)
                        .addOnCompleteListener { task ->
                            if (!task.isSuccessful) {
                                Toast.makeText(context, task.exception!!.message.toString(), Toast.LENGTH_LONG).show()
                            }
                        }
                }
            })
        } else {
            val userJoined = MultiPlayerUserJoined(game, FirebaseAuth.getInstance().uid!!, 0, role, false)
            firebaseFirestore.collection("Multi_Player_Users_Joined")
                .add(userJoined)
                .addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Toast.makeText(context, task.exception!!.message.toString(), Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    fun getGamesByPIN(pin: String, callback: MultiPlayerGamesCallBack) {
        firebaseFirestore.collection("Multi_Player_Games")
            .whereEqualTo("game_pin", pin)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val multiPlayerGames = ArrayList<MultiPlayerGame>()
                    for (document in task.result!!) {
                        val active = document.get("active") as Boolean
                        val progress = document.get("progress") as Boolean
                        val gid = document.get("gid") as String
                        val created_on = document.get("created_by") as String
                        val created_by = document.get("created_on") as String
                        val game_pin = document.get("game_pin") as String
                        multiPlayerGames.add(MultiPlayerGame(gid, active, progress, created_on, created_by, game_pin))
                    }
                    callback.onCallback(multiPlayerGames)
                } else {
                    Toast.makeText(context, task.exception!!.message.toString(), Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun getAllActiveGames(callback: MultiPlayerGamesCallBack) {
        firebaseFirestore.collection("Multi_Player_Games")
            .whereEqualTo("active", true)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val multiPlayerGames = ArrayList<MultiPlayerGame>()
                    for (document in task.result!!) {
                        val active = document.get("active") as Boolean
                        val progress = document.get("progress") as Boolean
                        val gid = document.get("gid") as String
                        val created_on = document.get("created_by") as String
                        val created_by = document.get("created_on") as String
                        val game_pin = document.get("game_pin") as String
                        multiPlayerGames.add(MultiPlayerGame(gid, active, progress, created_on, created_by, game_pin))
                    }
                    callback.onCallback(multiPlayerGames)
                } else {
                    Toast.makeText(context, task.exception!!.message.toString(), Toast.LENGTH_LONG).show()
                }
            }
    }

    fun userLeavesGame(gid: String) {
        val multiPlayerUsersCollection = firebaseFirestore.collection("Multi_Player_Users_Joined")
        firebaseFirestore.collection("Multi_Player_Users_Joined")
            .whereEqualTo("gid", gid)
            .whereEqualTo("uid", FirebaseAuth.getInstance().uid)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        multiPlayerUsersCollection.document(document.id).delete()
                    }
                } else {
                    Toast.makeText(context, task.exception!!.message.toString(), Toast.LENGTH_LONG)
                        .show()
                }
            }
    }

    fun getUsersForGame(gid: String, callback: MultiPlayerUsersCallBack) {
        firebaseFirestore.collection("Multi_Player_Users_Joined")
            .whereEqualTo("gid", gid)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val users = ArrayList<MultiPlayerUserJoined>()
                    for (document in task.result!!) {
                        val role = document.get("role") as String
                        val score = document.get("score") as Long
                        val winner = document.get("winner") as Boolean
                        val uid = document.get("uid") as String
                        users.add(MultiPlayerUserJoined(gid, uid, score, role, winner))
                    }
                    callback.onCallback(users)
                } else {
                    Toast.makeText(context, task.exception!!.message.toString(), Toast.LENGTH_LONG)
                        .show()
                }
            }
    }

    fun addQuestionToGame(gid: String, qid: String) {
        firebaseFirestore.collection("Multi_Player_Quiz_Questions")
            .add(MultiPlayerGameQuestion(qid, gid))
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Toast.makeText(context, task.exception!!.message.toString(), Toast.LENGTH_LONG)
                        .show()
                }
            }
    }

    fun startMultiPlayerGame(gamePIN: String) {
        getGamesByPIN(gamePIN, object: MultiPlayerGamesCallBack {
            override fun onCallback(value: ArrayList<MultiPlayerGame>) {
                val game = value[0]
                game.progress = true
                firebaseFirestore.collection("Multi_Player_Games")
                    .document(game.gid)
                    .set(game)
                    .addOnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            Toast.makeText(context, task.exception!!.message.toString(), Toast.LENGTH_LONG).show()
                        }
                    }
            }
        })
    }

    fun getQuestionsForQuiz(gid: String, callback: MultiPlayerQuestionsCallBack) {
        firebaseFirestore.collection("Multi_Player_Quiz_Questions")
            .whereEqualTo("gid", gid)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val gameQuestions = ArrayList<MultiPlayerGameQuestion>()
                    for (document in task.result!!) {
                        val qid = document.get("qid") as String
                        gameQuestions.add(MultiPlayerGameQuestion(qid, gid))
                    }
                    callback.onCallback(gameQuestions)
                } else {
                    Toast.makeText(context, task.exception!!.message.toString(), Toast.LENGTH_LONG).show()
                }
            }
    }
}