package com.example.quizzicat.Adapters

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.quizzicat.Facades.ImageLoadingFacade
import com.example.quizzicat.Facades.PendingDataRetrievalFacade
import com.example.quizzicat.Facades.TopicsDataRetrievalFacade
import com.example.quizzicat.Model.PendingQuestion
import com.example.quizzicat.Model.PendingQuestionAnswer
import com.example.quizzicat.Model.Topic
import com.example.quizzicat.R
import com.example.quizzicat.Utils.PendingAnswersCallback
import com.example.quizzicat.Utils.TopicCallBack
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class PendingQuestionsAdapter(
    private val source: String,
    private val mainContext: Context?,
    private val firebaseFirestore: FirebaseFirestore,
    private val list: ArrayList<PendingQuestion>): RecyclerView.Adapter<PendingQuestionsAdapter.PendingQuestionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PendingQuestionViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return PendingQuestionViewHolder(source, inflater, parent)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private fun setAnswerData(answerText: TextView, pendingAnswer: PendingQuestionAnswer) {
        answerText.text = pendingAnswer.answer_text
        if (pendingAnswer.correct)
            answerText.setBackgroundResource(R.drawable.shape_rect_green)
    }

    override fun onBindViewHolder(holder: PendingQuestionViewHolder, position: Int) {
        val pendingQuestion = list[position]
        holder.bind(firebaseFirestore, mainContext!!, pendingQuestion)

        holder.question_topic_icon!!.setOnClickListener {
            PendingDataRetrievalFacade(firebaseFirestore, mainContext)
                .getAnswersForAQuestion(object: PendingAnswersCallback {
                    override fun onCallback(value: ArrayList<PendingQuestionAnswer>) {
                        val inflated = LayoutInflater.from(mainContext)
                        val questionAnswersView = inflated.inflate(R.layout.view_pending_question_answers, null)
                        val questionText = questionAnswersView.findViewById<TextView>(R.id.display_question_answer_text)
                        questionText!!.text = list[position].question_text
                        val firstAnswerText = questionAnswersView.findViewById<TextView>(R.id.display_first_answer_text)
                        setAnswerData(firstAnswerText, value[0])
                        val secondAnswerText = questionAnswersView.findViewById<TextView>(R.id.display_second_answer_text)
                        setAnswerData(secondAnswerText, value[1])
                        val thirdAnswerText = questionAnswersView.findViewById<TextView>(R.id.display_third_answer_text)
                        setAnswerData(thirdAnswerText, value[2])
                        val fourthAnswerText = questionAnswersView.findViewById<TextView>(R.id.display_fourth_answer_text)
                        setAnswerData(fourthAnswerText, value[3])

                        AlertDialog.Builder(mainContext)
                            .setView(questionAnswersView)
                            .setPositiveButton("Exit", null)
                            .show()
                    }
                }, pendingQuestion.pqid)
        }

        holder.report_question!!.setOnClickListener {
            if (source == "USER_PENDING") {
                AlertDialog.Builder(mainContext)
                    .setTitle("Delete Question")
                    .setMessage("Are you sure you want to delete the pending question?")
                    .setPositiveButton("Yes") { _, _ ->
                        run {
                            val pendingQuestionsCollection = firebaseFirestore.collection("Pending_Questions")
                            firebaseFirestore.collection("Pending_Questions")
                                .whereEqualTo("pqid", pendingQuestion.pqid)
                                .get()
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        for (document in task.result!!) {
                                            pendingQuestionsCollection.document(document.id).delete()
                                        }
                                        val pendingAnswersCollection = firebaseFirestore.collection("Pending_Question_Answers")
                                        firebaseFirestore.collection("Pending_Question_Answers")
                                            .whereEqualTo("pqid", pendingQuestion.pqid)
                                            .get()
                                            .addOnCompleteListener { task1 ->
                                                if (task1.isSuccessful) {
                                                    for (document in task1.result!!) {
                                                        pendingAnswersCollection.document(document.id).delete()
                                                    }
                                                    list.removeAt(position)
                                                    notifyDataSetChanged()
                                                    Toast.makeText(mainContext, "The question has been successfully deleted!", Toast.LENGTH_LONG).show()
                                                } else {
                                                    Toast.makeText(mainContext, task1.exception!!.message.toString(), Toast.LENGTH_LONG).show()
                                                }
                                            }
                                    } else {
                                        Toast.makeText(mainContext, task.exception!!.message.toString(), Toast.LENGTH_LONG).show()
                                    }
                                }
                        }
                    }
                    .setNegativeButton("No", null)
                    .create()
                    .show()
            }
        }
    }

    class PendingQuestionViewHolder(val source: String, inflater: LayoutInflater, parent: ViewGroup) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.view_pending_question, parent, false)) {

        var question_topic_icon: ImageView? = null
        private var question_text: TextView? = null
        private var question_rating: RatingBar? = null
        var report_question: ImageView? = null

        init {
            question_topic_icon = itemView.findViewById(R.id.pending_question_topic_icon)
            question_text = itemView.findViewById(R.id.pending_question_topic_text)
            question_rating = itemView.findViewById(R.id.pending_question_rating)
            report_question = itemView.findViewById(R.id.pending_question_report)
        }

        fun bind(firebaseFirestore: FirebaseFirestore, mainContext: Context, question: PendingQuestion) {
            TopicsDataRetrievalFacade(firebaseFirestore, mainContext).getTopicDetails(object :
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
            }, question.tid)
        }

    }
}