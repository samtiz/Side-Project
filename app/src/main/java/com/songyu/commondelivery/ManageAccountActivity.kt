package com.songyu.commondelivery

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ManageAccountActivity : BasicActivity() {

        private lateinit var mFirebaseAuth: FirebaseAuth
        private lateinit var mDatabaseReference: DatabaseReference
        private var adapter :  ManageAccountListAdapter? = null
        private lateinit var txtName: TextView
        private lateinit var txtEmail: TextView

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_manage_account)

            mFirebaseAuth = FirebaseAuth.getInstance()
            mDatabaseReference = FirebaseDatabase.getInstance().getReference("logintest")

            val uid = mFirebaseAuth.currentUser?.uid

            txtName = findViewById(R.id.txt_manage_username)
            txtEmail = findViewById(R.id.txt_manage_email)
            mDatabaseReference.child("UserAccount").child(uid!!).get().addOnSuccessListener {
                val username = it.child("nickname").value.toString()
                val postId = it.child("postId").value.toString()
                txtName.text = username
                adapter = ManageAccountListAdapter(this@ManageAccountActivity, username, postId)

                val recyclerView: RecyclerView = findViewById(R.id.recyclerView_manage_account)
                recyclerView.layoutManager = WrapContentLinearLayoutManager(this@ManageAccountActivity)
                recyclerView.adapter = adapter
                adapter?.notifyDataSetChanged()
            }.addOnFailureListener { Toast.makeText(this, "nickname get Failed", Toast.LENGTH_SHORT).show() }
            txtEmail.text = mFirebaseAuth.currentUser?.email.toString()



            val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
            bottomNavigationView.selectedItemId = R.id.navigation_myAccount
            bottomNavigationView.setOnNavigationItemSelectedListener {item ->
                when (item.itemId) {
                    R.id.navigation_home -> {
                        val intent = Intent(applicationContext, MainActivity::class.java)
                        intent.flags = (Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                        startActivityIfNeeded(intent, 0)
                        overridePendingTransition(R.anim.none, R.anim.none)
                        true
                    }
                    R.id.navigation_chat -> {
                        mDatabaseReference.child("UserAccount").child(uid!!).child("postId").get().addOnSuccessListener {
                            val postId = it.value.toString()
                            if (postId == "null"){
                                Toast.makeText(applicationContext, "참여하신 채팅방이 없습니다.", Toast.LENGTH_SHORT).show()
                                bottomNavigationView.selectedItemId = R.id.navigation_myAccount

                            }
                            else{
                                val intent = Intent(applicationContext, MessageActivity::class.java)
                                intent.putExtra("postId", postId)
                                startActivity(intent)
                            }
                        }.addOnFailureListener {
                        }
                        true
                    }
                    R.id.navigation_add -> {
                        mDatabaseReference.child("UserAccount").child(uid).child("postId").get().addOnSuccessListener {
                            val postId = it.value.toString()
                            if(postId == "null"){
                                val intent = Intent(applicationContext, WritePostActivity::class.java)
                                intent.putExtra("uid", uid)
                                startActivity(intent)
                                overridePendingTransition(R.anim.horizon_enter, R.anim.none)
                            }
                            else{
                                Toast.makeText(applicationContext, "이미 참여한 모집방이 존재합니다.", Toast.LENGTH_SHORT).show()
                                bottomNavigationView.selectedItemId = R.id.navigation_myAccount
                            }
                        }.addOnFailureListener {
                        }
                        true
                    }
                    R.id.navigation_myAccount -> {
                        true
                    }
                    else -> false
                }
            }

        }

    override fun onResume() {
        super.onResume()
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.navigation_myAccount
        if (adapter != null){
            adapter?.notifyDataSetChanged()
        }
    }
}