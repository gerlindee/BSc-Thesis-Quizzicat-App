package com.example.quizzicat.Fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import android.widget.Toast
import com.example.quizzicat.Adapters.TopicCategoriesAdapter
import com.example.quizzicat.LoginActivity
import com.example.quizzicat.Model.TopicCategory

import com.example.quizzicat.R
import com.example.quizzicat.Utils.CustomCallBack
import com.example.quizzicat.Utils.DesignUtils
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_topic_categories.*

class TopicCategoriesFragment : Fragment() {

    private var topicCategoriesGridView: GridView ? = null
    private var topicCategoriesAdapter: TopicCategoriesAdapter ? = null
    private var topicCategoriesList: ArrayList<TopicCategory> ? = null

    private var mFirestoreDatabase: FirebaseFirestore? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_topic_categories, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mFirestoreDatabase = Firebase.firestore

        topicCategoriesGridView = view.findViewById(R.id.categories_grid_view)
        setDataList(object: CustomCallBack {
            override fun onCallback(value: List<TopicCategory>) {
                topicCategoriesList = value as ArrayList<TopicCategory>
                topicCategoriesAdapter = TopicCategoriesAdapter(context!!, topicCategoriesList!!)
                topicCategoriesGridView?.adapter = topicCategoriesAdapter
            }
        })

        topicCategoriesGridView?.setOnItemClickListener { parent, view, position, id ->
            val selectedCategory = topicCategoriesList!![position]
            Toast.makeText(context, selectedCategory.name, Toast.LENGTH_LONG).show()
        }
    }

    private fun setDataList(myCallback: CustomCallBack) {
        mFirestoreDatabase!!.collection("Topic_Categories")
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
                    Log.d("TopicCategory", task.exception.toString())
                }
            }
    }

}
