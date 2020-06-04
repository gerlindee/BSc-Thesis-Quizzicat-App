package com.example.quizzicat.Utils

import com.example.quizzicat.Model.UserReports

interface UserReportsCallBack {
    fun onCallback(value: ArrayList<UserReports>)

}