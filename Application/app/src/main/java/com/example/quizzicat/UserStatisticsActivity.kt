package com.example.quizzicat

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.example.quizzicat.Facades.ImageLoadingFacade
import com.example.quizzicat.Facades.UserDataRetrievalFacade
import com.example.quizzicat.Model.*
import com.example.quizzicat.Utils.CustomCallBack
import com.example.quizzicat.Utils.TopicsPlayedCallBack
import com.example.quizzicat.Utils.UserDataCallBack
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
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
    private var soloGamesCorrect: TextView? = null
    private var soloGamesIncorrect: TextView? = null
    private var noGamesPlayed: LinearLayout? = null
    private var gamesPlayed: LinearLayout? = null
    private var soloGamesPieCharts: LinearLayout? = null
    private var categoriesPieChart: PieChart? = null
    private var topicsPieChart: PieChart? = null

    private var topicsPlayed = ArrayList<Topic>()
    private var categoriesPlayed = ArrayList<TopicCategory>()
    private var topicsHistory = ArrayList<TopicPlayed>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_statistics)

        mFirebaseAuth = FirebaseAuth.getInstance()
        mFirestoreDatabase = Firebase.firestore

        setupLayoutElements()

        setUserProfileData()

        getUserPlayedHistory(object: TopicsPlayedCallBack {
            override fun onCallback(value: List<TopicPlayed>) {
                topicsHistory = value as ArrayList<TopicPlayed>
                if (topicsHistory.isEmpty()) {
                    progressBar!!.visibility = View.GONE
                    noGamesPlayed!!.visibility = View.VISIBLE
                    gamesPlayed!!.visibility = View.GONE
                    soloGamesPieCharts!!.visibility = View.GONE
                } else {
                    determineGamesPlayed(topicsHistory)
                    getTopicsPlayedData(object: CustomCallBack {
                        override fun onCallback(value: List<AbstractTopic>) {
                            topicsPlayed = value as ArrayList<Topic>
                            getCategoriesPlayedData(object: CustomCallBack {
                                override fun onCallback(value: List<AbstractTopic>) {
                                    categoriesPlayed = value as ArrayList<TopicCategory>
                                    createCategoriesPieChart(createCategoriesPieChartDataset(topicsHistory))
                                    createTopicsPieChart()
                                    noGamesPlayed!!.visibility = View.GONE
                                    gamesPlayed!!.visibility = View.VISIBLE
                                    soloGamesPieCharts!!.visibility = View.VISIBLE
                                    progressBar!!.visibility = View.GONE
                                }
                            }, topicsPlayed)
                        }
                    }, topicsHistory)
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
                    val cid = document.get("cid") as Long
                    val times_played_solo = document.get("times_played_solo") as Long
                    val uid = document.get("uid") as String
                    val topicPlayed = TopicPlayed(pid, tid, cid, uid, correct_answers, incorrect_answers, times_played_solo)
                    topicsPlayed.add(topicPlayed)
                }
                callback.onCallback(topicsPlayed)
            }
    }

    private fun determineGamesPlayed(topicsPlayed: ArrayList<TopicPlayed>) {
        var soloGamesPlayed = 0
        var soloCorrectAnswers = 0
        var soloIncorrectAnswers = 0
        for (topic in topicsPlayed) {
            soloGamesPlayed += topic.times_played_solo.toInt()
            soloCorrectAnswers += topic.correct_answers.toInt()
            soloIncorrectAnswers += topic.incorrect_answers.toInt()
        }
        soloGames!!.text = soloGamesPlayed.toString()
        soloGamesCorrect!!.text = soloCorrectAnswers.toString()
        soloGamesIncorrect!!.text = soloIncorrectAnswers.toString()
    }

    private fun getTopicsPlayedData(callback: CustomCallBack, topicsPlayed: ArrayList<TopicPlayed>) {
        val playedTopics = ArrayList<Long>()
        for (topic in topicsPlayed) {
            playedTopics.add(topic.tid)
        }
        mFirestoreDatabase!!.collection("Topics")
            .whereIn("tid", playedTopics)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val topics = ArrayList<Topic>()
                    for (document in task.result!!) {
                        val topicCID = document.get("cid") as Long
                        val topicTID = document.get("tid") as Long
                        val topicURL = document.get("icon_url") as String
                        val topicName = document.get("name") as String
                        val topic = Topic(topicTID, topicCID, topicURL, topicName)
                        topics.add(topic)
                    }
                    callback.onCallback(topics)
                } else {
                    Toast.makeText(this, task.exception!!.message.toString(), Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun getCategoriesPlayedData(callback: CustomCallBack, topicList: ArrayList<Topic>) {
        val playedCategories = ArrayList<Long>()
        for (topic in topicList) {
            playedCategories.add(topic.cid)
        }
        mFirestoreDatabase!!.collection("Topic_Categories")
            .whereIn("cid", playedCategories)
            .get()
            .addOnCompleteListener { task1 ->
                if (task1.isSuccessful) {
                    val categories = ArrayList<TopicCategory>()
                    for (document in task1.result!!) {
                        val topicCategoryID = document.get("cid") as Long
                        val topicCategoryURL = document.get("icon_url") as String
                        val topicCategoryName = document.get("name") as String
                        val topicCategory = TopicCategory(topicCategoryID, topicCategoryURL, topicCategoryName)
                        categories.add(topicCategory)
                    }
                    callback.onCallback(categories)
                } else {
                    Toast.makeText(this, task1.exception!!.message.toString(), Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun createCategoriesPieChartDataset(topicsPlayed: ArrayList<TopicPlayed>): HashMap<String, Int> {
        val categoriesMap = HashMap<String, Int>() // (name, times_played)
        for (topic in topicsPlayed) {
            val categoryName = getCategoryByID(topic.cid)
            if (!categoriesMap.containsKey(categoryName)) {
                categoriesMap[categoryName] = topic.times_played_solo.toInt()
            } else {
                val timesPlayed = categoriesMap[categoryName]!!.plus(topic.times_played_solo.toInt())
                categoriesMap[categoryName] = timesPlayed
            }
        }
        return categoriesMap
    }

    private fun createCategoriesPieChart(dataset: HashMap<String, Int>) {
        val pieEntries = ArrayList<PieEntry>()
        for (category in dataset.keys) {
            pieEntries.add(PieEntry(dataset[category]!!.toFloat(), category))
        }
        categoriesPieChart!!.description.isEnabled = false
        categoriesPieChart!!.isDrawHoleEnabled = true
        categoriesPieChart!!.setUsePercentValues(false)
        categoriesPieChart!!.setHoleColor(Color.WHITE)
        categoriesPieChart!!.transparentCircleRadius = 60f
        categoriesPieChart!!.animateY(1000, Easing.EaseInOutCubic)
        categoriesPieChart!!.setDrawEntryLabels(false)

        val pieDataSet = PieDataSet(pieEntries, "")
        pieDataSet.sliceSpace = 3f
        pieDataSet.selectionShift = 5f
        pieDataSet.colors = ColorTemplate.JOYFUL_COLORS.toList()
        pieDataSet.valueTextSize = 14f
        pieDataSet.valueTextColor = Color.WHITE
        categoriesPieChart!!.data = PieData((pieDataSet))
        categoriesPieChart!!.legend.isEnabled = true
        categoriesPieChart!!.legend.isWordWrapEnabled = true
        categoriesPieChart!!.invalidate()
    }

    private fun getCategoryByID(cid: Long) : String {
        for (category in categoriesPlayed) {
            if (category.cid == cid)
                return category.name
        }
        return ""
    }

    private fun createTopicsPieChart() {
        val pieEntries = ArrayList<PieEntry>()
        for (topic in topicsHistory) {
            pieEntries.add(PieEntry(topic.times_played_solo.toFloat(), getTopicByID(topic.tid)))
        }
        topicsPieChart!!.description.isEnabled = false
        topicsPieChart!!.isDrawHoleEnabled = true
        topicsPieChart!!.setUsePercentValues(false)
        topicsPieChart!!.setHoleColor(Color.WHITE)
        topicsPieChart!!.transparentCircleRadius = 60f
        topicsPieChart!!.animateY(1000, Easing.EaseInOutCubic)
        topicsPieChart!!.setDrawEntryLabels(false)

        val pieDataSet = PieDataSet(pieEntries, "")
        pieDataSet.sliceSpace = 3f
        pieDataSet.selectionShift = 5f
        pieDataSet.colors = ColorTemplate.PASTEL_COLORS.toList()
        pieDataSet.valueTextSize = 14f
        pieDataSet.valueTextColor = Color.WHITE
        topicsPieChart!!.data = PieData((pieDataSet))
        topicsPieChart!!.legend.isEnabled = true
        topicsPieChart!!.legend.isWordWrapEnabled = true
        topicsPieChart!!.invalidate()
    }

    private fun getTopicByID(tid: Long): String {
        for (topic in topicsPlayed) {
            if (topic.tid == tid)
                return topic.name
        }
        return ""
    }

    private fun setupLayoutElements() {
        userProfilePicture = findViewById(R.id.statistics_avatar)
        userDisplayName = findViewById(R.id.statistics_username)
        progressBar = findViewById(R.id.statistics_progress_bar)
        progressBar!!.visibility = View.VISIBLE
        soloGames = findViewById(R.id.statistics_solo_wins)
        soloGamesCorrect = findViewById(R.id.statistics_solo_correct)
        soloGamesIncorrect = findViewById(R.id.statistics_solo_wrong)
        noGamesPlayed = findViewById(R.id.layout_no_games_played)
        gamesPlayed = findViewById(R.id.layout_solo_games_played)
        soloGamesPieCharts = findViewById(R.id.layout_solo_pie_charts)
        categoriesPieChart = findViewById(R.id.solo_chart_categories)
        topicsPieChart = findViewById(R.id.solo_chart_topics)
    }
}
