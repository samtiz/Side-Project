package com.example.logintest

import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.Intent
import android.database.Cursor
import android.database.MatrixCursor
import android.graphics.PorterDuff
import android.os.Bundle
import android.provider.BaseColumns
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.*
import android.widget.Spinner
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : BasicActivity() {

    private lateinit var mFirebaseAuth: FirebaseAuth
    private lateinit var mDatabaseReference: DatabaseReference
    private var userLocation: String? = null
    private lateinit var adapter:ListAdapter
    private lateinit var txtSubject: TextView
    private val viewModel by lazy { ViewModelProvider(this).get(ListViewModel::class.java) }
    private var selectedDormCategory: String? = null
    private var selectedFoodCategory: String? = null
    private lateinit var btnArea: Button
    private var searchText: String? = null
    private var restaurantNameList: ArrayList<String?> = ArrayList() // 추천검색어용
    private lateinit var suggestions: List<String?>
    private lateinit var spinnerItem: Spinner
//    private var fragPager: ViewPager? = null
//    private var navigationBar: TabLayout? = null

    private var postId : String? = null
    private var uid : String? = null
    private var userName : String? = null

    private var btnCat1: Button? = null
    private var btnCat2: Button? = null
    private var btnCat3: Button? = null
    private var btnCat4: Button? = null
    private var btnCat5: Button? = null
    private var btnCat6: Button? = null
    private var btnCat7: Button? = null
    private var btnCat8: Button? = null
    private var btnCat9: Button? = null
    private var btnCat10: Button? = null
    private var btnCat11: Button? = null
    private var btnCat12: Button? = null
    private var btnCat13: Button? = null
    private var btnCat14: Button? = null
    private var btnCat15: Button? = null
    private var btnCat16: Button? = null
    private var btnCat17: Button? = null
    private var btnCat18: Button? = null
    private var btnCat19: Button? = null
    private var btnCat20: Button? = null
    private var btnCat21: Button? = null
    private var btnCat22: Button? = null
    private var btnCatList: ArrayList<Button?>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mFirebaseAuth = FirebaseAuth.getInstance()
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("logintest")

        selectedDormCategory = "전체"
        selectedFoodCategory = "전체"

        spinnerItem = findViewById<Spinner>(R.id.loc_spinner)

        setSupportActionBar(toolbar_main)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val GAA = GoogleApiAvailability.getInstance()
        GAA.makeGooglePlayServicesAvailable(this@MainActivity).addOnCompleteListener {
            if (!it.isSuccessful) {
                Toast.makeText(this, "makeGooglePlayServicesAvailable failed", Toast.LENGTH_SHORT).show()
                Log.d("error", "makeGooglePlayServicesAvailable failed")
            }
        }

        uid = mFirebaseAuth.currentUser?.uid!!

        mDatabaseReference.child("UserAccount").child(uid!!).child("nowChatting").setValue(false)

        mDatabaseReference.child("UserAccount").child(uid!!).child("firebaseToken").get().addOnSuccessListener {
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("error", "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new FCM registration token
                val realToken = task.result.toString()
                val savedToken = it.value.toString()

                if (realToken != savedToken) {
                    mDatabaseReference.child("UserAccount").child(uid!!).child("firebaseToken").setValue(realToken)
                }
            })
        }

        adapter = ListAdapter(this@MainActivity)

        // swipe로 refresh하기
        swipeRefreshLayout.setOnRefreshListener {
            observerData()
            swipeRefreshLayout.isRefreshing = false
        }

        val recyclerView : RecyclerView = findViewById(R.id.recyclerView_main)
        recyclerView.layoutManager = WrapContentLinearLayoutManager(this@MainActivity) // recyclerView의 안정성을 위함
//        recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
        recyclerView.adapter = adapter
        observerData() // 초기 데이터 가져오기

        mDatabaseReference.child("Post").addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
//                if (snapshot.value is Post ) {
//                    Log.d("111111111111111", "11111111111111111")
                    val temp = snapshot.getValue(Post::class.java)
                    if (temp != null) {
                        restaurantNameList.add(temp.restaurantName)
                    }
                    suggestions = restaurantNameList.distinct()
//                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                // 빡치지만 이전 데이터를 가져올 수 없음. 수정 기능을 만들 때 데이터를 지우고 새 데이터를 추가할 것.
                // 혹은 valueEventListener로 매번 restaurantNameList를 지우고 새로만들고 해도 되긴 한데... 별로지 않나..
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
//                if (snapshot.value is Post ) {
                    val temp = snapshot.getValue(Post::class.java)
                    if (temp != null) {
                        restaurantNameList.remove(temp.restaurantName)
                    }
                    suggestions = restaurantNameList.distinct()
