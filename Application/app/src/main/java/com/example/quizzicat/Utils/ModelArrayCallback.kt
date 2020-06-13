package com.example.quizzicat.Utils

import com.example.quizzicat.Model.ModelEntity

interface ModelArrayCallback {
    fun onCallback(value: List<ModelEntity>)
}