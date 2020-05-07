package com.example.quizzicat.Facades

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.nostra13.universalimageloader.core.assist.FailReason
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener
import com.squareup.picasso.Picasso

class ImageLoadingFacade(var context: Context) {
    private val imageLoader: ImageLoader = ImageLoader.getInstance()

    fun loadImage(url: String, imageView: ImageView) {
        this.loadImageWithPicasso(url, imageView)
    }

    private fun loadImageWithPicasso(url: String, imageView: ImageView) {
        Picasso.with(context).load(url).into(imageView)
    }

    private fun loadImageWithUIL(url: String, imageView: ImageView) {
        val config = ImageLoaderConfiguration.Builder(context).build()
        imageLoader.init(config)

        imageLoader.loadImage(url, object : ImageLoadingListener {
            override fun onLoadingStarted(imageUri: String?, view: View?) {}

            override fun onLoadingFailed(imageUri: String?, view: View?, failReason: FailReason?) {}

            override fun onLoadingComplete(imageUri: String?, view: View?, loadedImage: Bitmap?) {
                imageView.setImageBitmap(loadedImage)
            }

            override fun onLoadingCancelled(imageUri: String?, view: View?) {}
        })
    }
}