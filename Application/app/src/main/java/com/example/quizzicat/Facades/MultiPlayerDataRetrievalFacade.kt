package com.example.quizzicat.Facades

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.quizzicat.Model.MultiPlayerGame
import com.example.quizzicat.Model.MultiPlayerUserJoined
import com.example.quizzicat.Utils.MultiPlayerGamesCallBack
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

    fun createMultiPlayerGame() {
        val gid = UUID.randomUUID().toString()
        val active = true
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
                val newGame = MultiPlayerGame(gid, active, created_on, created_by, randomPIN.toString())
                firebaseFirestore.collection("Multi_Player_Games")
                    .document(newGame.gid)
                    .set(newGame)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            insertUserJoinedGame(newGame.gid, "CREATOR")
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
                        val gid = document.get("gid") as String
                        val created_on = document.get("created_by") as String
                        val created_by = document.get("created_on") as String
                        val game_pin = document.get("game_pin") as String
                        multiPlayerGames.add(MultiPlayerGame(gid, active, created_on, created_by, game_pin))
                    }
                    callback.onCallback(multiPlayerGames)
                } else {
                    Toast.makeText(context, task.exception!!.message.toString(), Toast.LENGTH_LONG).show()
                }
            }
    }

    fun getAllActiveGames(callback: MultiPlayerGamesCallBack) {
        firebaseFirestore.collection("Multi_Player_Games")
            .whereEqualTo("active", true)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val multiPlayerGames = ArrayList<MultiPlayerGame>()
                    for (document in task.result!!) {
                        val active = document.get("active") as Boolean
                        val gid = document.get("gid") as String
                        val created_on = document.get("created_by") as String
                        val created_by = document.get("created_on") as String
                        val game_pin = document.get("game_pin") as String
                        multiPlayerGames.add(MultiPlayerGame(gid, active, created_on, created_by, game_pin))
                    }
                    callback.onCallback(multiPlayerGames)
                } else {
                    Toast.makeText(context, task.exception!!.message.toString(), Toast.LENGTH_LONG).show()
                }
            }
    }
}