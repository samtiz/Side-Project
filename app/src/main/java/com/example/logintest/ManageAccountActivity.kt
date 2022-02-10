package com.example.logintest

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
        private lateinit var adapter: ManageAccountListAdapter
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
            mDatabaseReference.child("UserAccount").child(uid!!).child("nickname").get().addOnSuccessListener {
                val username = it.value as String
                txtName.text = username
            }.addOnFailureListener { Toast.makeText(this, "nickname get Failed", Toast.LENGTH_SHORT).show() }
            txtEmail.text = mFirebaseAuth.currentUser?.email.toString()

            adapter = ManageAccountListAdapter(this@ManageAccountActivity)

            val recyclerView: RecyclerView = findViewById(R.id.recyclerView_manage_account)
            recyclerView.layoutManager = WrapContentLinearLayoutManager(this@ManageAccountActivity)
            recyclerView.adapter = adapter
            adapter.notifyDataSetChanged()

//            val btnLogout: Button = findViewById(R.id.btn_logout)
//            btnLogout.setOnClickListener{
//                mFirebaseAuth.signOut()
//                Toast.makeText(this@ManageAccountActivity, "로그아웃 되었습니다", Toast.LENGTH_SHORT).show()
//                MySharedPreferences.clearUser(this)
//                val intent = Intent(this@ManageAccountActivity, LoginActivity::class.java)
//                startActivity(intent)
//                finish()
//            }
//
//            val btnSecession: Button = findViewById(R.id.btn_secession)
//            btnSecession.setOnClickListener {
//                val mFirebaseUser: FirebaseUser? = mFirebaseAuth.currentUser
//                lateinit var  strPwd: String
//                mFirebaseUser?.uid.let { it1 ->
//                    if (it1 != null) {
//                        strPwd = mDatabaseReference.child("UserAccount").child(it1).child("password").toString()
//                        mDatabaseReference.child("UserAccount").child(it1).removeValue()
//
//
//                        mFirebaseUser?.delete()?.addOnCompleteListener { task ->
//                            if (task.isSuccessful) {
//                                Toast.makeText(this@ManageAccountActivity, "성공적으로 계정을 탈퇴하였습니다.", Toast.LENGTH_SHORT).show()
//                                val intent = Intent(this@ManageAccountActivity, LoginActivity::class.java)
//                                startActivity(intent)
//                                finish()
//                            }
//                            else {
//                                Toast.makeText(this@ManageAccountActivity, "계정 삭제에 실패하였습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
//                                val account = UserAccount()
//                                account.emailId = mFirebaseUser?.email
//                                account.idToken = mFirebaseUser?.uid
//                                account.password = strPwd
//
//                                mFirebaseUser?.uid?.let { it2 -> mDatabaseReference.child("UserAccount").child(it2).setValue(account) }
//                            }
//                        }
//
//                    }
//                    else{
//                        Toast.makeText(this@ManageAccountActivity, "사용자 데이터를 지우는데 실패하셨습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            }

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
                        true
                    }
                    R.id.navigation_add -> {
//                        val i = getPostNum()
//                        if (i > 0) {
//                            MaterialAlertDialogBuilder(applicationContext).setMessage("이미 작성한 게시물이 ${i}개 있습니다. 또 작성하시겠습니까?")
//                                .setPositiveButton("확인", DialogInterface.OnClickListener { _, _ ->
//                                    val intent2 = Intent(applicationContext, WritePostActivity::class.java)
//                                    intent2.putExtra("uid", mFirebaseAuth.currentUser?.uid)
//                                    startActivity(intent2)
//                                    overridePendingTransition(R.anim.horizon_enter, R.anim.none)
//                                })
//                                .setNegativeButton("취소") { _, _ -> }.show()
//                        }
//                        else {
//                            val intent2 = Intent(applicationContext, WritePostActivity::class.java)
//                            intent2.putExtra("uid", mFirebaseAuth.currentUser?.uid)
//                            startActivity(intent2)
//                            overridePendingTransition(R.anim.horizon_enter, R.anim.none)
//                        }
//                        val sameUserPosts = ArrayList<Post>()
//                        mDatabaseReference.child("Post").orderByChild("uid").equalTo(mFirebaseAuth.currentUser?.uid).addListenerForSingleValueEvent(object :
//                            ValueEventListener {
//                            override fun onCancelled(error: DatabaseError) {
//                            }
//
//                            override fun onDataChange(snapshot: DataSnapshot) {
//                                sameUserPosts.clear()
//                                for (data in snapshot.children) {
//                                    sameUserPosts.add(data.getValue<Post>()!!)
//                                }
//                                if (sameUserPosts.isEmpty()) {
//                                    println(sameUserPosts.size)
//                                    val intent2 = Intent(applicationContext, WritePostActivity::class.java)
//                                    intent2.putExtra("uid", mFirebaseAuth.currentUser?.uid)
//                                    startActivity(intent2)
//                                } else {
//                                    MaterialAlertDialogBuilder(this@ManageAccountActivity).setMessage("이미 작성한 게시물이 ${sameUserPosts.size}개 있습니다. 또 작성하시겠습니까?")
//                                        .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
//                                            val intent2 = Intent(applicationContext, WritePostActivity::class.java)
//                                            intent2.putExtra("uid", mFirebaseAuth.currentUser?.uid)
//                                            startActivity(intent2)
//                                        })
//                                        .setNegativeButton("취소") { _, _ -> }.show()
//                                }
//                            }
//                        })

                        val intent = Intent(applicationContext, WritePostActivity::class.java)
                        intent.putExtra("uid", mFirebaseAuth.currentUser?.uid)
                        startActivity(intent)
                        overridePendingTransition(R.anim.horizon_enter, R.anim.none)
                        true
                    }
//                    R.id.navigation_myPost -> {
//                        val intent = Intent(applicationContext, ManagePostActivity::class.java)
//                        intent.flags = (Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
//                        startActivityIfNeeded(intent, 0)
//                        overridePendingTransition(R.anim.none, R.anim.none)
//                        true
//                    }
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
        adapter.notifyDataSetChanged()
    }


}