package com.songyu.commondelivery

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import java.util.regex.Pattern

class RegisterActivity : BasicActivity() {
    private lateinit var mFirebaseAuth: FirebaseAuth            // Firebase certification
    private lateinit var mDatabaseReference: DatabaseReference  // Firebase real time database
    private lateinit var mEtEmail: EditText                     // email text
    private lateinit var mEtPwd: EditText                       // pwd text
    private lateinit var mTxtPwdCheck: TextView
    private lateinit var mEtPwdConfirm: EditText                // pwd text
    private lateinit var mTxtPwdConfirm: TextView
    private lateinit var mEtName: EditText                      // NickName text
    private lateinit var mBtnRegister: Button                   // Register button
    private lateinit var mTxtDorm: EditText
    private lateinit var mBtnDorm: Button
    private val dormitories = arrayOf("세종관", "사랑관", "소망관", "성실관", "진리관", "아름관", "신뢰관", "지혜관", "갈릴레이관",
        "여울/나들관", "다솜/희망관", "원내아파트", "나래/미르관", "나눔관", "문지관", "화암관")
    private var selectedItemIndex = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        mFirebaseAuth = FirebaseAuth.getInstance()
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("logintest")

        mEtEmail = findViewById(R.id.edit_email)
        mEtPwd = findViewById(R.id.edit_pwd)
        mTxtPwdCheck = findViewById(R.id.txt_pwdcheck)
        mEtName = findViewById(R.id.edit_name)
        mEtPwdConfirm = findViewById(R.id.edit_pwdconfirm)
        mTxtPwdConfirm = findViewById(R.id.txt_pwdconfirm)
        mBtnRegister = findViewById((R.id.btn_register))
        mTxtDorm = findViewById(R.id.txt_dorm)
        mBtnDorm = findViewById(R.id.btn_dorm)


        var isPwdValid = false

        mEtPwdConfirm.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (mEtPwd.text.toString() == mEtPwdConfirm.text.toString()) {
                    mTxtPwdConfirm.text = "* 비밀번호가 일치합니다."
                    mTxtPwdConfirm.setTextColor(Color.parseColor("#88006400"))
                }
                else {
                    mTxtPwdConfirm.text = "* 비밀번호가 일치하지 않습니다."
                    mTxtPwdConfirm.setTextColor(Color.parseColor("#88ff0000"))
                }
            }
        })

        mEtPwdConfirm.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
            override fun afterTextChanged(s: Editable?) {
                if (mEtPwd.text.toString() == mEtPwdConfirm.text.toString()) {
                    mTxtPwdConfirm.text = "* 비밀번호가 일치합니다."
                    mTxtPwdConfirm.setTextColor(Color.parseColor("#88006400"))
                }
                else {
                    mTxtPwdConfirm.text = "* 비밀번호가 일치하지 않습니다."
                    mTxtPwdConfirm.setTextColor(Color.parseColor("#88ff0000"))
                }

                mEtPwd.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                    override fun afterTextChanged(s: Editable?) {
                        if (mEtPwd.text.toString() == mEtPwdConfirm.text.toString()) {
                            mTxtPwdConfirm.text = "* 비밀번호가 일치합니다."
                            mTxtPwdConfirm.setTextColor(Color.parseColor("#88006400"))
                        }
                        else {
                            mTxtPwdConfirm.text = "* 비밀번호가 일치하지 않습니다."
                            mTxtPwdConfirm.setTextColor(Color.parseColor("#88ff0000"))
                        }
                    }
                })
            }
        })


        mEtPwd.setOnFocusChangeListener(object : View.OnFocusChangeListener{
            override fun onFocusChange(view: View?, hasFocus: Boolean) {
                if (!hasFocus) {
                    if (!Pattern.matches("^(?=.*[A-Za-z])(?=.*[0-9]).{7,15}.\$", mEtPwd.text.toString())){
                        mTxtPwdCheck.text = "* 8~16자 숫자, 문자를 사용하세요."
                        mTxtPwdCheck.setTextColor(Color.parseColor("#88ff0000"))
                        isPwdValid = true
                    }
                }
                else{
                    mTxtPwdConfirm.text = ""
                    mTxtPwdCheck.text = ""
                    isPwdValid = false
                }
            }
        })

        mBtnDorm.setOnClickListener {
            var selectedDorm = dormitories[selectedItemIndex]
            MaterialAlertDialogBuilder(this).setTitle("기숙사 선택").setSingleChoiceItems(dormitories, selectedItemIndex) { _, which ->
                selectedItemIndex = which
                selectedDorm = dormitories[which]
            }.setPositiveButton("확인") { _, _ ->
                mTxtDorm.setText(selectedDorm)
            }.setNeutralButton("취소") { _, _ ->  }.show()
        }

        mBtnRegister.setOnClickListener(View.OnClickListener {
            val strEmail: String = mEtEmail.text.toString()
            val strPwd: String = mEtPwd.text.toString()
            val strName: String = mEtName.text.toString()
            val strPwdConfirm = mEtPwdConfirm.text.toString()
            val strDorm: String = mTxtDorm.text.toString()
            when {
                !Patterns.EMAIL_ADDRESS.matcher(strEmail).matches() -> {
                    Toast.makeText(this@RegisterActivity, "이메일을 입력해주세요", Toast.LENGTH_SHORT).show()
                }
                strPwd.isEmpty() -> {
                    Toast.makeText(this@RegisterActivity, "비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
                }
                strPwdConfirm.isEmpty() -> {
                    Toast.makeText(this@RegisterActivity, "비밀번호 확인란을 입력해주세요", Toast.LENGTH_SHORT).show()
                }
                strName.isEmpty() -> {
                    Toast.makeText(this@RegisterActivity, "닉네임을 입력해주세요", Toast.LENGTH_SHORT).show()
                }
                strDorm.isEmpty()-> {
                    Toast.makeText(this@RegisterActivity, "기숙사를 입력해주세요", Toast.LENGTH_SHORT).show()
                }
                strPwd != strPwdConfirm -> {
                    Toast.makeText(this@RegisterActivity, "비밀번호를 확인하세요", Toast.LENGTH_SHORT).show()
                }
                isPwdValid -> {
                    Toast.makeText(this@RegisterActivity, "비밀번호 형식을 확인하세요", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    mFirebaseAuth.createUserWithEmailAndPassword(strEmail, strPwd).addOnCompleteListener(this) {task: Task<AuthResult> ->
                        if (task.isSuccessful) {
                            val mFirebaseUser: FirebaseUser? = mFirebaseAuth.currentUser
                            val account = UserAccount()
                            account.emailId = mFirebaseUser?.email
                            account.idToken = mFirebaseUser?.uid
                            account.password = strPwd
                            account.nickname = strName
                            account.dorm = strDorm
                            account.nowChatting = false
                            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                                if (!task.isSuccessful) {
                                    Log.w("FCM token error", "Fetching FCM registration token failed", task.exception)
                                    return@OnCompleteListener
                                }

                                // Get new FCM registration token
                                val token = task.result.toString()
                                account.firebaseToken = token

                                mFirebaseUser?.uid?.let { it1 -> mDatabaseReference.child("UserAccount").child(it1).setValue(account) }

                                Toast.makeText(this@RegisterActivity, "회원가입에 성공하셨습니다", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                                startActivity(intent)
                                finish()
                            })

                        } else {
                            Toast.makeText(this@RegisterActivity, "회원가입에 실패하셨습니다", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })

    }


}


