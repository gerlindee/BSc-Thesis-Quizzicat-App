package com.example.quizzicat.Adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.quizzicat.Facades.ImageLoadingFacade
import com.example.quizzicat.Model.AbstractTopic
import com.example.quizzicat.Model.ModelEntity
import com.example.quizzicat.Model.TopicCategory
import com.example.quizzicat.R

class TopicCategoriesAdapter(var context: Context, var arrayList: ArrayList<ModelEntity>) : BaseAdapter() {

    override fun getItem(position: Int): Any {
        return arrayList[position]
    }

    override fun getItemId(position: Int): Long {
        val item = arrayList[position] as AbstractTopic
        return item.cid
    }

    override fun getCount(): Int {
        return arrayList.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val categoryView = View.inflate(context, R.layout.view_category_grid_item, null)
        val categoryIcon = categoryView.findViewById<ImageView>(R.id.category_icon)
        val categoryName = categoryView.findViewById<TextView>(R.id.category_name)

        val categoryItem = arrayList[position] as AbstractTopic
        ImageLoadingFacade(context).loadImage(categoryItem.icon_url, categoryIcon)
        categoryName.text = categoryItem.name

        return categoryView
    }
}