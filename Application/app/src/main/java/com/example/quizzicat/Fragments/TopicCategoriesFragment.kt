package com.example.quizzicat.Fragments

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.quizzicat.Adapters.TopicCategoriesAdapter
import com.example.quizzicat.Model.AbstractTopic
import com.example.quizzicat.Model.Topic
import com.example.quizzicat.Model.TopicCategory
import com.example.quizzicat.R
import com.example.quizzicat.SoloQuizActivity
import com.example.quizzicat.Utils.CustomCallBack
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_topic_categories, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mFirestoreDatabase = Firebase.firestore

        topicCategoriesGridView = view.findViewById(R.id.categories_grid_view)
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
                        soloQuizIntent.putExtra("questionsTopic", topicsList!![position].TID)
                        startActivity(soloQuizIntent)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            } else {
                val selectedCategory = topicCategoriesList!![position]
                getTopicsForCategory(object: CustomCallBack {
                    override fun onCallback(value: List<AbstractTopic>) {
                        topicsList = value as ArrayList<Topic>
                        topicCategoriesAdapter!!.arrayList = topicsList as ArrayList<AbstractTopic>
                        topicCategoriesAdapter!!.notifyDataSetChanged()
                    }
                }, selectedCategory.CID)
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
        getCategories(object: CustomCallBack {
            override fun onCallback(value: List<AbstractTopic>) {
                topicCategoriesList = value as ArrayList<TopicCategory>
                topicCategoriesAdapter = TopicCategoriesAdapter(context!!, topicCategoriesList as ArrayList<AbstractTopic>)
                topicCategoriesGridView?.adapter = topicCategoriesAdapter
            }
        })
    }

    private fun getCategories(myCallback: CustomCallBack) {
        mFirestoreDatabase!!.collection("Topic_Categories")
            .orderBy("Name")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val topicCategories = ArrayList<TopicCategory>()
                    for (document in task.result!!) {
                        val topicCategoryID = document.get("CID") as Long
                        val topicCategoryURL = document.get("Icon_URL") as String
                        val topicCategoryName = document.get("Name") as String
                        val topicCategory = TopicCategory(topicCategoryID, topicCategoryURL, topicCategoryName)
                        topicCategories.add(topicCategory)
                    }
                    myCallback.onCallback(topicCategories)
                } else {
                    Log.d("TopicCategoryQuery", task.exception.toString())
                }
            }
    }

    private fun getTopicsForCategory(myCallback: CustomCallBack, selectedCategory: Long) {
        topicsLevel = true
        mFirestoreDatabase!!.collection("Topics")
            .whereEqualTo("CID", selectedCategory)
            .orderBy("Name")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val topics = ArrayList<Topic>()
                    for (document in task.result!!) {
                        val topicCID = document.get("CID") as Long
                        val topicTID = document.get("TID") as Long
                        val topicURL = document.get("Icon_URL") as String
                        val topicName = document.get("Name") as String
                        val topic = Topic(topicTID, topicCID, topicURL, topicName)
                        topics.add(topic)
                    }
                    myCallback.onCallback(topics)
                } else {
                    Log.d("TopicQuery", task.exception.toString())
                }
            }
    }

}
