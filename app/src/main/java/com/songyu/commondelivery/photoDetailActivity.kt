package com.songyu.commondelivery

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.bumptech.glide.Glide

class photoDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_detail)

        val photo = intent.getStringExtra("imageUrl")
        val imageUrl = Uri.parse(photo)
        var photoView : ImageView? = null


        photoView = findViewById(R.id.photo_detail)
        Glide.with(this).load(imageUrl).into(photoView)
    }
}