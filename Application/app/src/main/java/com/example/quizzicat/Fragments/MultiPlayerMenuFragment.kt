package com.example.quizzicat.Fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quizzicat.Adapters.MultiPlayerGamesAdapter
import com.example.quizzicat.Adapters.TopicSpinnerAdapter
import com.example.quizzicat.Facades.MultiPlayerDataRetrievalFacade
import com.example.quizzicat.Facades.TopicsDataRetrievalFacade
import com.example.quizzicat.Model.*
import com.example.quizzicat.MultiPlayerLobbyActivity

import com.example.quizzicat.R
import com.example.quizzicat.SoloQuizActivity
import com.example.quizzicat.Utils.CounterCallBack
import com.example.quizzicat.Utils.CustomCallBack
import com.example.quizzicat.Utils.MultiPlayerGamesCallBack
import com.example.quizzicat.Utils.MultiPlayerUsersCallBack
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.view_customize_multi_quiz.*
import org.w3c.dom.Text
import java.util.*
import kotlin.collections.ArrayList

class MultiPlayerMenuFragment : Fragment() {

    private var mFirestoreDatabase: FirebaseFirestore? = null

    private var createNewGame: MaterialButton? = null
    private var joinGame: MaterialButton? = null
    private var gamesPlayed: RecyclerView? = null
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

        setupGames()

        joinGame!!.setOnClickListener {
            val inflater = LayoutInflater.from(context)
            val joinView = inflater.inflate(R.layout.view_join_multi_player_game, null)
            val gamePIN: TextInputEditText = joinView.findViewById(R.id.join_game_pin)

            AlertDialog.Builder(context!!)
                .setView(joinView)
                .setPositiveButton("Join Game") { _, _ ->
                    val dataAgent = MultiPlayerDataRetrievalFacade(mFirestoreDatabase!!, context!!)
                    dataAgent.getGamesByPIN(gamePIN.text.toString(), object: MultiPlayerGamesCallBack {
                        override fun onCallback(value: ArrayList<MultiPlayerGame>) {
                            if (value.size == 0) {
                                Toast.makeText(context, "The given PIN could not be recognised! Please check with the game host and try again.", Toast.LENGTH_LONG).show()
                            } else {
                                dataAgent.insertUserJoinedGame(gamePIN.text.toString(), "PLAYER")
                                val lobbyIntent = Intent(activity, MultiPlayerLobbyActivity::class.java)
                                lobbyIntent.putExtra("gamePIN", gamePIN.text.toString())
                                lobbyIntent.putExtra("gid", value[0].gid)
                                lobbyIntent.putExtra("userRole", "PLAYER")
                                startActivity(lobbyIntent)
                            }

                        }
                    })
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        createNewGame!!.setOnClickListener {
            val inflater = LayoutInflater.from(context)
            val customizingQuizView = inflater.inflate(R.layout.view_customize_multi_quiz, null)
            val selectedCategory: Spinner = customizingQuizView.findViewById(R.id.customize_multi_quiz_category)
            val selectedTopic: Spinner = customizingQuizView.findViewById(R.id.customize_multi_quiz_topic)
            val selectedDifficulty : Spinner = customizingQuizView.findViewById(R.id.customize_multi_quiz_difficulty)
            val selectedNumberOfQuestions : Spinner = customizingQuizView.findViewById(R.id.customize_multi_quiz_number)
            var selectedTopicItem: Topic = Topic(0, 0, "", "")
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
                        .createMultiPlayerGame(selectedTopicItem.tid, object: MultiPlayerGamesCallBack{
                            override fun onCallback(value: ArrayList<MultiPlayerGame>) {
                                val lobbyIntent = Intent(activity, MultiPlayerLobbyActivity::class.java)
                                lobbyIntent.putExtra("gamePIN", value[0].game_pin)
                                lobbyIntent.putExtra("gid", value[0].gid)
                                lobbyIntent.putExtra("userRole", "CREATOR")
                                lobbyIntent.putExtra("questionsTopic", selectedTopicItem.tid)
                                lobbyIntent.putExtra("questionsDifficulty", selectedDifficulty.selectedItem.toString())
                                lobbyIntent.putExtra("questionsNumber", selectedNumberOfQuestions.selectedItem.toString())
                                startActivity(lobbyIntent)
                            }
                        })
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
        gamesPlayed = view?.findViewById(R.id.multi_player_games_played)
    }

    private fun setupGames() {
        MultiPlayerDataRetrievalFacade(mFirestoreDatabase!!, context!!)
            .getUserPlayedGames(object: MultiPlayerUsersCallBack {
                override fun onCallback(value: ArrayList<MultiPlayerUserJoined>) {
                    if (value.size == 0) {
                        createNewGame!!.visibility = View.VISIBLE
                        joinGame!!.visibility = View.VISIBLE
                        noGamesLayout!!.visibility = View.VISIBLE
                        progressBar!!.visibility = View.GONE
                    } else {
                        gamesPlayed!!.apply {
                            layoutManager = LinearLayoutManager(activity)
                            adapter = MultiPlayerGamesAdapter(context, mFirestoreDatabase!!, value)
                            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
                        }
                        createNewGame!!.visibility = View.VISIBLE
                        joinGame!!.visibility = View.VISIBLE
                        gamesPlayed!!.visibility = View.VISIBLE
                        progressBar!!.visibility = View.GONE
                    }
                }
            })
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
