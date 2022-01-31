package com.example.logintest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import org.w3c.dom.Text

class MainActivity : BasicActivity() {

    private lateinit var mFirebaseAuth: FirebaseAuth
    private lateinit var mDatabaseReference: DatabaseReference

    private lateinit var adapter:ListAdapter
    private val viewModel by lazy { ViewModelProvider(this).get(ListViewModel::class.java) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mFirebaseAuth = FirebaseAuth.getInstance()
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("logintest")

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
                        //Toast.makeText(this@MainActivity, "${sameUserPosts.toString()}", Toast.LENGTH_SHORT).show()
                        Toast.makeText(this@MainActivity, "이미 작성한 게시물이 ${sameUserPosts.size}개 있습니다. 또 작성하시겠습니까?", Toast.LENGTH_SHORT).show()
                    }
                }
            })

        }


        adapter = ListAdapter(this)

        val recyclerView : RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
        recyclerView.adapter = adapter
        observerData("전체")

        val txtSubject: TextView = findViewById(R.id.txt_subject)

        val btnAllCategory: Button = findViewById(R.id.btn_allCategory)
        btnAllCategory.setOnClickListener{
            observerData("전체")
            txtSubject.text = "전체"
        }
        val btnAsian: Button = findViewById(R.id.btn_asian)
        btnAsian.setOnClickListener{
            observerData("아시안")
            txtSubject.text = "아시안"
        }
        val btnBoonsik: Button = findViewById(R.id.btn_boonsik)
        btnBoonsik.setOnClickListener{
            observerData("분식")
            txtSubject.text = "분식"
        }
        val btnBurger: Button = findViewById(R.id.btn_burger)
        btnBurger.setOnClickListener{
            observerData("버거")
            txtSubject.text = "버거"
        }
        val btnChicken: Button = findViewById(R.id.btn_chicken)
        btnChicken.setOnClickListener{
            observerData("치킨")
            txtSubject.text = "치킨"
        }
        val btnChinese: Button = findViewById(R.id.btn_chinese)
        btnChinese.setOnClickListener{
            observerData("중식")
            txtSubject.text = "중식"
        }
        val btnCoffee: Button = findViewById(R.id.btn_coffee)
        btnCoffee.setOnClickListener{
            observerData("커피·차")
            txtSubject.text = "커피·차"
        }
        val btnDessert: Button = findViewById(R.id.btn_dessert)
        btnDessert.setOnClickListener{
            observerData("디저트")
            txtSubject.text = "디저트"
        }
        val btnDonkkas: Button = findViewById(R.id.btn_donkkas)
        btnDonkkas.setOnClickListener{
            observerData("돈까스")
            txtSubject.text = "돈까스"
        }
        val btnJapanese: Button = findViewById(R.id.btn_japanese)
        btnJapanese.setOnClickListener{
            observerData("일식")
            txtSubject.text = "일식"
        }
        val btnJokbal: Button = findViewById(R.id.btn_jokbal)
        btnJokbal.setOnClickListener{
            observerData("족발·보쌈")
            txtSubject.text = "족발·보쌈"
        }
        val btnJook: Button = findViewById(R.id.btn_jook)
        btnJook.setOnClickListener{
            observerData("죽")
            txtSubject.text = "죽"
        }
        val btnKorean: Button = findViewById(R.id.btn_korean)
        btnKorean.setOnClickListener{
            observerData("한식")
            txtSubject.text = "한식"
        }
        val btnMexican: Button = findViewById(R.id.btn_mexican)
        btnMexican.setOnClickListener{
            observerData("멕시칸")
            txtSubject.text = "멕시칸"
        }
        val btnPizza: Button = findViewById(R.id.btn_pizza)
        btnPizza.setOnClickListener{
            observerData("피자")
            txtSubject.text = "피자"
        }
        val btnRawFish: Button = findViewById(R.id.btn_rawFish)
        btnRawFish.setOnClickListener{
            observerData("회·해물")
            txtSubject.text = "회·해물"
        }
        val btnRoast: Button = findViewById(R.id.btn_roast)
        btnRoast.setOnClickListener{
            observerData("구이")
            txtSubject.text = "구이"
        }
        val btnSalad: Button = findViewById(R.id.btn_salad)
        btnSalad.setOnClickListener{
            observerData("샐러드")
            txtSubject.text = "샐러드"
        }
        val btnSandwich: Button = findViewById(R.id.btn_sandwich)
        btnSandwich.setOnClickListener{
            observerData("샌드위치")
            txtSubject.text = "샌드위치"
        }
        val btnWestern: Button = findViewById(R.id.btn_western)
        btnWestern.setOnClickListener{
            observerData("양식")
            txtSubject.text = "양식"
        }
        val btnZzim: Button = findViewById(R.id.btn_zzim)
        btnZzim.setOnClickListener{
            observerData("찜·탕")
            txtSubject.text = "찜·탕"
        }
        val btnMealBox: Button = findViewById(R.id.btn_mealBox)
        btnMealBox.setOnClickListener {
            observerData("도시락")
            txtSubject.text = "도시락"
        }


        val btnArea: Button = findViewById(R.id.btn_area)
        btnArea.setOnClickListener {
            var selectedItemIndex: Int = 0
            val dormCategory = arrayOf("같은 건물만", "북측기숙사", "서측기숙사", "동측기숙사", "문지관", "화암관" )
            var selectedDormCategory = dormCategory[selectedItemIndex]
            MaterialAlertDialogBuilder(this).setTitle("기숙사 선택").setSingleChoiceItems(dormCategory, selectedItemIndex) { _, which ->
                selectedItemIndex = which
                selectedDormCategory = dormCategory[which]
            }.setPositiveButton("확인") { _, _ ->
                btnArea.setText(selectedDormCategory)
            }.setNeutralButton("취소") { _, _ ->  }.show()
        }

    }

    fun observerData(category:String){
        viewModel.fetchData(category).observe(this, Observer {
            //Toast.makeText(this@MainActivity, "${it.size}개의 포스트 감지", Toast.LENGTH_SHORT).show()
            adapter.setListData(it)
            adapter.notifyDataSetChanged()
        })
    }


    fun isNorth(dorm: String): Boolean {
        return dorm == "사랑관" || dorm == "소망관" || dorm == "성실관" || dorm == "진리관" || dorm == "아름관" || dorm == "신뢰관" || dorm == "지혜관"
    }
    fun isWest(dorm: String): Boolean {
        return dorm == "갈릴레이관" || dorm == "여울/나들관" || dorm == "다솜/희망관" || dorm == "원내아파트" || dorm == "나래/미르관" || dorm == "나눔관"
    }
    fun isEast(dorm: String): Boolean {
        return dorm == "세종관"
    }
    fun isMunji(dorm: String): Boolean {
        return dorm == "문지관"
    }
    fun isHwaam(dorm: String): Boolean {
        return dorm == "화암관"
    }


}