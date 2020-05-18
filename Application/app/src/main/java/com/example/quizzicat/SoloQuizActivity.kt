package com.example.quizzicat

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.quizzicat.Model.ActiveQuestion
import com.example.quizzicat.Model.ActiveQuestionAnswer
import com.example.quizzicat.Utils.DesignUtils
import java.util.concurrent.TimeUnit

class SoloQuizActivity : AppCompatActivity() {
    private var questionList = ArrayList<ActiveQuestion>()
    private var answersList = ArrayList<ActiveQuestionAnswer>()

    private var currentQuestionNr = 0;

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

    private var correctAnswers = 0
    private var incorrectAnswers = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solo_quiz)

        setupHardcodedQuestions()
        setupLayoutElements()

        setQuestionView() // load the first question

        setTimer()

        answerGroup!!.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, i ->
            if (i == R.id.solo_quiz_question_answ_1 ||
                i == R.id.solo_quiz_question_answ_2 ||
                i == R.id.solo_quiz_question_answ_3 ||
                i == R.id.solo_quiz_question_answ_4) {
                    nextQuestionButton!!.isEnabled = true
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
                    DesignUtils.showSnackbar(window.decorView.rootView, "Good job!", this)
                } else {
                    incorrectAnswers += 1
                    DesignUtils.showSnackbar(window.decorView.rootView, "Oh-oh, that doesn't seem right", this)
                }
                showResult(false)
            } else {
                if (currentQuestionNr == (questionList.size - 2)) {
                    nextQuestionButton!!.text = "Finish"
                }
                val correctAnswer = getCorrectAnswer(currentQuestionNr)
                currentQuestionNr += 1
                questionProgress!!.progress += 1
                val selectedAnswer = findViewById<RadioButton>(answerGroup!!.checkedRadioButtonId)
                if (selectedAnswer.text == correctAnswer.Answer_Text) {
                    correctAnswers += 1
                    DesignUtils.showSnackbar(window.decorView.rootView, "Good job!", this)
                } else {
                    incorrectAnswers += 1
                    DesignUtils.showSnackbar(window.decorView.rootView, "Oh-oh, that doesn't seem right", this)
                }
                setQuestionView()
                answerGroup!!.clearCheck()
            }
        }
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setTitle("Quit")
            .setMessage("Are you sure you want to quit? All progress will be lost!")
            .setPositiveButton("Exit", DialogInterface.OnClickListener{
                    _, _ ->
                run {
                    val mainMenuIntent = Intent(this, MainMenuActivity::class.java)
                    startActivity(mainMenuIntent)
                }
            })
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
        var titleAlertDialog = ""
        if (isOutOfTime) {
            titleAlertDialog = "Oops! Seems you ran out of time :("
        } else {
            titleAlertDialog = "Results"
        }
        val resultText =
            "Correctly answered questions: $correctAnswers\nIncorrectly answered questions: $incorrectAnswers"
        AlertDialog.Builder(this)
            .setTitle(titleAlertDialog)
            .setMessage(resultText)
            .setPositiveButton("Exit", DialogInterface.OnClickListener{
                    _, _ ->
                run {
                    val mainMenuIntent = Intent(this, MainMenuActivity::class.java)
                    startActivity(mainMenuIntent)
                }
            })
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

    private fun setupHardcodedQuestions() {
        var question = ActiveQuestion(1, 3, "What is Hermione Granger's real name?", 1)
        questionList.add(question)
        question = ActiveQuestion(2, 3, "Harry, Ron, and Hermione help save the Sorcerer's Stone from being stolen. How old was its co-creator, Nicholas Flamel, when he decided to destroy it?", 3)
        questionList.add(question)
        question = ActiveQuestion(3, 3, "Monkshood and wolfsbane are the same plant, also known as what?", 3)
        questionList.add(question)

        var answer = ActiveQuestionAnswer(1, 1, "Jean", true)
        answersList.add(answer)
        answer = ActiveQuestionAnswer(2, 1, "Marlene", false)
        answersList.add(answer)
        answer = ActiveQuestionAnswer(3, 1, "Joanne", false)
        answersList.add(answer)
        answer = ActiveQuestionAnswer(4, 1, "Kaitlin", false)
        answersList.add(answer)

        answer = ActiveQuestionAnswer(5, 2, "854", false)
        answersList.add(answer)
        answer = ActiveQuestionAnswer(6, 2, "763", false)
        answersList.add(answer)
        answer = ActiveQuestionAnswer(7, 2, "665", true)
        answersList.add(answer)
        answer = ActiveQuestionAnswer(8, 2, "276", false)
        answersList.add(answer)

        answer = ActiveQuestionAnswer(9, 3, "Bezoar", false)
        answersList.add(answer)
        answer = ActiveQuestionAnswer(10, 3, "Aconite", true)
        answersList.add(answer)
        answer = ActiveQuestionAnswer(11, 3, "Gillyflower", false)
        answersList.add(answer)
        answer = ActiveQuestionAnswer(12, 3, "Lion's Heart", false)
        answersList.add(answer)
    }
}
