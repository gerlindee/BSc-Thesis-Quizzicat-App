package com.example.quizzicat

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.example.quizzicat.Adapters.TopicSpinnerAdapter
import com.example.quizzicat.Exceptions.CreateQuestionException
import com.example.quizzicat.Facades.TopicsDataRetrievalFacade
import com.example.quizzicat.Model.*
import com.example.quizzicat.Utils.ModelArrayCallback
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.ArrayList


class CreateQuestionActivity : AppCompatActivity() {

    private var DEFAULT_TOPIC = "https://firebasestorage.googleapis.com/v0/b/quizzicat-ca219.appspot.com/o/topic_default.png?alt=media&token=3c7894aa-681d-4c80-bbba-89aea5215ba9"

    private var mFirestoreDatabase: FirebaseFirestore? = null

    private var categoriesSpinner: Spinner? = null
    private var categoriesSpinnerValues = ArrayList<TopicSpinnerAdapter.TopicSpinnerItem>()
    private var topicsSpinner: Spinner? = null
    private var topicsSpinnerValues = ArrayList<TopicSpinnerAdapter.TopicSpinnerItem>()
    private var categoriesList = ArrayList<TopicCategory>()
    private var topicsList = ArrayList<Topic>()

    private var difficultiesSpinner: Spinner? = null
    private var questionText: EditText? = null
    private var firstAnswerCheckbox: CheckBox? = null
    private var secondAnswerCheckbox: CheckBox? = null
    private var thirdAnswerCheckbox: CheckBox? = null
    private var fourthAnswerCheckbox: CheckBox? = null
    private var firstAnswerText: EditText? = null
    private var secondAnswerText: EditText? = null
    private var thirdAnswerText: EditText? = null
    private var fourthAnswerText: EditText? = null
    private var submitQuestion: MaterialButton? = null

    private var selectedTopicTID: Long? = null

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

