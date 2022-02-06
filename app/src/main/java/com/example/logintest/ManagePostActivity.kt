package com.example.logintest

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import kotlinx.android.synthetic.main.activity_manage_post.*

class ManagePostActivity: BasicActivity() {
    private lateinit var adapter:ListAdapter
    private lateinit var mFirebaseAuth: FirebaseAuth
    private lateinit var mDatabaseReference: DatabaseReference
    private val viewModel by lazy { ViewModelProvider(this).get(ListViewModel::class.java) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_post)

        mFirebaseAuth = FirebaseAuth.getInstance()
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("logintest")

        adapter = ListAdapter(this@ManagePostActivity)

        val recyclerView : RecyclerView = findViewById(R.id.recyclerView_manage_post)
        recyclerView.layoutManager = WrapContentLinearLayoutManager(this@ManagePostActivity) // recyclerView의 안정성을 위함
//        recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
        recyclerView.adapter = adapter
        observerData_myPost() // 초기 데이터 가져오기

        setSupportActionBar(toolbar_manage_post)
        supportActionBar?.setDisplayShowTitleEnabled(false)



        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.navigation_myPost
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
//                    val i = getPostNum()
//                    if (i > 0) {
//                        MaterialAlertDialogBuilder(applicationContext).setMessage("이미 작성한 게시물이 ${i}개 있습니다. 또 작성하시겠습니까?")
//                            .setPositiveButton("확인", DialogInterface.OnClickListener { _, _ ->
//                                val intent2 = Intent(applicationContext, WritePostActivity::class.java)
//                                intent2.putExtra("uid", mFirebaseAuth.currentUser?.uid)
//                                startActivity(intent2)
//                                overridePendingTransition(R.anim.horizon_enter, R.anim.none)
//                            })
//                            .setNegativeButton("취소") { _, _ -> }.show()
//                    }
//                    else {
//                        val intent2 = Intent(applicationContext, WritePostActivity::class.java)
//                        intent2.putExtra("uid", mFirebaseAuth.currentUser?.uid)
//                        startActivity(intent2)
//                        overridePendingTransition(R.anim.horizon_enter, R.anim.none)
//                    }
//                    val sameUserPosts = ArrayList<Post>()
//                    mDatabaseReference.child("Post").orderByChild("uid").equalTo(mFirebaseAuth.currentUser?.uid).addListenerForSingleValueEvent(object :
//                        ValueEventListener {
//                        override fun onCancelled(error: DatabaseError) {
//                        }
//
//                        override fun onDataChange(snapshot: DataSnapshot) {
//                            sameUserPosts.clear()
//                            for (data in snapshot.children) {
//                                sameUserPosts.add(data.getValue<Post>()!!)
//                            }
//                            if (sameUserPosts.isEmpty()) {
//                                println(sameUserPosts.size)
//                                val intent2 = Intent(applicationContext, WritePostActivity::class.java)
//                                intent2.putExtra("uid", mFirebaseAuth.currentUser?.uid)
//                                startActivity(intent2)
//                            } else {
//                                MaterialAlertDialogBuilder(this@ManagePostActivity).setMessage("이미 작성한 게시물이 ${sameUserPosts.size}개 있습니다. 또 작성하시겠습니까?")
//                                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
//                                        val intent2 = Intent(applicationContext, WritePostActivity::class.java)
//                                        intent2.putExtra("uid", mFirebaseAuth.currentUser?.uid)
//                                        startActivity(intent2)
//                                    })
//                                    .setNegativeButton("취소") { _, _ -> }.show()
//                            }
//                        }
//                    })


                    val intent = Intent(applicationContext, WritePostActivity::class.java)
                    intent.putExtra("uid", mFirebaseAuth.currentUser?.uid)
                    startActivity(intent)
                    overridePendingTransition(R.anim.horizon_enter, R.anim.none)
                    true
                }
                R.id.navigation_myPost -> {
                    true
                }
                R.id.navigation_myAccount -> {
                    val intent = Intent(applicationContext, ManageAccountActivity::class.java)
                    intent.flags = (Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    startActivityIfNeeded(intent, 0)
                    overridePendingTransition(R.anim.none, R.anim.none)
                    true
                }
                else -> false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_manage_post, menu)
        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.delete_all_post -> {
                //TODO
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.navigation_myPost
        observerData_myPost()

    }

    fun observerData_myPost() {
        viewModel.fetchMyPost().observe(this@ManagePostActivity, Observer {
            adapter.setListData(it)
            adapter.notifyDataSetChanged()
        })
    }
}