package com.example.quizzicat.Utils

import com.example.quizzicat.Model.TopicsComparisonValue

interface ComparisonValuesCallBack {
    fun onCallback(value: ArrayList<TopicsComparisonValue>)
}