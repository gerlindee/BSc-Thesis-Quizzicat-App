package com.example.quizzicat.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.quizzicat.Facades.ImageLoadingFacade
import com.example.quizzicat.Facades.TopicDataRetrievalFacade
import com.example.quizzicat.Model.ActiveQuestion
import com.example.quizzicat.Model.PendingQuestion
import com.example.quizzicat.Model.Topic
import com.example.quizzicat.R
import com.example.quizzicat.Utils.TopicCallBack
import com.google.firebase.firestore.FirebaseFirestore

class ActiveQuestionsAdapter(
    private val mainContext: Context?,
    private val firebaseFirestore: FirebaseFirestore,
    private val list: List<ActiveQuestion>): RecyclerView.Adapter<ActiveQuestionsAdapter.ActiveQuestionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActiveQuestionViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ActiveQuestionViewHolder(inflater, parent)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ActiveQuestionViewHolder, position: Int) {
        val activeQuestion = list[position]
        holder.bind(firebaseFirestore, mainContext!!, activeQuestion)
    }

    class ActiveQuestionViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.view_question_card, parent, false)) {

        private var question_topic_icon: ImageView? = null
        private var question_text: TextView? = null
        private var question_difficulty: TextView? = null

        init {
            question_topic_icon = itemView.findViewById(R.id.view_question_topic_icon)
            question_text = itemView.findViewById(R.id.view_question_text)
            question_difficulty = itemView.findViewById(R.id.view_question_difficulty)
        }

        fun bind(firebaseFirestore: FirebaseFirestore, mainContext: Context, question: ActiveQuestion) {
            TopicDataRetrievalFacade(firebaseFirestore, question.tid).getTopicDetails(object :
                TopicCallBack {
                override fun onCallback(value: Topic) {
                    question_text!!.text = question.question_text
                    var questionDifficultyString = ""
                    when (question.difficulty) {
                        1.toLong() -> questionDifficultyString = "Easy"
                        2.toLong() -> questionDifficultyString = "Medium"
                        3.toLong() -> questionDifficultyString = "Hard"
                    }
                    question_difficulty!!.text = questionDifficultyString
                    ImageLoadingFacade(mainContext).loadImage(value.icon_url, question_topic_icon!!)
                }
            })
        }
    }
}