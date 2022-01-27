package com.example.logintest

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.RangeSlider
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

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
    private lateinit var mDatabaseReference: DatabaseReference  // Firebase real time database

    private var uid: String? = null
    private var minCost: Int? = null
    private var maxCost: Int? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write_post)

        if (intent.hasExtra("uid")) {
            uid = intent.getStringExtra("uid")
        }

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

        mBtnPost.setOnClickListener {
            if (mEtFoodCategory.text.isBlank() || mEtRestaurantName.text.isBlank() || mEtTime.text.isBlank()) {
                Toast.makeText(this@WritePostActivity, "음식 카테고리, 음식점, 만료시간을 모두 설정해주세요", Toast.LENGTH_SHORT).show()
            }
            else {
                val post = Post()
                post.foodCategory = mEtFoodCategory.text.toString()
                post.restaurantName = mEtRestaurantName.text.toString()
                post.minDeliveryFee = minCost
                post.maxDeliveryFee = maxCost
                post.mainText = mEtMainText.text.toString()
                post.timeLimit = mEtTime.text.toString()

                uid?.let { it1 -> mDatabaseReference.child("Post").child(it1).setValue(post) }

                val intent = Intent(this@WritePostActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }

        }

        mBtnTime.setOnClickListener {
            val cal = Calendar.getInstance()
            val timeSetListener = TimePickerDialog.OnTimeSetListener { view, hour, minute ->
                val timeString = "${hour}:${minute}"
                mEtTime.setText(timeString)
            }
            TimePickerDialog(this, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE),true).show()
        }

    }

    private val rangeSliderTouchListener: RangeSlider.OnSliderTouchListener = object : RangeSlider.OnSliderTouchListener {
        override fun onStartTrackingTouch(slider: RangeSlider) {}
        override fun onStopTrackingTouch(slider: RangeSlider) {
            minCost = mCostSlider.values[0].toInt()
            maxCost = mCostSlider.values[1].toInt()
            mTxtSelectedFee.text = "$minCost 원 ~ $maxCost 원"
        }
    }

    var selectedItemIndex = 0
    fun showFoodCategoryDialog(view: View) {
        val foods = arrayOf("한식", "치킨", "분식", "돈까스", "족발/보쌈", "찜/탕", "구이", "피자", "중식", "일식", "회/해물", "양식", "커피/차", "디저트", "간식", "아시안", "샌드위치", "샐러드", "버거", "멕시칸", "도시락", "죽", "프렌차이즈")
        var selectedFoodCategory = foods[selectedItemIndex]
        MaterialAlertDialogBuilder(this).setTitle("배달 카테고리 선택").setSingleChoiceItems(foods, selectedItemIndex) { _, which ->
            selectedItemIndex = which
            selectedFoodCategory = foods[which]
        }.setPositiveButton("확인") { _, _ ->
            mEtFoodCategory.setText(selectedFoodCategory)
        }.setNeutralButton("취소") { _, _ ->  }.show()

    }


}

