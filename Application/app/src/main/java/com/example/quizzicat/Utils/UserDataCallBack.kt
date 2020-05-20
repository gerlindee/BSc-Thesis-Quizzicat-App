package com.example.quizzicat.Utils

import com.example.quizzicat.Model.User

interface UserDataCallBack {
    fun onCallback(value: User)
}