package com.example.quizzicat

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.example.quizzicat.Facades.MultiPlayerDataRetrievalFacade
import com.example.quizzicat.Facades.QuestionsDataRetrievalFacade
import com.example.quizzicat.Model.ActiveQuestion
import com.example.quizzicat.Model.ActiveQuestionAnswer
import com.example.quizzicat.Model.ModelEntity
import com.example.quizzicat.Model.MultiPlayerGameQuestion
import com.example.quizzicat.Utils.ModelArrayCallback
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit

class MultiPlayerQuizActivity : AppCompatActivity() {

    private var mFirebaseFirestore: FirebaseFirestore? = null
    private var gid: String? = ""
    private var gamePIN: String? = ""
    private var questionList = ArrayList<ActiveQuestion>()
    private var answersList = ArrayList<ActiveQuestionAnswer>()

    private var currentQuestionNr = 0
    private var userScore = 0

    // layout elements
    private var answer1: RadioButton? = null
    private var answer2: RadioButton? = null
    private var answer3: RadioButton? = null
    private var answer4: RadioButton? = null
    private var answerGroup: RadioGroup? = null
    private var questionNumberText: TextView? = null
    private var questionTimeText: TextView? = null
    private var questionProgress: ProgressBar? = null
    private var questionText: TextView? = null
    private var nextQuestionButton: Button? = null
    private var timer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solo_quiz)

        mFirebaseFirestore = Firebase.firestore
        gid = intent.extras!!.getString("gid")
        gamePIN = intent.extras!!.getString("gamePIN")

        setupLayoutElements()
        getQuestionsAndAnswers()

        answerGroup!!.setOnCheckedChangeListener { _, index ->
            highlightSelectedAnswer(index)
        }

        questionProgress!!.max = questionList.size
        questionProgress!!.progress = 1

        nextQuestionButton!!.setOnClickListener {
            userAnswersQuestion()
        }
    }

    private fun userAnswersQuestion() {
        if (currentQuestionNr == (questionList.size - 1)) {
            val correctAnswer = getCorrectAnswer(currentQuestionNr)
            val selectedAnswer = findViewById<RadioButton>(answerGroup!!.checkedRadioButtonId)
            if (selectedAnswer.text == correctAnswer.answer_text) {
                userScore += computeScorePerAnswer()
                setAnswerHighlight(selectedAnswer, true)
            } else {
                setAnswerHighlight(selectedAnswer, false)
            }
            timer!!.cancel()
            timer!!.onFinish()
        } else {
            timer!!.cancel()
            if (currentQuestionNr == (questionList.size - 2)) {
                nextQuestionButton!!.text = getString(R.string.string_finish_quiz)
            }
            val correctAnswer = getCorrectAnswer(currentQuestionNr)
            currentQuestionNr += 1
            questionProgress!!.progress += 1
            val selectedAnswer = findViewById<RadioButton>(answerGroup!!.checkedRadioButtonId)
            if (selectedAnswer == null) { // time expires but user has not selected anything
                setAnswerHighlight(answer1!!, false)
                setAnswerHighlight(answer2!!, false)
                setAnswerHighlight(answer3!!, false)
                setAnswerHighlight(answer4!!, false)
                Handler().postDelayed({
                    setQuestionView()
                    answerGroup!!.clearCheck()
                    answer1!!.background = getDrawable(R.drawable.shape_rect_light_yellow)
                    answer2!!.background = getDrawable(R.drawable.shape_rect_light_yellow)
                    answer3!!.background = getDrawable(R.drawable.shape_rect_light_yellow)
                    answer4!!.background = getDrawable(R.drawable.shape_rect_light_yellow)
                    timer!!.start()
                }, 2000)
            } else {
                if (selectedAnswer.text == correctAnswer.answer_text) {
                    userScore += computeScorePerAnswer()
                    setAnswerHighlight(selectedAnswer, true)
                } else {
                    setAnswerHighlight(selectedAnswer, false)
                }
                Handler().postDelayed({
                    setQuestionView()
                    answerGroup!!.clearCheck()
                    selectedAnswer.background = getDrawable(R.drawable.shape_rect_light_yellow)
                    timer!!.start()
                }, 2000)
            }
        }
    }

    private fun getCorrectAnswer(questionNumber: Int) : ActiveQuestionAnswer {
        val currentQuestion = questionList[questionNumber]
        for (answer in answersList) {
            if (answer.qid == currentQuestion.qid && answer.correct)
                return answer
        }
        return ActiveQuestionAnswer("", "", "a", false)
    }

    private fun setupLayoutElements() {
        questionNumberText = findViewById(R.id.solo_quiz_question_nr_text)
        questionTimeText = findViewById(R.id.solo_quiz_question_time_text)
        questionProgress = findViewById(R.id.solo_quiz_question_progress)
        questionText = findViewById(R.id.solo_quiz_question_text)
        answerGroup = findViewById(R.id.solo_quiz_question_answ_group)
        answer1 = findViewById(R.id.solo_quiz_question_answ_1)
        answer2 = findViewById(R.id.solo_quiz_question_answ_2)
        answer3 = findViewById(R.id.solo_quiz_question_answ_3)
        answer4 = findViewById(R.id.solo_quiz_question_answ_4)
        nextQuestionButton = findViewById(R.id.solo_quiz_next_button)
    }

    private fun getQuestionsAndAnswers() {
        MultiPlayerDataRetrievalFacade(mFirebaseFirestore!!, this)
            .getQuestionsForQuiz(gid!!, object: ModelArrayCallback {
                override fun onCallback(value: List<ModelEntity>) {
                    val questionIDs = ArrayList<String>()
                    for (question in value as ArrayList<MultiPlayerGameQuestion>) {
                        questionIDs.add(question.qid)
                    }
                    QuestionsDataRetrievalFacade(mFirebaseFirestore!!, this@MultiPlayerQuizActivity)
                        .getQuestionsData(questionIDs, object: ModelArrayCallback {
                            override fun onCallback(value: List<ModelEntity>) {
                                questionList = value as ArrayList<ActiveQuestion>
                                val questionsQIDList = ArrayList<String>()
                                for (question in questionList) {
                                    questionsQIDList.add(question.qid)
                                }
                                QuestionsDataRetrievalFacade(mFirebaseFirestore!!, applicationContext)
                                    .getAnswers(object : ModelArrayCallback {
                                        override fun onCallback(value: List<ModelEntity>) {
                                            answersList = value as ArrayList<ActiveQuestionAnswer>
                                            setQuestionView()
                                            setTimer()
                                        }
                                    }, questionsQIDList)
                            }
                        })
                }
            })
    }

    private fun highlightSelectedAnswer(position: Int) {
        nextQuestionButton!!.isEnabled = position == R.id.solo_quiz_question_answ_1 ||
                position == R.id.solo_quiz_question_answ_2 ||
                position == R.id.solo_quiz_question_answ_3 ||
                position == R.id.solo_quiz_question_answ_4
        if (position == R.id.solo_quiz_question_answ_1) {
            answer1!!.background = getDrawable(R.drawable.shape_rect_light_yellow_stroke)
            answer2!!.background = getDrawable(R.drawable.shape_rect_light_yellow)
            answer3!!.background = answer2!!.background
            answer4!!.background = answer2!!.background
        }
        if (position == R.id.solo_quiz_question_answ_2) {
            answer2!!.background = getDrawable(R.drawable.shape_rect_light_yellow_stroke)
            answer1!!.background = getDrawable(R.drawable.shape_rect_light_yellow)
            answer3!!.background = answer1!!.background
            answer4!!.background = answer1!!.background
        }
        if (position == R.id.solo_quiz_question_answ_3) {
            answer3!!.background = getDrawable(R.drawable.shape_rect_light_yellow_stroke)
            answer2!!.background = getDrawable(R.drawable.shape_rect_light_yellow)
            answer1!!.background = answer2!!.background
            answer4!!.background = answer2!!.background
        }
        if (position == R.id.solo_quiz_question_answ_4) {
            answer4!!.background = getDrawable(R.drawable.shape_rect_light_yellow_stroke)
            answer2!!.background = getDrawable(R.drawable.shape_rect_light_yellow)
            answer1!!.background = answer2!!.background
            answer3!!.background = answer2!!.background
        }
    }

    private fun setQuestionView() {
        val currentQuestion = questionList[currentQuestionNr]
        val number = currentQuestionNr + 1
        questionNumberText!!.text = number.toString() + "/" + questionList.size.toString()
        questionText!!.text = currentQuestion.question_text
        val currentAnswers = ArrayList<ActiveQuestionAnswer>()
        for (answer in answersList) {
            if (answer.qid == currentQuestion.qid)
                currentAnswers.add(answer)
        }
        answer1!!.text = currentAnswers[0].answer_text
        answer2!!.text = currentAnswers[1].answer_text
        answer3!!.text = currentAnswers[2].answer_text
        answer4!!.text = currentAnswers[3].answer_text
    }

    private fun setTimer() {
        timer = object: CountDownTimer(11000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val formattedSecondsLeft = String.format(
                    "%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(
                        TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                    ),
                    TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                    )
                )
                questionTimeText!!.text = formattedSecondsLeft
            }

            override fun onFinish() {
                if (currentQuestionNr == (questionList.size - 1)) {
                    MultiPlayerDataRetrievalFacade(mFirebaseFirestore!!, this@MultiPlayerQuizActivity)
                        .setUserScore(gid!!, userScore.toLong())
                    val scoreboardIntent = Intent(this@MultiPlayerQuizActivity, MultiPlayerScoreboardActivity::class.java)
                    scoreboardIntent.putExtra("gid", gid)
                    scoreboardIntent.putExtra("gamePIN", gamePIN)
                    startActivity(scoreboardIntent)
                } else {
                    userAnswersQuestion()
                }
            }
        }
        timer!!.start()
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setTitle("Quit")
            .setMessage("Are you sure you want to leave the game? All progress will be lost!")
            .setPositiveButton("Exit") { _, _ ->
                run {
                    MultiPlayerDataRetrievalFacade(mFirebaseFirestore!!, this)
                        .userLeavesGame(gid!!)
                    val mainMenuIntent = Intent(this, MainMenuActivity::class.java)
                    startActivity(mainMenuIntent)
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    private fun setAnswerHighlight(selectedAnswer: RadioButton, isCorrect: Boolean) {
        if (isCorrect) {
            selectedAnswer.background = getDrawable(R.drawable.shape_correct_answer)
        } else {
            selectedAnswer.background = getDrawable(R.drawable.shape_wrong_answer)
        }
    }

    private fun computeScorePerAnswer(): Int {
        return questionTimeText!!.text.split(":")[1].toInt()
    }
}
