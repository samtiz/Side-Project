package com.songyu.commondelivery

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_image_picker.*

class imagePickerActivity : AppCompatActivity() {
    var list = ArrayList<String>()
//    var uriList = ArrayList<String>()
    lateinit var adapter : ImagePickerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_picker)

        setSupportActionBar(toolbar_image_picker)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        getGalleryPhotos(applicationContext)

        adapter = ImagePickerAdapter(applicationContext,list)
        var rcv = findViewById<RecyclerView>(R.id.rcv)
        rcv.layoutManager = GridLayoutManager(applicationContext,3)
        rcv.adapter = adapter

//        var button = findViewById<Button>(R.id.submit)
//        button.setOnClickListener {
//            var intent = Intent(this,MessageActivity::class.java)
////            bundle.putString("image",adapter.list.get(adapter.checkBox))
////            startActivity(intent,bundle)
//            intent.putExtra("image", adapter.list.get(adapter.checkBox))
//            //intent.putExtra("image", uriList.get(adapter.checkBox))
//            setResult(Activity.RESULT_OK, intent);
//            finish()
//        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.clear()
        menuInflater.inflate(R.menu.menu_image_picker, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.action_send -> {
                if (adapter.checkBox != -1){
                    var intent = Intent(this,MessageActivity::class.java)
                    intent.putExtra("image", adapter.list.get(adapter.checkBox))
                    setResult(Activity.RESULT_OK, intent);
                    finish()
                }

            }
        }
        return super.onOptionsItemSelected(item)
    }


    fun getGalleryPhotos( context : Context){

        var uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        var columns =
            arrayOf(MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID)
        val orderBy = MediaStore.Images.Media._ID

        var cursor = context.contentResolver.query(uri,columns,null,null,orderBy)


        if(cursor !=null && cursor.count>0){
            while(cursor.moveToNext()){

                var indexPath = cursor.getColumnIndex(MediaStore.MediaColumns.DATA)
                list.add(cursor.getString(indexPath))
//                uriList.add(uri.toString())

            }
        }else{
            Log.e("getGalleryPhotos","error")

        }
        list.reverse()
    }
}