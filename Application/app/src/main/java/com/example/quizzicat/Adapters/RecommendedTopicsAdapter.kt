package com.example.quizzicat.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.quizzicat.Facades.ImageLoadingFacade
import com.example.quizzicat.Model.Topic
import com.example.quizzicat.R
import com.example.quizzicat.SoloQuizActivity
import com.google.firebase.firestore.FirebaseFirestore

class RecommendedTopicsAdapter(
    private val mainContext: Context?,
    private var list: ArrayList<Topic>
) : RecyclerView.Adapter<RecommendedTopicsAdapter.TopicViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopicViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return TopicViewHolder(inflater, parent)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: TopicViewHolder, position: Int) {
        val topic = list[position]
        holder.bind(mainContext!!, topic)

        holder.topicIcon!!.setOnClickListener {
            val inflater = LayoutInflater.from(mainContext)
            val customizingQuizView = inflater.inflate(R.layout.view_customize_solo_quiz, null)
            val selectedDifficulty : Spinner = customizingQuizView.findViewById(R.id.customize_quiz_difficulty)
            val selectedNumberOfQuestions : Spinner = customizingQuizView.findViewById(R.id.customize_quiz_number)

            AlertDialog.Builder(mainContext)
                .setView(customizingQuizView)
                .setPositiveButton("Let's play") { _, _ ->
                    val soloQuizIntent = Intent(mainContext, SoloQuizActivity::class.java)
                    soloQuizIntent.putExtra("questionsDifficulty", selectedDifficulty.selectedItem.toString())
                    soloQuizIntent.putExtra("questionsNumber", selectedNumberOfQuestions.selectedItem.toString())
                    soloQuizIntent.putExtra("questionsTopic", topic.tid)
                    soloQuizIntent.putExtra("questionsCategory", topic.cid)
                    mainContext.startActivity(soloQuizIntent)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    class TopicViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.view_category_grid_item, parent, false)) {

        var topicIcon: ImageView? = null
        private var topicName: TextView? = null

        init {
            topicIcon = itemView.findViewById(R.id.category_icon)
            topicName = itemView.findViewById(R.id.category_name)
        }

        fun bind(mainContext: Context, topic: Topic) {
            ImageLoadingFacade(mainContext).loadImage(topic.icon_url, topicIcon!!)
            topicName!!.visibility = View.GONE
        }
    }
}