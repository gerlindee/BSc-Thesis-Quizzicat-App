package com.example.quizzicat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.cardview.widget.CardView

class QuestionsFactoryActivity : AppCompatActivity() {

    private var create_question_card: CardView? = null
    private var approved_questions_card: CardView? = null
    private var pending_questions_card: CardView? = null
    private var rejected_questions_card: CardView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_questions_factory)

        setupLayoutElements()

        create_question_card!!.setOnClickListener {
            val createQuestionIntent = Intent(this, CreateQuestionActivity::class.java)
            startActivity(createQuestionIntent)
        }

        pending_questions_card!!.setOnClickListener {
            val pendingQuestionsIntent = Intent(this, UserQuestionsPendingActivity::class.java)
            startActivity(pendingQuestionsIntent)
        }

        approved_questions_card!!.setOnClickListener {
            val activeQuestionsIntent = Intent(this, UserQuestionsAcceptedActivity::class.java)
            startActivity(activeQuestionsIntent)
        }
    }

    private fun setupLayoutElements() {
        create_question_card    = findViewById(R.id.factory_add_question_card)
        approved_questions_card = findViewById(R.id.factory_view_approved)
        pending_questions_card  = findViewById(R.id.factory_view_pending)
        rejected_questions_card = findViewById(R.id.factory_view_rejected)
    }
}
