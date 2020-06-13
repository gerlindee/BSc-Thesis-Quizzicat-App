package com.example.quizzicat.Fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import android.widget.ProgressBar
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.quizzicat.Adapters.TopicCategoriesAdapter
import com.example.quizzicat.Facades.TopicsDataRetrievalFacade
import com.example.quizzicat.Model.ModelEntity
import com.example.quizzicat.Model.Topic
import com.example.quizzicat.Model.TopicCategory
import com.example.quizzicat.R
import com.example.quizzicat.SoloQuizActivity
import com.example.quizzicat.Utils.ModelArrayCallback
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_topic_categories.*

class TopicCategoriesFragment : Fragment() {

    private var topicCategoriesGridView: GridView ? = null
    private var topicCategoriesAdapter: TopicCategoriesAdapter ? = null
    private var topicCategoriesList: ArrayList<TopicCategory> ? = null
    private var topicsList: ArrayList<Topic> ? = null
    private var mFirestoreDatabase: FirebaseFirestore? = null
    private var topicsLevel: Boolean = false
    private var topicsDataRetrievalFacade: TopicsDataRetrievalFacade? = null
    private var progressIndicator: ProgressBar? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_topic_categories, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mFirestoreDatabase = Firebase.firestore
        topicsDataRetrievalFacade = TopicsDataRetrievalFacade(mFirestoreDatabase!!, context!!)

        topicCategoriesGridView = view.findViewById(R.id.categories_grid_view)
        progressIndicator = view.findViewById(R.id.topics_progress_bar)

        setCategoriesData()

        topicCategoriesGridView?.setOnItemClickListener { _, _, position, _ ->
            if (topicsLevel) {
                val inflater = LayoutInflater.from(context)
                val customizingQuizView = inflater.inflate(R.layout.view_customize_solo_quiz, null)
                val selectedDifficulty : Spinner = customizingQuizView.findViewById(R.id.customize_quiz_difficulty)
                val selectedNumberOfQuestions : Spinner = customizingQuizView.findViewById(R.id.customize_quiz_number)

                AlertDialog.Builder(context!!)
                    .setView(customizingQuizView)
                    .setPositiveButton("Let's play") { _, _ ->
                        val soloQuizIntent = Intent(activity, SoloQuizActivity::class.java)
                        soloQuizIntent.putExtra("questionsDifficulty", selectedDifficulty.selectedItem.toString())
                        soloQuizIntent.putExtra("questionsNumber", selectedNumberOfQuestions.selectedItem.toString())
                        soloQuizIntent.putExtra("questionsTopic", topicsList!![position].tid)
                        soloQuizIntent.putExtra("questionsCategory", topicsList!![position].cid)
                        startActivity(soloQuizIntent)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            } else {
                val selectedCategory = topicCategoriesList!![position]
                topicsLevel = true
                topicsDataRetrievalFacade!!.getTopicsForACategory(object: ModelArrayCallback {
                    override fun onCallback(value: List<ModelEntity>) {
                        topicsList = value as ArrayList<Topic>
                        topicCategoriesAdapter!!.arrayList = topicsList as ArrayList<ModelEntity>
                        topicCategoriesAdapter!!.notifyDataSetChanged()
                    }
                }, selectedCategory.cid)
                categories_go_back.visibility = View.VISIBLE
            }
        }

        categories_go_back.setOnClickListener {
            setCategoriesData()
            topicsLevel = false
            categories_go_back.visibility = View.GONE
        }
    }

    private fun setCategoriesData() {
        progressIndicator!!.visibility = View.VISIBLE
        topicCategoriesGridView!!.visibility = View.GONE
        topicsDataRetrievalFacade!!.getTopicCategories(object: ModelArrayCallback {
            override fun onCallback(value: List<ModelEntity>) {
                topicCategoriesList = value as ArrayList<TopicCategory>
                topicCategoriesAdapter = TopicCategoriesAdapter(context!!, topicCategoriesList as ArrayList<ModelEntity>)
                topicCategoriesGridView?.adapter = topicCategoriesAdapter
                progressIndicator!!.visibility = View.GONE
                topicCategoriesGridView!!.visibility = View.VISIBLE
            }
        })
    }

}
