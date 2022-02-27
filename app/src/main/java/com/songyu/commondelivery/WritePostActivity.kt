package com.songyu.commondelivery

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.RangeSlider
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_write_post.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class WritePostActivity: BasicActivity() {

    private lateinit var mEtFoodCategory: EditText
    private lateinit var mBtnFoodCategory: Button
    private lateinit var mEtRestaurantName: EditText
    private lateinit var mSwitch: Switch
    private lateinit var mTxtDeliveryFee: TextView
    private lateinit var mCostSlider: RangeSlider
    private lateinit var mTxtSelectedFee: TextView
    private lateinit var mEtTime: EditText
    private lateinit var mBtnTime: Button
    private lateinit var mTxtTimeWarning: TextView
    private lateinit var mEtMainText: EditText
    private lateinit var mBtnPost: Button
    private lateinit var mEtLocation: EditText
    private lateinit var mBtnLocation: Button
    private lateinit var mDatabaseReference: DatabaseReference  // Firebase real time database

    private var users: HashMap<String, String>? = null
    private var comments: HashMap<String, PostComment>? = null
    private var uid: String? = null
    private var postId: String? = null
    private var isModify: Boolean = false
    private var realTimeLimit: String? = null

    // 현태 수정 ///////
    private var userName: String? = null
    /////////////////

    private var dorm: String? = null
    private var minCost: Int? = 1000
    private var maxCost: Int? = 4000

    private val selectedItems = ArrayList<Int>()
    private val foods = arrayOf("한식", "치킨", "분식", "돈까스", "족발·보쌈", "찜·탕", "구이", "피자", "중식", "일식", "회·해물", "양식", "커피·차", "디저트", "아시안", "샌드위치", "샐러드", "버거", "멕시칸", "도시락", "죽")
    private val locations = arrayOf("택시승강장", "쪽문", "세종관", "사랑관", "소망관", "성실관", "진리관", "아름관", "신뢰관", "지혜관", "갈릴레이관",
            "여울/나들관", "다솜/희망관", "원내아파트", "나래/미르관", "나눔관", "문지관", "화암관")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write_post)

        if (intent.hasExtra("uid")) {
            uid = intent.getStringExtra("uid")
        }



        setSupportActionBar(toolbar_write_post)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        mDatabaseReference = FirebaseDatabase.getInstance().getReference("logintest")
        mEtFoodCategory = findViewById(R.id.edit_category)
        mBtnFoodCategory = findViewById(R.id.btn_category)
        mEtRestaurantName = findViewById(R.id.edit_restaurant_name)
        mSwitch = findViewById(R.id.sw_decided)
        mTxtDeliveryFee = findViewById(R.id.txt_deliveryFee)
        mCostSlider = findViewById(R.id.slide_costRange)
        mTxtSelectedFee = findViewById(R.id.txt_selectedFee)
        mEtTime = findViewById(R.id.edit_time)
        mBtnTime = findViewById(R.id.btn_time)
        mTxtTimeWarning = findViewById(R.id.txt_timeWarning)
        mEtMainText = findViewById(R.id.edit_mainText)
        mBtnPost = findViewById(R.id.btn_post)
        mEtLocation = findViewById(R.id.edit_location)
        mBtnLocation = findViewById(R.id.btn_location)


        val app = applicationContext as GlobalVariable
        mEtLocation.setText(app.getUserLocation())
        mBtnLocation.setOnClickListener{
            val builder = AlertDialog.Builder(this@WritePostActivity)
            builder.setTitle("배달 수령 위치").setItems(locations, DialogInterface.OnClickListener { _, which ->
                mEtLocation.setText(locations[which])
            })
            builder.show()
        }


        if (intent.hasExtra("postId")) {
            postId = intent.getStringExtra("postId")
            isModify = true
            mDatabaseReference.child("Post").child(postId!!).get().addOnSuccessListener {
                val pastPost = it.getValue(Post::class.java)
                var temp: String = ""
                for (cat in pastPost?.foodCategories!!) {
                    if (cat.isNotEmpty()) {
                        temp += cat
                        temp += ", "
                        selectedItems.add(foods.indexOf(cat))
                    }
                }
                mEtFoodCategory.setText(temp.trim().substring(0, temp.trim().length-1))
                mEtRestaurantName.setText(pastPost.restaurantName)
                mCostSlider.setValues(pastPost.minDeliveryFee?.toFloat(), pastPost.maxDeliveryFee?.toFloat())
                minCost = pastPost.minDeliveryFee
                maxCost = pastPost.maxDeliveryFee
                mEtMainText.setText(pastPost.mainText)
                mEtTime.setText(pastPost.timeLimit?.let { it1 -> leftPad(it1) })
                mEtLocation.setText(pastPost.dorm)
                users = pastPost.users
                comments = pastPost.comments
            }
        }


        var inputRestaurantName: String? = null
        mSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                inputRestaurantName = mEtRestaurantName.text.toString()
                mEtRestaurantName.setText("미정")
                mEtRestaurantName.isEnabled = false
                mTxtDeliveryFee.visibility = GONE
                mCostSlider.visibility = GONE
                mTxtSelectedFee.visibility = GONE

            }
            else {
                mEtRestaurantName.setText(inputRestaurantName)
                mEtRestaurantName.isEnabled = true
                mTxtDeliveryFee.visibility = VISIBLE
                mCostSlider.visibility = VISIBLE
                mTxtSelectedFee.visibility = VISIBLE
            }
        }

        mCostSlider.addOnSliderTouchListener(rangeSliderTouchListener)

