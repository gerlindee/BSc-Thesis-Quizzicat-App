package com.example.quizzicat.Adapters

import android.R
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.quizzicat.Facades.ImageLoadingFacade


class TopicSpinnerAdapter(
    private var mainContext: Context,
    private var arrayList: ArrayList<TopicSpinnerItem>
) : ArrayAdapter<TopicSpinnerAdapter.TopicSpinnerItem>(mainContext, 0, arrayList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return initializeView(position, convertView, parent)
    }

    override fun isEnabled(position: Int): Boolean {
        if (position == 0) {
            return false
        }
        return true
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return initializeView(position, convertView, parent)
    }

    private fun initializeView(position: Int, convertView: View?, parent: ViewGroup): View {
        val categoryView = View.inflate(mainContext, com.example.quizzicat.R.layout.view_category_spinner_item, null)
        val categoryIcon = categoryView.findViewById<ImageView>(com.example.quizzicat.R.id.category_spinner_icon)
        val categoryName = categoryView.findViewById<TextView>(com.example.quizzicat.R.id.category_spinner_name)
        val currentItem = getItem(position)
        ImageLoadingFacade(mainContext).loadImage(currentItem!!.category_icon, categoryIcon)
        categoryName.text = currentItem.category_name
        if (position == 0) {
            categoryName.setTextColor(Color.GRAY)
        }
        return categoryView
    }

    class TopicSpinnerItem(
        val category_icon: String,
        val category_name: String
    )
}