//                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                Toast.makeText(this@MainActivity, "do not reach here: onChildMoved", Toast.LENGTH_SHORT).show()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "child event listener of post dir failed", Toast.LENGTH_SHORT).show()
            }

        })



        txtSubject= findViewById(R.id.txt_subject)
        btnArea = findViewById(R.id.btn_area)
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.navigation_home
        bottomNavigationView.setOnNavigationItemSelectedListener {item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    true
                }
                R.id.navigation_chat -> {
                    mDatabaseReference.child("UserAccount").child(uid!!).child("postId").get().addOnSuccessListener {
                        postId = it.value.toString()
                        if (postId == "null"){
                            Toast.makeText(applicationContext, "참여하신 채팅방이 없습니다.", Toast.LENGTH_SHORT).show()
                            bottomNavigationView.selectedItemId = R.id.navigation_home

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
//                                MaterialAlertDialogBuilder(this@MainActivity).setMessage("이미 작성한 게시물이 ${sameUserPosts.size}개 있습니다. 또 작성하시겠습니까?")
//                                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
//                                        val intent2 = Intent(applicationContext, WritePostActivity::class.java)
//                                        intent2.putExtra("uid", mFirebaseAuth.currentUser?.uid)
//                                        startActivity(intent2)
//                                    })
//                                    .setNegativeButton("취소") { _, _ -> }.show()
//                            }
//                        }
//                    })
                    mDatabaseReference.child("UserAccount").child(uid!!).child("postId").get().addOnSuccessListener {
                        postId = it.value.toString()
                        if(postId == "null"){
                            val intent = Intent(applicationContext, WritePostActivity::class.java)
                            intent.putExtra("uid", uid)
                            startActivity(intent)
                            overridePendingTransition(R.anim.horizon_enter, R.anim.none)
                        }
                        else{
                            Toast.makeText(applicationContext, "이미 참여한 모집방이 존재합니다.", Toast.LENGTH_SHORT).show()
                            bottomNavigationView.selectedItemId = R.id.navigation_home
                        }
                    }.addOnFailureListener {
                    }

                    true
                }
//                R.id.navigation_myPost -> {
//                    val intent = Intent(applicationContext, ManagePostActivity::class.java)
//                    intent.flags = (Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
//                    startActivityIfNeeded(intent, 0)
//                    overridePendingTransition(R.anim.none, R.anim.none)
//                    true
//                }
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

        btnCat1 = findViewById(R.id.btn_allCategory)
        btnCat2 = findViewById(R.id.btn_korean)
        btnCat3 = findViewById(R.id.btn_chicken)
        btnCat4 = findViewById(R.id.btn_boonsik)
        btnCat5 = findViewById(R.id.btn_donkkas)
        btnCat6 = findViewById(R.id.btn_jokbal)
        btnCat7 = findViewById(R.id.btn_zzim)
        btnCat8 = findViewById(R.id.btn_roast)
        btnCat9 = findViewById(R.id.btn_pizza)
        btnCat10 = findViewById(R.id.btn_chinese)
        btnCat11 = findViewById(R.id.btn_japanese)
        btnCat12 = findViewById(R.id.btn_rawFish)
        btnCat13 = findViewById(R.id.btn_western)
        btnCat14 = findViewById(R.id.btn_coffee)
        btnCat15 = findViewById(R.id.btn_dessert)
        btnCat16 = findViewById(R.id.btn_asian)
        btnCat17 = findViewById(R.id.btn_sandwich)
        btnCat18 = findViewById(R.id.btn_salad)
        btnCat19 = findViewById(R.id.btn_burger)
        btnCat20 = findViewById(R.id.btn_mexican)
        btnCat21 = findViewById(R.id.btn_mealBox)
        btnCat22 = findViewById(R.id.btn_jook)
        btnCatList = arrayListOf(btnCat1, btnCat2, btnCat3, btnCat4, btnCat5, btnCat6, btnCat7, btnCat8, btnCat9, btnCat10,
            btnCat11, btnCat12, btnCat13, btnCat14, btnCat15, btnCat16, btnCat17, btnCat18, btnCat19, btnCat20, btnCat21, btnCat22, )
        btnCat1?.isSelected = true

    }

    override fun onResume() {
        super.onResume()
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.navigation_home
        observerData()

    }




    fun changeListwithFoodCategory(v: View) {
        val foodCategory: String = v.tag.toString()
        selectedFoodCategory = foodCategory
        observerData()
        txtSubject.text = foodCategory
        btnCatList?.forEach {
            if (it?.isSelected == true && it != v) {
                it.isSelected = false
            }
        }
        v.isSelected = true
    }


    @SuppressLint("NotifyDataSetChanged")
    fun observerData(){
        mDatabaseReference.child("UserAccount").child(uid!!).child("postId").get().addOnSuccessListener { snapshot ->
            val userPostId = snapshot.value.toString()
            if (userPostId != "null") {
                viewModel.fetchData(selectedFoodCategory, selectedDormCategory, userLocation).observe(this, Observer {
                    for (i: Int in 0 until it.size) {
                        if (it[i].users.contains(uid)) {
                            val p = it[i]
                            it.removeAt(i)
                            it.add(0, p)
                        }
                    }
                    adapter.setListData(it)
                    if (!searchText.isNullOrEmpty()) {
                        adapter.filter.filter(searchText)
                    }
                    adapter.notifyDataSetChanged()
                })
            }
            else {
                viewModel.fetchData(selectedFoodCategory, selectedDormCategory, userLocation).observe(this, Observer {
                    adapter.setListData(it)
                    if (!searchText.isNullOrEmpty()) {
                        adapter.filter.filter(searchText)
                    }
                    adapter.notifyDataSetChanged()
                })
            }
        }.addOnFailureListener{ e ->
            viewModel.fetchData(selectedFoodCategory, selectedDormCategory, userLocation).observe(this, Observer {
                adapter.setListData(it)
                if (!searchText.isNullOrEmpty()) {
                    adapter.filter.filter(searchText)
                }
                adapter.notifyDataSetChanged()
            })
            Toast.makeText(this@MainActivity, "get() postId of User failed", Toast.LENGTH_SHORT).show()
            Log.d("MainActivity", e.message.toString())
        }

    }

    fun selectArea(view: View) {
        val dormCategory = arrayOf("전체", "같은 위치만", "북측", "서측", "동측", "문지캠", "화암캠")
        MaterialAlertDialogBuilder(this@MainActivity).setTitle("지역 선택").setItems(dormCategory) { dialog, which ->
            // The 'which' argument contains the index position
            // of the selected item
            selectedDormCategory = dormCategory[which]
            val obj: String = "지역: $selectedDormCategory"
            btnArea.text = obj
            observerData()
        }.show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        val menuItem = menu?.findItem(R.id.action_search)

        val searchView: SearchView = menuItem?.actionView as SearchView

        val spinnerArray: Array<String> = arrayOf("택시승강장" ,"쪽문", "세종관", "사랑관", "소망관", "성실관", "진리관", "아름관", "신뢰관", "지혜관", "갈릴레이관",
                "여울/나들관", "다솜/희망관", "원내아파트", "나래/미르관", "나눔관", "문지관", "화암관")

        val loc_spinnerAdapter: ArrayAdapter<String> = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, spinnerArray)
        spinnerItem.adapter = loc_spinnerAdapter

        mFirebaseAuth = FirebaseAuth.getInstance()
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("logintest")

        // userLocation 받아오기. 기본은 자기기숙사
        if (userLocation == null) {
            mDatabaseReference.child("UserAccount").child(uid.toString()).child("dorm").get().addOnSuccessListener {
                userLocation = it.value.toString()
                spinnerItem.setSelection(spinnerArray.indexOf(userLocation))
                val app = applicationContext as GlobalVariable
                app.setUserLocation(userLocation)
            }.addOnFailureListener {
                Toast.makeText(this@MainActivity, "get() dorm failed", Toast.LENGTH_SHORT).show()
            }
        }
        else {
            spinnerItem.setSelection(spinnerArray.indexOf(userLocation))
        }


        spinnerItem.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                userLocation = spinnerArray[position]
                val app = applicationContext as GlobalVariable
                app.setUserLocation(userLocation)
                observerData()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }



        searchView.onActionViewExpanded()  // 두번째 검색버튼 눌린걸 디폴트로
        searchView.maxWidth = Integer.MAX_VALUE
        searchView.queryHint = "어느 음식점을 찾으시나요?"

        // 검색어 추천

        // Tlqkf 이거 왜않되 도대체 안해 Tlqkf