        topicsSpinner!!.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) {
                if (position != 0) {
                    selectedTopicTID = topicsList[position - 1].tid
                }
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {

            }
        }
        
        firstAnswerCheckbox!!.setOnCheckedChangeListener { _, isChecked ->
            Log.d("CHECKBOX", isChecked.toString())
            if (isChecked) {
                disableUnselectedCheckboxes(1)
                highlightCorrectAnswer(1)
            } else {
                enabledAllCheckboxes()
                unhighlightAnswerTexts()
            }
        }
        
        secondAnswerCheckbox!!.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                disableUnselectedCheckboxes(2)
                highlightCorrectAnswer(2)
            } else {
                enabledAllCheckboxes()
                unhighlightAnswerTexts()
            }
        }
        
        thirdAnswerCheckbox!!.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                disableUnselectedCheckboxes(3)
                highlightCorrectAnswer(3)
            } else {
                enabledAllCheckboxes()
                unhighlightAnswerTexts()
            }
        }
        
        fourthAnswerCheckbox!!.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                disableUnselectedCheckboxes(4)
                highlightCorrectAnswer(4)
            } else {
                enabledAllCheckboxes()
                unhighlightAnswerTexts()
            }
        }

        submitQuestion!!.setOnClickListener {
            try {
                checkCompleteInput()
                val pqid = UUID.randomUUID().toString()
                var difficulty = 1.toLong()
                when (difficultiesSpinner!!.selectedItem.toString()) {
                    "Easy" -> difficulty = 1.toLong()
                    "Medium" -> difficulty = 2.toLong()
                    "Hard" -> difficulty = 3.toLong()
                }
                val questionText = questionText!!.text
                val submittedBy = FirebaseAuth.getInstance().currentUser!!.uid
                val newQuestion = PendingQuestion(pqid, selectedTopicTID!!, difficulty, questionText.toString(), submittedBy, 0, 0, 0)
                mFirestoreDatabase!!.collection("Pending_Questions")
                    .document(pqid)
                    .set(newQuestion)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val answersList = ArrayList<PendingQuestionAnswer>()
                            var paid = UUID.randomUUID().toString()
                            var answer_text = firstAnswerText!!.text.toString()
                            var is_correct = firstAnswerCheckbox!!.isChecked
                            answersList.add(PendingQuestionAnswer(paid, pqid, answer_text, is_correct))

                            paid = UUID.randomUUID().toString()
                            answer_text = secondAnswerText!!.text.toString()
                            is_correct = secondAnswerCheckbox!!.isChecked
                            answersList.add(PendingQuestionAnswer(paid, pqid, answer_text, is_correct))

                            paid = UUID.randomUUID().toString()
                            answer_text = thirdAnswerText!!.text.toString()
                            is_correct = thirdAnswerCheckbox!!.isChecked
                            answersList.add(PendingQuestionAnswer(paid, pqid, answer_text, is_correct))

                            paid = UUID.randomUUID().toString()
                            answer_text = fourthAnswerText!!.text.toString()
                            is_correct = fourthAnswerCheckbox!!.isChecked
                            answersList.add(PendingQuestionAnswer(paid, pqid, answer_text, is_correct))

                            for (answer in answersList) {
                                mFirestoreDatabase!!.collection("Pending_Question_Answers")
                                    .document(answer.paid)
                                    .set(answer)
                                    .addOnCompleteListener { task1 ->
                                        if (!task1.isSuccessful) {
                                            Toast.makeText(this, task1.exception!!.message, Toast.LENGTH_LONG).show()
                                        }
                                    }
                            }

                            Toast.makeText(this, "Question successfully created!", Toast.LENGTH_LONG).show()
                            clearInputValues()
                        } else {
                            Toast.makeText(this, task.exception!!.message, Toast.LENGTH_LONG).show()
                        }
                    }
            } catch (ex: CreateQuestionException) {
                ex.displayMessageWithToast(this)
            }
        }
    }

    private fun clearInputValues() {
        difficultiesSpinner!!.setSelection(0)
        categoriesSpinner!!.setSelection(0)
        topicsSpinner!!.setSelection(0)
        topicsSpinner!!.visibility = View.GONE
        questionText!!.text.clear()
        firstAnswerText!!.text.clear()
        secondAnswerText!!.text.clear()
        thirdAnswerText!!.text.clear()
        fourthAnswerText!!.text.clear()
        firstAnswerCheckbox!!.isChecked = false
        secondAnswerCheckbox!!.isChecked = false
        thirdAnswerCheckbox!!.isChecked = false
        fourthAnswerCheckbox!!.isChecked = false
    }

    private fun setupLayoutElements() {
        difficultiesSpinner = findViewById(R.id.create_question_difficulty)
        categoriesSpinner = findViewById(R.id.create_question_category)
        topicsSpinner = findViewById(R.id.create_question_topic)
        questionText = findViewById(R.id.create_question_text)
        firstAnswerCheckbox = findViewById(R.id.first_answer_checkbox)
        secondAnswerCheckbox = findViewById(R.id.second_answer_checkbox)
        thirdAnswerCheckbox = findViewById(R.id.third_answer_checkbox)
        fourthAnswerCheckbox = findViewById(R.id.fourth_answer_checkbox)
        firstAnswerText = findViewById(R.id.first_answer_text)
        secondAnswerText = findViewById(R.id.second_answer_text)
        thirdAnswerText = findViewById(R.id.third_answer_text)
        fourthAnswerText = findViewById(R.id.fourth_answer_text)
        submitQuestion = findViewById(R.id.create_question_submit_button)
    }

    private fun setCategoriesSpinnerValues() {
        TopicsDataRetrievalFacade(mFirestoreDatabase!!, this)
            .getTopicCategories(object: ModelArrayCallback {
                override fun onCallback(value: List<ModelEntity>) {
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
            .getTopicsForACategory(object: ModelArrayCallback {
                override fun onCallback(value: List<ModelEntity>) {
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

    private fun enabledAllCheckboxes() {
        firstAnswerCheckbox!!.isEnabled = true
        secondAnswerCheckbox!!.isEnabled = true
        thirdAnswerCheckbox!!.isEnabled = true
        fourthAnswerCheckbox!!.isEnabled = true
    }

    private fun unhighlightAnswerTexts() {
        firstAnswerText!!.background = ResourcesCompat.getDrawable(resources, R.drawable.shape_rect_light_yellow, null)
        secondAnswerText!!.background = ResourcesCompat.getDrawable(resources, R.drawable.shape_rect_light_yellow, null)
        thirdAnswerText!!.background = ResourcesCompat.getDrawable(resources, R.drawable.shape_rect_light_yellow, null)
        fourthAnswerText!!.background = ResourcesCompat.getDrawable(resources, R.drawable.shape_rect_light_yellow, null)
    }

    private fun disableUnselectedCheckboxes(checkboxNumber: Int) {
        when (checkboxNumber) {
            1 -> {
                secondAnswerCheckbox!!.isEnabled = false
                thirdAnswerCheckbox!!.isEnabled = false
                fourthAnswerCheckbox!!.isEnabled = false
            }
            2 -> {
                firstAnswerCheckbox!!.isEnabled = false
                thirdAnswerCheckbox!!.isEnabled = false
                fourthAnswerCheckbox!!.isEnabled = false
            }
            3 -> {
                firstAnswerCheckbox!!.isEnabled = false
                secondAnswerCheckbox!!.isEnabled = false
                fourthAnswerCheckbox!!.isEnabled = false
            }
            4 -> {
                firstAnswerCheckbox!!.isEnabled = false
                secondAnswerCheckbox!!.isEnabled = false
                thirdAnswerCheckbox!!.isEnabled = false
            }
        }
    }

    private fun highlightCorrectAnswer(checkboxNumber: Int) {
        when (checkboxNumber) {
            1 -> {
                firstAnswerText!!.background = ResourcesCompat.getDrawable(resources, R.drawable.shape_rect_green, null)
            }
            2 -> {
                secondAnswerText!!.background = ResourcesCompat.getDrawable(resources, R.drawable.shape_rect_green, null)
            }
            3 -> {
                thirdAnswerText!!.background = ResourcesCompat.getDrawable(resources, R.drawable.shape_rect_green, null)
            }
            4 -> {
                fourthAnswerText!!.background = ResourcesCompat.getDrawable(resources, R.drawable.shape_rect_green, null)
            }
        }
    }

    private fun checkCompleteInput() {
        if (difficultiesSpinner!!.selectedItem.toString() == "Difficulty")
            throw CreateQuestionException("Please select a difficulty for the question!")
        if (topicsSpinner!!.visibility == View.GONE)
            throw CreateQuestionException("Please select a topic for the question!")
        if (questionText!!.text.isEmpty())
            throw CreateQuestionException("Question text cannot be empty!")
        if (firstAnswerText!!.text.isEmpty() || secondAnswerText!!.text.isEmpty() || thirdAnswerText!!.text.isEmpty() || fourthAnswerText!!.text.isEmpty())
            throw CreateQuestionException("Answer text cannot be empty!")
        if (!firstAnswerCheckbox!!.isChecked && !secondAnswerCheckbox!!.isChecked && !thirdAnswerCheckbox!!.isChecked && !fourthAnswerCheckbox!!.isChecked)
            throw CreateQuestionException("A correct answer needs to be selected!")
    }
}
