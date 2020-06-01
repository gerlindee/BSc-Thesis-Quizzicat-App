package com.example.quizzicat.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.quizzicat.Facades.ImageLoadingFacade
import com.example.quizzicat.Facades.TopicDataRetrievalFacade
import com.example.quizzicat.Model.PendingQuestion
import com.example.quizzicat.Model.Topic
import com.example.quizzicat.R
import com.example.quizzicat.Utils.TopicCallBack
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class PendingQuestionsAdapter(
    private val source: String,
    private val mainContext: Context?,
    private val firebaseFirestore: FirebaseFirestore,
    private val list: List<PendingQuestion>): RecyclerView.Adapter<PendingQuestionsAdapter.PendingQuestionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PendingQuestionViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return PendingQuestionViewHolder(source, inflater, parent)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: PendingQuestionViewHolder, position: Int) {
        val pendingQuestion = list[position]
        holder.bind(firebaseFirestore, mainContext!!, pendingQuestion)
    }

    class PendingQuestionViewHolder(val source: String, inflater: LayoutInflater, parent: ViewGroup) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.view_pending_question, parent, false)) {

        private var question_topic_icon: ImageView? = null
        private var question_text: TextView? = null
        private var question_rating: RatingBar? = null
        private var report_question: ImageView? = null

        init {
            question_topic_icon = itemView.findViewById(R.id.pending_question_topic_icon)
            question_text = itemView.findViewById(R.id.pending_question_topic_text)
            question_rating = itemView.findViewById(R.id.pending_question_rating)
            report_question = itemView.findViewById(R.id.pending_question_report)
        }

        fun bind(firebaseFirestore: FirebaseFirestore, mainContext: Context, question: PendingQuestion) {
            TopicDataRetrievalFacade(firebaseFirestore, question.tid).getTopicDetails(object :
                TopicCallBack {
                override fun onCallback(value: Topic) {
                    question_text!!.text = question.question_text
                    question_rating!!.rating = question.avg_rating.toFloat()
                    if (source == "USER_PENDING") {
                        question_rating!!.setIsIndicator(true)
                        report_question!!.setBackgroundResource(R.drawable.delete_bin)
                    }
                    ImageLoadingFacade(mainContext).loadImage(value.icon_url, question_topic_icon!!)
                }
            })
        }

    }
}