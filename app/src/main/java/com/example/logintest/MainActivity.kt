package com.example.logintest

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue

class MainActivity : BasicActivity() {

    private lateinit var mFirebaseAuth: FirebaseAuth
    private lateinit var mDatabaseReference: DatabaseReference

    private lateinit var adapter:ListAdapter
    private lateinit var txtSubject: TextView
    private val viewModel by lazy { ViewModelProvider(this).get(ListViewModel::class.java) }
    private var selectedDormCategory: String? = null
    private var selectedFoodCategory: String? = null
    private var userLocation: String? = null
    private lateinit var btnArea: Button



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mFirebaseAuth = FirebaseAuth.getInstance()
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("logintest")

        selectedDormCategory = "전체"
        selectedFoodCategory = "전체"

        mDatabaseReference.child("UserAccount").child(mFirebaseAuth.currentUser?.uid!!).child("dorm").get().addOnSuccessListener {
            userLocation = it.value.toString()
        }.addOnFailureListener {
            Toast.makeText(this@MainActivity, "get() dorm failed", Toast.LENGTH_SHORT).show()
        }

        val btnManageAccount: ImageButton = findViewById(R.id.btn_manageAccount)
        btnManageAccount.setOnClickListener {
            val intent = Intent(this@MainActivity, ManageAccountActivity::class.java)
            startActivity(intent)
        }

        val btnAdd: FloatingActionButton = findViewById(R.id.btn_add)
        btnAdd.setOnClickListener {
            // 자신이 쓴 게시글 있는지 확인하는 코드 추가해야함

            // 이걸로 받아오고싶은데 이러면 'Query'이고 내가 원하는건 Post 객체들 중 uid가 유저의 uid랑 같은 Post list인데 이걸 어케 하는지 모르겠음
            // val a = mDatabaseReference.child("Post").orderByChild("uid").equalTo(mFirebaseAuth.currentUser?.uid)

            //근데 데이터 구조 보다가 본건데 회원 정보는 realtime database가 아니라 storage에 넣어주는게 좋지 않을까
            val sameUserPosts = ArrayList<Post>()
            mDatabaseReference.child("Post").orderByChild("uid").equalTo(mFirebaseAuth.currentUser?.uid).addListenerForSingleValueEvent(object :
                    ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    //sameUserPosts.clear()
                    for (data in snapshot.children){
                        sameUserPosts.add(data.getValue<Post>()!!)
                    }
                    if (sameUserPosts.isEmpty()) {
                        println(sameUserPosts.size)
                        val intent2 = Intent(this@MainActivity, WritePostActivity::class.java)
                        intent2.putExtra("uid", mFirebaseAuth.currentUser?.uid)
                        startActivity(intent2)
                    }else {
                        MaterialAlertDialogBuilder(this@MainActivity).setMessage("이미 작성한 게시물이 ${sameUserPosts.size}개 있습니다. 또 작성하시겠습니까?")
                                .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                                    val intent2 = Intent(this@MainActivity, WritePostActivity::class.java)
                                    intent2.putExtra("uid", mFirebaseAuth.currentUser?.uid)
                                    startActivity(intent2)
                                })
                                .setNegativeButton("취소"){ _, _ ->  }.show()

                        //Toast.makeText(this@MainActivity, "이미 작성한 게시물이 ${sameUserPosts.size}개 있습니다. 또 작성하시겠습니까?", Toast.LENGTH_SHORT).show()
                    }
                }
            })

        }


        adapter = ListAdapter(this)

        val recyclerView : RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
        recyclerView.adapter = adapter
        observerData()

        txtSubject= findViewById(R.id.txt_subject)
        btnArea = findViewById(R.id.btn_area)

    }

    fun changeListwithFoodCategory(v: View) {
        val foodCategory: String = v.tag.toString()
        selectedFoodCategory = foodCategory
        observerData()
        txtSubject.text = foodCategory
    }

    fun observerData(){
        viewModel.fetchData(selectedFoodCategory, selectedDormCategory, userLocation).observe(this, Observer {
            //Toast.makeText(this@MainActivity, "${it.size}개의 포스트 감지", Toast.LENGTH_SHORT).show()
            adapter.setListData(it)
            adapter.notifyDataSetChanged()
        })
    }

    fun selectArea(view: View) {
        val dormCategory = arrayOf("전체", "같은 건물만", "북측기숙사", "서측기숙사", "동측기숙사", "문지캠", "화암캠" )
        MaterialAlertDialogBuilder(this).setTitle("지역 선택").setItems(dormCategory, DialogInterface.OnClickListener { dialog, which ->
            // The 'which' argument contains the index position
            // of the selected item
            selectedDormCategory = dormCategory[which]
            val obj: String = "지역: $selectedDormCategory"
            btnArea.text = obj
            observerData()
        }).show()
    }

}