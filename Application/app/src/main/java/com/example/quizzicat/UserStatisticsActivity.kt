package com.example.quizzicat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.example.quizzicat.Facades.ImageLoadingFacade
import com.example.quizzicat.Facades.UserDataRetrievalFacade
import com.example.quizzicat.Model.TopicPlayed
import com.example.quizzicat.Model.User
import com.example.quizzicat.Utils.TopicsPlayedCallBack
import com.example.quizzicat.Utils.UserDataCallBack
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import de.hdodenhof.circleimageview.CircleImageView

class UserStatisticsActivity : AppCompatActivity() {

    private var mFirebaseAuth: FirebaseAuth? = null
    private var mFirestoreDatabase: FirebaseFirestore? = null

    private var userProfilePicture: CircleImageView? = null
    private var userDisplayName: TextView? = null
    private var progressBar: ProgressBar? = null
    private var soloGames: TextView? = null
    private var groupWins: TextView? = null
    private var groupGames: TextView? = null
    private var noGamesPlayed: LinearLayout? = null
    private var gamesPlayed: LinearLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_statistics)

        mFirebaseAuth = FirebaseAuth.getInstance()
        mFirestoreDatabase = Firebase.firestore

        setupLayoutElements()

        setUserProfileData()

        getUserPlayedHistory(object: TopicsPlayedCallBack {
            override fun onCallback(value: List<TopicPlayed>) {
                if (value.isEmpty()) {
                    noGamesPlayed!!.visibility = View.VISIBLE
                } else {
                    gamesPlayed!!.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun setUserProfileData() {
        UserDataRetrievalFacade(mFirestoreDatabase!!, mFirebaseAuth!!.currentUser!!.uid)
            .getUserDetails(object : UserDataCallBack {
                override fun onCallback(value: User) {
                    ImageLoadingFacade(this@UserStatisticsActivity).loadImageIntoCircleView(value.avatar_url, userProfilePicture!!)
                    userDisplayName!!.text = value.display_name
                    progressBar!!.visibility = View.GONE
                }
            })
    }

    private fun getUserPlayedHistory(callback: TopicsPlayedCallBack) {
        mFirestoreDatabase!!.collection("Topics_Played")
            .whereEqualTo("uid", mFirebaseAuth!!.currentUser!!.uid)
            .get()
            .addOnCompleteListener { task ->
                val topicsPlayed = ArrayList<TopicPlayed>()
                for (document in task.result!!) {
                    val correct_answers = document.get("correct_answers") as Long
                    val incorrect_answers = document.get("incorrect_answers") as Long
                    val pid = document.get("pid") as String
                    val tid = document.get("tid") as Long
                    val times_played_multi = document.get("times_played_multi") as Long
                    val times_played_solo = document.get("times_played_solo") as Long
                    val times_won = document.get("times_won") as Long
                    val uid = document.get("uid") as String
                    val topicPlayed = TopicPlayed(pid, tid, uid, correct_answers, incorrect_answers, times_played_solo, times_played_multi, times_won)
                    topicsPlayed.add(topicPlayed)
                }
                callback.onCallback(topicsPlayed)
            }
    }

    private fun setupLayoutElements() {
        userProfilePicture = findViewById(R.id.statistics_avatar)
        userDisplayName = findViewById(R.id.statistics_username)
        progressBar = findViewById(R.id.statistics_progress_bar)
        progressBar!!.visibility = View.VISIBLE
        soloGames = findViewById(R.id.statistics_solo_wins)
        groupGames = findViewById(R.id.statistics_multi_plays)
        groupWins = findViewById(R.id.statistics_multi_wins)
        noGamesPlayed = findViewById(R.id.layout_no_games_played)
        gamesPlayed = findViewById(R.id.layout_games_played)
    }
}
