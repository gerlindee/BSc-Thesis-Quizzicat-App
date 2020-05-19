package com.example.quizzicat

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.quizzicat.Model.ActiveQuestion
import com.example.quizzicat.Model.ActiveQuestionAnswer
import com.example.quizzicat.Utils.AnswersCallBack
import com.example.quizzicat.Utils.DesignUtils
import com.example.quizzicat.Utils.QuestionsCallBack
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.util.concurrent.TimeUnit

class SoloQuizActivity : AppCompatActivity() {
    private var questionList = ArrayList<ActiveQuestion>()
    private var answersList = ArrayList<ActiveQuestionAnswer>()

    private var currentQuestionNr = 0
    private var correctAnswers = 0
    private var incorrectAnswers = 0

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

    private var mFirestoreDatabase: FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solo_quiz)

        mFirestoreDatabase = Firebase.firestore

        setupLayoutElements()

        getQuestionsAndAnswers()

        answerGroup!!.setOnCheckedChangeListener({ _, i ->
            nextQuestionButton!!.isEnabled = i == R.id.solo_quiz_question_answ_1 ||
                    i == R.id.solo_quiz_question_answ_2 ||
                    i == R.id.solo_quiz_question_answ_3 ||
                    i == R.id.solo_quiz_question_answ_4
            if (i == R.id.solo_quiz_question_answ_1) {
                answer1!!.background = getDrawable(R.drawable.shape_rect_light_yellow_stroke)
                answer2!!.background = getDrawable(R.drawable.shape_rect_light_yellow)
                answer3!!.background = answer2!!.background
                answer4!!.background = answer2!!.background
            }
            if (i == R.id.solo_quiz_question_answ_2) {
                answer2!!.background = getDrawable(R.drawable.shape_rect_light_yellow_stroke)
                answer1!!.background = getDrawable(R.drawable.shape_rect_light_yellow)
                answer3!!.background = answer1!!.background
                answer4!!.background = answer1!!.background
            }
            if (i == R.id.solo_quiz_question_answ_3) {
                answer3!!.background = getDrawable(R.drawable.shape_rect_light_yellow_stroke)
                answer2!!.background = getDrawable(R.drawable.shape_rect_light_yellow)
                answer1!!.background = answer2!!.background
                answer4!!.background = answer2!!.background
            }
            if (i == R.id.solo_quiz_question_answ_4) {
                answer4!!.background = getDrawable(R.drawable.shape_rect_light_yellow_stroke)
                answer2!!.background = getDrawable(R.drawable.shape_rect_light_yellow)
                answer1!!.background = answer2!!.background
                answer3!!.background = answer2!!.background
            }
        })

        questionProgress!!.max = questionList.size
        questionProgress!!.progress = 1

        nextQuestionButton!!.setOnClickListener {
            if (currentQuestionNr == (questionList.size - 1)) {
                val correctAnswer = getCorrectAnswer(currentQuestionNr)
                val selectedAnswer = findViewById<RadioButton>(answerGroup!!.checkedRadioButtonId)
                if (selectedAnswer.text == correctAnswer.Answer_Text) {
                    correctAnswers += 1
                    setAnswerHighlight(selectedAnswer, true)
                } else {
                    incorrectAnswers += 1
                    setAnswerHighlight(selectedAnswer, false)
                }
                showResult(false)
            } else {
                if (currentQuestionNr == (questionList.size - 2)) {
                    nextQuestionButton!!.text = getString(R.string.string_finish_quiz)
                }
                val correctAnswer = getCorrectAnswer(currentQuestionNr)
                currentQuestionNr += 1
                questionProgress!!.progress += 1
                val selectedAnswer = findViewById<RadioButton>(answerGroup!!.checkedRadioButtonId)
                if (selectedAnswer.text == correctAnswer.Answer_Text) {
                    correctAnswers += 1
                    setAnswerHighlight(selectedAnswer, true)
                } else {
                    incorrectAnswers += 1
                    setAnswerHighlight(selectedAnswer, false)
                }
                Handler().postDelayed({
                    setQuestionView()
                    answerGroup!!.clearCheck()
                    selectedAnswer.background = getDrawable(R.drawable.shape_rect_light_yellow)
                }, 2000)
            }
        }
    }

    private fun getQuestionsAndAnswers() {
        getQuestions(object: QuestionsCallBack {
            override fun onCallback(value: ArrayList<ActiveQuestion>) {
                questionList = value
                if (questionList.size == 0) {
                    val mainMenuIntent = Intent(applicationContext, MainMenuActivity::class.java)
                    startActivity(mainMenuIntent)
                } else {
                    randomizeQuestions()
                    val questionsQIDList = ArrayList<Long>()
                    for (question in questionList) {
                        questionsQIDList.add(question.QID)
                    }
                    getAnswers(object : AnswersCallBack {
                        override fun onCallback(value: ArrayList<ActiveQuestionAnswer>) {
                            answersList = value
                            setQuestionView()
                            setTimer()
                        }
                    }, questionsQIDList)
                }
            }
        }, intent.extras!!.getString("questionsDifficulty")!!, intent.extras!!.getString("questionsNumber")!!, intent.extras!!.getLong("questionsTopic"))
    }

    private fun randomizeQuestions() {
        val numberOfQuestions = intent.extras!!.getString("questionsNumber")!!.toInt()
        val randomQuestionPositions = ArrayList<Int>()
        var idx = 1
        while (idx <= numberOfQuestions && randomQuestionPositions.size < questionList.size) {
            var randomValue = (0 until questionList.size).random()
            while (randomValue in randomQuestionPositions) {
                randomValue = (0 until questionList.size).random()
            }
            randomQuestionPositions.add(randomValue)
            idx += 1
        }
        val randomizedQuestions = ArrayList<ActiveQuestion>()
        for (i in randomQuestionPositions) {
            randomizedQuestions.add(questionList[i])
        }
        questionList = randomizedQuestions
    }

    private fun getQuestions(callback: QuestionsCallBack, questionsDifficulty: String, questionsNumber: String, questionsTopic: Long) {
        var difficultyKey: Int? = null
        when (questionsDifficulty) {
            "Random" -> difficultyKey = 0
            "Easy" -> difficultyKey = 1
            "Medium" -> difficultyKey = 2
            "Hard" -> difficultyKey = 3
        }
        if (difficultyKey == 0) {
            mFirestoreDatabase!!.collection("Active_Questions")
                .whereEqualTo("TID", questionsTopic)
                .limit(questionsNumber.toLong())
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val quizQuestions = ArrayList<ActiveQuestion>()
                        for (document in task.result!!) {
                            val quizQuestionDifficulty = document.get("Difficulty") as Long
                            val quizQuestionQID = document.get("QID") as Long
                            val quizQuestionText = document.get("QuestionText") as String
                            val quizQuestionTID = document.get("TID") as Long
                            val quizQuestion = ActiveQuestion(quizQuestionQID, quizQuestionTID, quizQuestionText, quizQuestionDifficulty)
                            quizQuestions.add(quizQuestion)
                        }
                        callback.onCallback(quizQuestions)
                    } else {
                        Log.d("QuestionsQuery", task.exception.toString())
                    }
                }
        } else {
            mFirestoreDatabase!!.collection("Active_Questions")
                .whereEqualTo("TID", questionsTopic)
                .whereEqualTo("Difficulty", difficultyKey)
                .limit(questionsNumber.toLong())
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val quizQuestions = ArrayList<ActiveQuestion>()
                        for (document in task.result!!) {
                            val quizQuestionDifficulty = document.get("Difficulty") as Long
                            val quizQuestionQID = document.get("QID") as Long
                            val quizQuestionText = document.get("QuestionText") as String
                            val quizQuestionTID = document.get("TID") as Long
                            val quizQuestion = ActiveQuestion(quizQuestionQID, quizQuestionTID, quizQuestionText, quizQuestionDifficulty)
                            quizQuestions.add(quizQuestion)
                        }
                        callback.onCallback(quizQuestions)
                    } else {
                        Log.d("QuestionsQuery", task.exception.toString())
                    }
                }
        }
    }

    private fun getAnswers(callback: AnswersCallBack, QIDList: ArrayList<Long>) {
        mFirestoreDatabase!!.collection("Active_Question_Answers")
            .whereIn("QID", QIDList)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val quizAnswers = ArrayList<ActiveQuestionAnswer>()
                    for (document in task.result!!) {
                        val answerAID = document.get("AID") as Long
                        val answerText = document.get("Answer_Text") as String
                        val answerCorrect = document.get("Is_Correct") as Boolean
                        val answerQID = document.get("QID") as Long
                        val quizAnswer = ActiveQuestionAnswer(answerAID, answerQID, answerText, answerCorrect)
                        quizAnswers.add(quizAnswer)
                    }
                    callback.onCallback(quizAnswers)
                } else {
                    Log.d("AnswersQuery", task.exception.toString())
                }
            }

    }

    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setTitle("Quit")
            .setMessage("Are you sure you want to quit? All progress will be lost!")
            .setPositiveButton("Exit") { _, _ ->
                run {
                    val mainMenuIntent = Intent(this, MainMenuActivity::class.java)
                    startActivity(mainMenuIntent)
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    private fun setTimer() {
        val timer = object: CountDownTimer(30000, 1000) {
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
                showResult(true)
            }
        }
        timer.start()
    }

    private fun showResult(isOutOfTime: Boolean) {
        val titleAlertDialog: String = if (isOutOfTime) {
            "Oops! Seems you ran out of time :("
        } else {
            "Results"
        }
        val resultText =
            "Correctly answered questions: $correctAnswers\nIncorrectly answered questions: $incorrectAnswers"
        AlertDialog.Builder(this)
            .setTitle(titleAlertDialog)
            .setMessage(resultText)
            .setPositiveButton("Exit") { _, _ ->
                run {
                    val mainMenuIntent = Intent(this, MainMenuActivity::class.java)
                    startActivity(mainMenuIntent)
                }
            }
            .setCancelable(false)
            .create()
            .show()
    }

    private fun getCorrectAnswer(questionNumber: Int) : ActiveQuestionAnswer {
        val currentQuestion = questionList[questionNumber]
        for (answer in answersList) {
            if (answer.QID == currentQuestion.QID && answer.Is_Correct)
                return answer
        }
        return ActiveQuestionAnswer(1, 1, "a", false)
    }
    
    private fun setQuestionView() {
        val currentQuestion = questionList[currentQuestionNr]
        val number = currentQuestionNr + 1
        questionNumberText!!.text = number.toString() + "/" + questionList.size.toString()
        questionText!!.text = currentQuestion.QuestionText
        val currentAnswers = ArrayList<ActiveQuestionAnswer>()
        for (answer in answersList) {
            if (answer.QID == currentQuestion.QID)
                currentAnswers.add(answer)
        }
        answer1!!.text = currentAnswers[0].Answer_Text
        answer2!!.text = currentAnswers[1].Answer_Text
        answer3!!.text = currentAnswers[2].Answer_Text
        answer4!!.text = currentAnswers[3].Answer_Text
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

    private fun setAnswerHighlight(selectedAnswer: RadioButton, isCorrect: Boolean) {
        if (isCorrect) {
            selectedAnswer.background = getDrawable(R.drawable.shape_correct_answer)
        } else {
            selectedAnswer.background = getDrawable(R.drawable.shape_wrong_answer)
        }
    }
}
