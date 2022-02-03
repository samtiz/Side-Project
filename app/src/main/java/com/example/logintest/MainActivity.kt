package com.example.logintest

import android.app.SearchManager
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.database.MatrixCursor
import android.os.Bundle
import android.provider.BaseColumns
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.MenuItemCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import kotlinx.android.synthetic.main.activity_main.*

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
    private var searchText: String? = null
    private var restaurantNameList: ArrayList<String?> = ArrayList() // 추천검색어용
    private lateinit var suggestions: List<String?>



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mFirebaseAuth = FirebaseAuth.getInstance()
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("logintest")

        selectedDormCategory = "전체"
        selectedFoodCategory = "전체"

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.setDisplayShowTitleEnabled(false)


        // userLocation 받아오기. 기본은 자기기숙사
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
                    sameUserPosts.clear()
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
        recyclerView.layoutManager = WrapContentLinearLayoutManager(this@MainActivity) // recyclerView의 안정성을 위함
        recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
        recyclerView.adapter = adapter
        observerData() // 초기 데이터 가져오기
        mDatabaseReference.child("Post").addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val temp = snapshot.getValue(Post::class.java)
                if (temp != null) {
                    restaurantNameList.add(temp.restaurantName)
                }
                suggestions = restaurantNameList.distinct()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                // 빡치지만 이전 데이터를 가져올 수 없음. 수정 기능을 만들 때 데이터를 지우고 새 데이터를 추가할 것.
                // 혹은 valueEventListener로 매번 restaurantNameList를 지우고 새로만들고 해도 되긴 한데... 별로지 않나..
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val temp = snapshot.getValue(Post::class.java)
                if (temp != null) {
                    restaurantNameList.remove(temp.restaurantName)
                }
                suggestions = restaurantNameList.distinct()
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "value listener of post dir failed", Toast.LENGTH_SHORT).show()
            }

        })



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
            adapter.setListData(it)
            adapter.filter.filter(searchText)
            adapter.notifyDataSetChanged()
        })
    }

    fun selectArea(view: View) {
        val dormCategory = arrayOf("전체", "같은 건물만", "북측기숙사", "서측기숙사", "동측기숙사", "문지캠", "화암캠" )
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

        searchView.setOnSuggestionListener(object: SearchView.OnSuggestionListener {
            override fun onSuggestionSelect(position: Int): Boolean {
                return false
            }

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
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                searchText = ""
                searchView.setQuery("", false)
                Fragment().hideKeyboard()
                searchView.clearFocus()
                observerData()
                return true
            }
        })


        return super.onCreateOptionsMenu(menu)
    }




}


