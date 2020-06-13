package com.example.quizzicat.Utils

import com.example.quizzicat.Model.ModelEntity

interface ModelCallback {
    fun onCallback(value: ModelEntity)
}