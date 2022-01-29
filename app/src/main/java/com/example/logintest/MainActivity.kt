package com.example.logintest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MainActivity : BasicActivity() {

    private lateinit var mFirebaseAuth: FirebaseAuth
    private lateinit var mDatabaseReference: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mFirebaseAuth = FirebaseAuth.getInstance()
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("logintest")

        val btnManageAccount: Button = findViewById(R.id.btn_manageAccount)
        btnManageAccount.setOnClickListener {
            val intent = Intent(this@MainActivity, ManageAccountActivity::class.java)
            startActivity(intent)
        }

        val btnAdd: FloatingActionButton = findViewById(R.id.btn_add)
        btnAdd.setOnClickListener {
            // 자신이 쓴 게시글 있는지 확인하는 코드 추가해야함
            //mDatabaseReference.child("Post")

            val intent2 = Intent(this@MainActivity, WritePostActivity::class.java)
            intent2.putExtra("uid", mFirebaseAuth.currentUser?.uid)
            startActivity(intent2)
        }
    }


}