//        /// 현태 수정 ////////
//        // 현재 사용자 닉네임 받아오기
//        mDatabaseReference.child("UserAccount").child(uid!!).child("nickname").get().addOnSuccessListener {
//            userName = it.value.toString()
//        }.addOnFailureListener {
//            Toast.makeText(this@WritePostActivity, "get() username failed", Toast.LENGTH_SHORT).show()
//        }
//        /////////////////////

        mDatabaseReference.child("UserAccount").child(uid!!).child("dorm").get().addOnSuccessListener {
            dorm = it.value.toString()
        }.addOnFailureListener {
            Toast.makeText(this@WritePostActivity, "get() dorm failed", Toast.LENGTH_SHORT).show()
        }


        mBtnPost.setOnClickListener {
            if (mEtFoodCategory.text.isBlank() || mEtRestaurantName.text.isBlank() || mEtTime.text.isBlank()) {
                Toast.makeText(this@WritePostActivity, "음식 카테고리, 음식점, 모집만료시간을 모두 설정해주세요", Toast.LENGTH_SHORT).show()
            }
            else if (mEtFoodCategory.text.split(", ").size > 4) {
                Toast.makeText(this@WritePostActivity, "음식 카테고리는 최대 4개까지만 설정 가능합니다.", Toast.LENGTH_SHORT).show()
            }
            else if (mEtFoodCategory.text.split(", ").size != 1 && mSwitch.isChecked) {
                Toast.makeText(this@WritePostActivity, "음식점 미정인 경우 카테고리는 한 가지만 설정해주세요.", Toast.LENGTH_SHORT).show()
            }
            else {

                val post = Post()
                val categoryList = ArrayList<String>()
                for (i in selectedItems) {
                    categoryList.add(foods[i])
                }
                while (categoryList.size < 4) {
                    categoryList.add("")
                }
                post.foodCategories = categoryList
                post.restaurantName = mEtRestaurantName.text.toString()
                post.minDeliveryFee = minCost
                post.maxDeliveryFee = maxCost
                post.mainText = mEtMainText.text.toString()
                post.timeLimit = realTimeLimit
                post.uid = uid
                post.dorm = mEtLocation.text.toString()
                post.visibility = true

                if (isModify) {
                    val timeText = mEtTime.text
                    val strTime = if (timeText[0] == '0') {
                        if (timeText[3] == '0') {
                            timeText.substring(1 until 3) + timeText[4].toString()
                        } else {
                            timeText.substring(1)
                        }
                    } else {
                        if (timeText[3] == '0') {
                            timeText.substring(0 until 3) + timeText[4].toString()
                        } else {
                            timeText
                        }
                    }
                    post.timeLimit = strTime.toString()
                    post.postId = postId
                    post.users = users!!
                    post.comments = comments!!
                    postId?.let { it1 -> mDatabaseReference.child("Post").child(it1).removeValue() }
                    postId?.let { it1 -> mDatabaseReference.child("Post").child(it1).setValue(post) }
                    Toast.makeText(this@WritePostActivity, "게시물을 수정하였습니다.", Toast.LENGTH_SHORT).show()
                }
                else {
                    val key = mDatabaseReference.child("Post").push().key

                    // 현태가 수정한 부분 //////
                    // post Id에 자동으로 부여된 게시물 id 넣기
                    post.postId = key
                    // post에 참여한 user hashmap에 현재 사용자 넣기
                    //post.users[uid!!] = userName!!
                    if (key == null) {
                        Toast.makeText(this@WritePostActivity, "게시를 실패하였습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        mDatabaseReference.child("Post").child(key).setValue(post)
                        Toast.makeText(this@WritePostActivity, "게시물을 올렸습니다.", Toast.LENGTH_SHORT).show()
                        //UserAccount에 postId 넣기
                        mDatabaseReference.child("UserAccount").child(uid!!).child("postId").setValue(key)
                        //Message 창으로 넘어가기
                        val intent = Intent(this@WritePostActivity, MessageActivity::class.java)
                        intent.putExtra("postId", key)
                        startActivity(intent)
                    }

                }

                //val userIdList = ArrayList<String>()
                //userIdList.add(uid!!)
                //post.usersId = userIdList

                //val userNameSet = MutableSet<String>()
                //userNameSet.add(userName!!)
                //post.userName = userNameSet
                ///////////////////////



//                    카테고리마다 차일드 만드는 주석
//                    for (category in categoryList) {
//                        if (category != "") {
//                            mDatabaseReference.child("${category}").child(key).setValue(post)
//                        }
//                    }
                finish()
            }

        }

        mBtnTime.setOnClickListener {
            val curTime = Date()
            val dateFormat = SimpleDateFormat("yyyyMMddkk:mm")
            val dateFormat2 = SimpleDateFormat("yyyyMMdd")
            val cal = Calendar.getInstance()
            var isToday = true
            var isAM = true
            val timeSetListener = TimePickerDialog.OnTimeSetListener { view, hour, minute ->
                realTimeLimit = "${hour}:${minute}"

                val setTime = dateFormat.parse(dateFormat2.format(curTime)+leftPad(realTimeLimit!!))

                println("시간")
                println(curTime)
                println(setTime)
                println(curTime.time)
                println(setTime.time)
                if (curTime.time > setTime.time){
                    isToday = false
                }
                var realTimeLimit2 = realTimeLimit
                if(hour > 12){
                    isAM = false
                    realTimeLimit2 = "${hour-12}:${minute}"
                }else if(hour == 12){
                    isAM = false
                    realTimeLimit2 = "${12}:${minute}"
                }else if (hour == 0){
                    realTimeLimit2 = "${12}:${minute}"
                }
                mEtTime.setText(
                    when {
                        isToday && isAM -> "오늘 오전 " + leftPad(realTimeLimit2!!)
                        isToday && !isAM -> "오늘 오후 " + leftPad(realTimeLimit2!!)
                        !isToday && isAM -> "내일 오전 " + leftPad(realTimeLimit2!!)
                        else -> {"내일 오후 " + leftPad(realTimeLimit2!!)}
                    }
                )
            }
            TimePickerDialog(this, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE),true).show()
        }



    }

    @SuppressLint("RestrictedApi")
    private val rangeSliderTouchListener: RangeSlider.OnSliderTouchListener = object : RangeSlider.OnSliderTouchListener {
        override fun onStartTrackingTouch(slider: RangeSlider) {}
        override fun onStopTrackingTouch(slider: RangeSlider) {
            minCost = mCostSlider.values[0].toInt()
            maxCost = mCostSlider.values[1].toInt()
            mTxtSelectedFee.text = "${minCost}원 ~ ${maxCost}원"
        }
    }


    fun showFoodCategoryDialog(view: View) {
        selectedItems.clear()
        MaterialAlertDialogBuilder(this@WritePostActivity).setTitle("배달 카테고리 모두 선택").setMultiChoiceItems(foods, null,  DialogInterface.OnMultiChoiceClickListener { dialog, which, isChecked ->
            if (isChecked) {
                // If the user checked the item, add it to the selected items
                selectedItems.add(which)
            } else if (selectedItems.contains(which)) {
                // Else, if the item is already in the array, remove it
                selectedItems.remove(which)
            }}).setPositiveButton("확인") { _, _ ->
                    var selectedFoodCategory: String = ""
                    for (i in selectedItems) {
                        selectedFoodCategory += foods[i]
                        selectedFoodCategory += ", "
                    }
            // TODO 아무것도 안선택하고 확인 누르면 에러ㅁㄴㅇ
                    if (selectedFoodCategory.isNotEmpty()) {
                        mEtFoodCategory.setText(selectedFoodCategory.trim().substring(0, selectedFoodCategory.trim().length-1))
                    }
                }.setNeutralButton("취소") { _, _ ->  }.show()

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                overridePendingTransition(R.anim.none, R.anim.horizon_exit)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


}

