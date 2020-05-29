package com.example.quizzicat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Spinner
import com.example.quizzicat.Adapters.TopicSpinnerAdapter
import com.example.quizzicat.Facades.TopicsDataRetrievalFacade
import com.example.quizzicat.Model.AbstractTopic
import com.example.quizzicat.Model.TopicCategory
import com.example.quizzicat.Utils.CustomCallBack
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class CreateQuestionActivity : AppCompatActivity() {

    private var mFirestoreDatabase: FirebaseFirestore? = null

    private var categoriesSpinner: Spinner? = null
    private var topicsSpinner: Spinner? = null

    private var categoriesList: ArrayList<TopicCategory>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_question)

        mFirestoreDatabase = Firebase.firestore

        setupLayoutElements()

        setCategoriesSpinnerValues()
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
                    val spinnerValues = ArrayList<TopicSpinnerAdapter.TopicSpinnerItem>()
                    for (category in categoriesList!!) {
                        spinnerValues.add(TopicSpinnerAdapter.TopicSpinnerItem(category.icon_url, category.name))
                    }
                    categoriesSpinner!!.adapter = TopicSpinnerAdapter(applicationContext, spinnerValues)
                }
            })
    }
}
