package com.example.quizzicat

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.quizzicat.Facades.ImageLoadingFacade
import com.example.quizzicat.Facades.TopicsDataRetrievalFacade
import com.example.quizzicat.Facades.UserDataRetrievalFacade
import com.example.quizzicat.Model.*
import com.example.quizzicat.Utils.CustomCallBack
import com.example.quizzicat.Utils.TopicsPlayedCallBack
import com.example.quizzicat.Utils.UserDataCallBack
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import de.hdodenhof.circleimageview.CircleImageView


class UserStatisticsActivity : AppCompatActivity() {

    private var mFirebaseAuth: FirebaseAuth? = null
    private var mFirestoreDatabase: FirebaseFirestore? = null
    private var topicsDataRetrievalFacade: TopicsDataRetrievalFacade? = null

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
    private var correctIncorrectBarLayout: LinearLayout? = null
    private var correctIncorrectBar: ProgressBar? = null

    private var topicsPlayed = ArrayList<Topic>()
    private var categoriesPlayed = ArrayList<TopicCategory>()
    private var topicsHistory = ArrayList<TopicPlayed>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_statistics)

        mFirebaseAuth = FirebaseAuth.getInstance()
        mFirestoreDatabase = Firebase.firestore
        topicsDataRetrievalFacade = TopicsDataRetrievalFacade(mFirestoreDatabase!!, this)

        setupLayoutElements()

        setUserProfileData()

        val topicsDataRetrievalFacade = TopicsDataRetrievalFacade(mFirestoreDatabase!!, this)

        topicsDataRetrievalFacade
            .getUserPlayedHistory(object : TopicsPlayedCallBack {
                override fun onCallback(value: List<TopicPlayed>) {
                    topicsHistory = value as ArrayList<TopicPlayed>
                    if (topicsHistory.isEmpty()) {
                        progressBar!!.visibility = View.GONE
                        noGamesPlayed!!.visibility = View.VISIBLE
                        gamesPlayed!!.visibility = View.GONE
                        soloGamesPieCharts!!.visibility = View.GONE
                        correctIncorrectBarLayout!!.visibility = View.GONE
                    } else {
                        determineGamesPlayed(topicsHistory)
                        topicsDataRetrievalFacade.getTopicsPlayedData(object : CustomCallBack {
                            override fun onCallback(value: List<AbstractTopic>) {
                                topicsPlayed = value as ArrayList<Topic>
                                topicsDataRetrievalFacade.getCategoriesPlayedData(object :
                                    CustomCallBack {
                                    override fun onCallback(value: List<AbstractTopic>) {
                                        categoriesPlayed = value as ArrayList<TopicCategory>
                                        createCategoriesPieChart(
                                            createCategoriesPieChartDataset(
                                                topicsHistory
                                            )
                                        )
                                        createTopicsPieChart()
                                        createCorrectIncorrectBar()
                                        noGamesPlayed!!.visibility = View.GONE
                                        gamesPlayed!!.visibility = View.VISIBLE
                                        soloGamesPieCharts!!.visibility = View.VISIBLE
                                        correctIncorrectBarLayout!!.visibility = View.VISIBLE
                                        progressBar!!.visibility = View.GONE
                                    }
                                }, topicsPlayed)
                            }
                        }, topicsHistory)
                    }
                }
            })

        categoriesPieChart!!.setOnChartValueSelectedListener(object: OnChartValueSelectedListener {
            override fun onNothingSelected() {
                createTopicsPieChart()
            }

            override fun onValueSelected(e: Entry?, h: Highlight?) {
                val selectedEntry = e as PieEntry
                val selectedCategory = getCIDByName(selectedEntry.label)
                updateTopicsPieChartCategory(selectedCategory)
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
        setupPieChart(categoriesPieChart!!, pieEntries, "JOY")
    }

    private fun getCategoryByID(cid: Long) : String {
        for (category in categoriesPlayed) {
            if (category.cid == cid)
                return category.name
        }
        return ""
    }

    private fun getCIDByName(name: String): Long {
        for (category in categoriesPlayed) {
            if (category.name == name)
                return category.cid
        }
        return -1
    }

    private fun createTopicsPieChart() {
        val pieEntries = ArrayList<PieEntry>()
        for (topic in topicsHistory) {
            pieEntries.add(PieEntry(topic.times_played_solo.toFloat(), getTopicByID(topic.tid)))
        }
        setupPieChart(topicsPieChart!!, pieEntries, "PASTEL")
    }

    private fun getTopicByID(tid: Long): String {
        for (topic in topicsPlayed) {
            if (topic.tid == tid)
                return topic.name
        }
        return ""
    }

    private fun createCorrectIncorrectBar() {
        var correctAnswers = 0
        var incorrectAnswers = 0
        for (topicPlayed in topicsHistory) {
           correctAnswers += topicPlayed.correct_answers.toInt()
           incorrectAnswers += topicPlayed.incorrect_answers.toInt()
        }
        val totalQuestions = correctAnswers + incorrectAnswers
        correctIncorrectBar!!.max = totalQuestions
        correctIncorrectBar!!.progress = correctAnswers
    }

    private fun updateTopicsPieChartCategory(cid: Long) {
        val pieEntries = ArrayList<PieEntry>()
        for (topic in topicsHistory) {
            if (topic.cid == cid) {
                pieEntries.add(PieEntry(topic.times_played_solo.toFloat(), getTopicByID(topic.tid)))
            }
        }
        setupPieChart(topicsPieChart!!, pieEntries, "PASTEL")
    }

    private fun setupPieChart(pieChart: PieChart, pieEntries: ArrayList<PieEntry>, colors: String) {
        pieChart.description.isEnabled = false
        pieChart.isDrawHoleEnabled = true
        pieChart.setUsePercentValues(false)
        pieChart.setHoleColor(Color.WHITE)
        pieChart.transparentCircleRadius = 60f
        pieChart.animateY(1000, Easing.EaseInOutCubic)
        pieChart.setDrawEntryLabels(false)

        val pieDataSet = PieDataSet(pieEntries, "")
        pieDataSet.sliceSpace = 3f
        pieDataSet.selectionShift = 5f
        if (colors == "JOY") {
            pieDataSet.colors = ColorTemplate.JOYFUL_COLORS.toList()
        } else {
            pieDataSet.colors = ColorTemplate.PASTEL_COLORS.toList()
        }
        pieDataSet.valueTextSize = 14f
        pieDataSet.valueTextColor = Color.WHITE
        pieChart.data = PieData((pieDataSet))
        pieChart.legend.isEnabled = true
        pieChart.legend.isWordWrapEnabled = true
        pieChart.invalidate()
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
        correctIncorrectBarLayout = findViewById(R.id.layout_correct_answers_bar)
        correctIncorrectBar = findViewById(R.id.answers_correct_incorrect_bar)
    }
}
