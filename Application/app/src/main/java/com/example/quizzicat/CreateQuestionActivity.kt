package com.example.quizzicat

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.Spinner
import android.widget.SpinnerAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.quizzicat.Adapters.TopicSpinnerAdapter
import com.example.quizzicat.Facades.TopicsDataRetrievalFacade
import com.example.quizzicat.Model.AbstractTopic
import com.example.quizzicat.Model.Topic
import com.example.quizzicat.Model.TopicCategory
import com.example.quizzicat.Utils.CustomCallBack
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class CreateQuestionActivity : AppCompatActivity() {

    private var DEFAULT_TOPIC = "https://firebasestorage.googleapis.com/v0/b/quizzicat-ca219.appspot.com/o/topic_default.png?alt=media&token=3c7894aa-681d-4c80-bbba-89aea5215ba9"

    private var mFirestoreDatabase: FirebaseFirestore? = null

    private var categoriesSpinner: Spinner? = null
    private var categoriesSpinnerValues = ArrayList<TopicSpinnerAdapter.TopicSpinnerItem>()
    private var topicsSpinner: Spinner? = null
    private var topicsSpinnerValues = ArrayList<TopicSpinnerAdapter.TopicSpinnerItem>()
    private var categoriesList = ArrayList<TopicCategory>()
    private var topicsList = ArrayList<Topic>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_question)

        mFirestoreDatabase = Firebase.firestore

        setupLayoutElements()

        setCategoriesSpinnerValues()

        categoriesSpinner!!.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) {
                if (position != 0) {
                    topicsSpinner!!.visibility = View.VISIBLE
                    val selectedCategory = categoriesList[position - 1]
                    setTopicsSpinnerValues(getCIDByName(selectedCategory.name))
                }
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {

            }
        }
    }

    private fun setupLayoutElements() {
        categoriesSpinner = findViewById(R.id.create_question_category)
        topicsSpinner = findViewById(R.id.create_question_topic)
    }

    private fun setCategoriesSpinnerValues() {
        TopicsDataRetrievalFacade(mFirestoreDatabase!!, this)
            .getTopicCategories(object: CustomCallBack {
                override fun onCallback(value: List<AbstractTopic>) {
                    categoriesList = value as ArrayList<TopicCategory>
                    categoriesSpinnerValues.add(TopicSpinnerAdapter.TopicSpinnerItem(DEFAULT_TOPIC, "Topic Category"))
                    for (category in categoriesList) {
                        categoriesSpinnerValues.add(TopicSpinnerAdapter.TopicSpinnerItem(category.icon_url, category.name))
                    }
                    categoriesSpinner!!.adapter = TopicSpinnerAdapter(applicationContext, categoriesSpinnerValues)
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

    private fun setTopicsSpinnerValues(CID: Long) {
        TopicsDataRetrievalFacade(mFirestoreDatabase!!, this)
            .getTopicsForACategory(object: CustomCallBack {
                override fun onCallback(value: List<AbstractTopic>) {
                    topicsList = value as ArrayList<Topic>
                    topicsSpinnerValues.clear()
                    topicsSpinnerValues.add(TopicSpinnerAdapter.TopicSpinnerItem(DEFAULT_TOPIC, "Topic"))
                    for (topic in topicsList) {
                        topicsSpinnerValues.add(TopicSpinnerAdapter.TopicSpinnerItem(topic.icon_url, topic.name))
                    }
                    topicsSpinner!!.adapter = TopicSpinnerAdapter(applicationContext, topicsSpinnerValues)
                }
            }, CID)
    }
}