//        searchView.findViewById<AutoCompleteTextView>(R.id.search_src_text).threshold = 1

        val from = arrayOf(SearchManager.SUGGEST_COLUMN_TEXT_1)
        val to = intArrayOf(R.id.item_label)
        val cursorAdapter = SimpleCursorAdapter(this@MainActivity, R.layout.item_suggestion, null, from, to, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER)
        searchView.suggestionsAdapter = cursorAdapter


        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val temp = selectedFoodCategory
                selectedFoodCategory = "전체"
                observerData()
                selectedFoodCategory = temp
                Fragment().hideKeyboard()
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchText = newText
                val cursor = MatrixCursor(arrayOf(BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1))
                newText?.let {
                    suggestions.forEachIndexed { index, suggestion ->
                        if (suggestion != null) {
                            if (suggestion.contains(newText, true))
                                cursor.addRow(arrayOf(index, suggestion))
                        }
                    }
                }
                cursorAdapter.changeCursor(cursor)
                return true

            }
        })

        searchView.setOnSuggestionListener(object : SearchView.OnSuggestionListener {
            override fun onSuggestionSelect(position: Int): Boolean {
                return false
            }

            @SuppressLint("Range")
            override fun onSuggestionClick(position: Int): Boolean {
                Fragment().hideKeyboard()
                val cursor = searchView.suggestionsAdapter.getItem(position) as Cursor
                val selection = cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1))

                searchView.setQuery(selection, true)
                // Do something with selection
                return true
            }

        })


        menuItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                searchView.isIconified = false
                spinnerItem.visibility = GONE
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                searchText = ""
                searchView.setQuery("", false)
                Fragment().hideKeyboard()
                spinnerItem.visibility = VISIBLE
                searchView.clearFocus()
                observerData()
                return true
            }
        })


        return super.onCreateOptionsMenu(menu)
    }




}


