package com.example.quizzicat.Fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.quizzicat.Adapters.TopicSpinnerAdapter
import com.example.quizzicat.Facades.MultiPlayerDataRetrievalFacade
import com.example.quizzicat.Facades.TopicsDataRetrievalFacade
import com.example.quizzicat.Model.AbstractTopic
import com.example.quizzicat.Model.Topic
import com.example.quizzicat.Model.TopicCategory

import com.example.quizzicat.R
import com.example.quizzicat.SoloQuizActivity
import com.example.quizzicat.Utils.CustomCallBack
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.ArrayList

class MultiPlayerMenuFragment : Fragment() {

    private var mFirestoreDatabase: FirebaseFirestore? = null

    private var createNewGame: MaterialButton? = null
    private var joinGame: MaterialButton? = null
    private var leaderboardResults: RecyclerView? = null
    private var progressBar: ProgressBar? = null
    private var noGamesLayout: LinearLayout? = null

    private var DEFAULT_TOPIC = "https://firebasestorage.googleapis.com/v0/b/quizzicat-ca219.appspot.com/o/topic_default.png?alt=media&token=3c7894aa-681d-4c80-bbba-89aea5215ba9"
    private var categoriesList = ArrayList<TopicCategory>()
    private var categoriesSpinnerValues = ArrayList<TopicSpinnerAdapter.TopicSpinnerItem>()
    private var topicsList = ArrayList<Topic>()
    private var topicsSpinnerValues = ArrayList<TopicSpinnerAdapter.TopicSpinnerItem>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_multi_player_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mFirestoreDatabase = Firebase.firestore

        initializeLayoutElements()

        noGamesLayout!!.visibility = View.VISIBLE

        createNewGame!!.setOnClickListener {
            val inflater = LayoutInflater.from(context)
            val customizingQuizView = inflater.inflate(R.layout.view_customize_multi_quiz, null)
            val selectedCategory: Spinner = customizingQuizView.findViewById(R.id.customize_multi_quiz_category)
            val selectedTopic: Spinner = customizingQuizView.findViewById(R.id.customize_multi_quiz_topic)
            val selectedDifficulty : Spinner = customizingQuizView.findViewById(R.id.customize_multi_quiz_difficulty)
            val selectedNumberOfQuestions : Spinner = customizingQuizView.findViewById(R.id.customize_multi_quiz_number)
            var selectedTopicItem: Topic
            var selectedCategoryItem: TopicCategory
            setCategoriesSpinnerValues(selectedCategory)

            selectedCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) {
                    if (position != 0) {
                        selectedCategoryItem = categoriesList[position - 1]
                        setTopicsSpinnerValues(selectedTopic, getCIDByName(selectedCategoryItem.name))
                    }
                }

                override fun onNothingSelected(parentView: AdapterView<*>?) {

                }
            }

            selectedTopic.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) {
                    if (position != 0) {
                        selectedTopicItem = topicsList[position - 1]
                    }
                }

                override fun onNothingSelected(parentView: AdapterView<*>?) {

                }
            }

            AlertDialog.Builder(context!!)
                .setView(customizingQuizView)
                .setPositiveButton("Create Game") { _, _ ->
                    MultiPlayerDataRetrievalFacade(mFirestoreDatabase!!, context!!)
                        .createMultiPlayerGame()
//                    val soloQuizIntent = Intent(activity, SoloQuizActivity::class.java)
//                    soloQuizIntent.putExtra("questionsDifficulty", selectedDifficulty.selectedItem.toString())
//                    soloQuizIntent.putExtra("questionsNumber", selectedNumberOfQuestions.selectedItem.toString())
//                    startActivity(soloQuizIntent)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun initializeLayoutElements() {
        createNewGame = view?.findViewById(R.id.create_multi_player_game)
        joinGame = view?.findViewById(R.id.join_multi_player_game)
        progressBar = view?.findViewById(R.id.multi_player_progress_bar)
        noGamesLayout = view?.findViewById(R.id.layout_no_games_played)
        leaderboardResults = view?.findViewById(R.id.multi_player_games_played)
    }

    private fun getCIDByName(name: String): Long {
        for (category in categoriesList) {
            if (category.name == name)
                return category.cid
        }
        return -1
    }

    private fun setCategoriesSpinnerValues(categoriesSpinner: Spinner) {
        TopicsDataRetrievalFacade(mFirestoreDatabase!!, context!!)
            .getTopicCategories(object: CustomCallBack {
                override fun onCallback(value: List<AbstractTopic>) {
                    categoriesList = value as ArrayList<TopicCategory>
                    categoriesSpinnerValues.add(TopicSpinnerAdapter.TopicSpinnerItem(DEFAULT_TOPIC, "Topic Category"))
                    for (category in categoriesList) {
                        categoriesSpinnerValues.add(TopicSpinnerAdapter.TopicSpinnerItem(category.icon_url, category.name))
                    }
                    categoriesSpinner.adapter = TopicSpinnerAdapter(context!!, categoriesSpinnerValues)
                }
            })
    }

    private fun setTopicsSpinnerValues(topicsSpinner: Spinner, CID: Long) {
        TopicsDataRetrievalFacade(mFirestoreDatabase!!, context!!)
            .getTopicsForACategory(object: CustomCallBack {
                override fun onCallback(value: List<AbstractTopic>) {
                    topicsList = value as ArrayList<Topic>
                    topicsSpinnerValues.clear()
                    topicsSpinnerValues.add(TopicSpinnerAdapter.TopicSpinnerItem(DEFAULT_TOPIC, "Topic"))
                    for (topic in topicsList) {
                        topicsSpinnerValues.add(TopicSpinnerAdapter.TopicSpinnerItem(topic.icon_url, topic.name))
                    }
                    topicsSpinner.adapter = TopicSpinnerAdapter(context!!, topicsSpinnerValues)
                    topicsSpinner.visibility = View.VISIBLE
                }
            }, CID)
    }

}
