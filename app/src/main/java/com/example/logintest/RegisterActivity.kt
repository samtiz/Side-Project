package com.example.logintest

import android.content.Intent
import android.graphics.Color
import android.nfc.Tag
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.gms.tasks.Task
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : BasicActivity() {
    private lateinit var mFirebaseAuth: FirebaseAuth            // Firebase certification
    private lateinit var mDatabaseReference: DatabaseReference  // Firebase real time database
    private lateinit var mEtEmail: EditText                     // email text
    private lateinit var mEtPwd: EditText                       // pwd text
    private lateinit var mEtPwdConfirm: EditText                // pwd text
    private lateinit var mTxtPwdConfirm: TextView
    private lateinit var mEtName: EditText                      // NickName text
    private lateinit var mBtnRegister: Button                   // Register button
    private lateinit var mTxtDorm: EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        mFirebaseAuth = FirebaseAuth.getInstance()
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("logintest")

        mEtEmail = findViewById(R.id.edit_email)
        mEtPwd = findViewById(R.id.edit_pwd)
        mEtName = findViewById(R.id.edit_name)
        mEtPwdConfirm = findViewById(R.id.edit_pwdconfirm)
        mTxtPwdConfirm = findViewById(R.id.txt_pwdconfirm)
        mBtnRegister = findViewById((R.id.btn_register))
        mTxtDorm = findViewById(R.id.txt_dorm)

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

        mBtnRegister.setOnClickListener(View.OnClickListener {
            val strEmail: String = mEtEmail.text.toString()
            val strPwd: String = mEtPwd.text.toString()
            val strName: String = mEtName.text.toString()
            val strPwdConfirm = mEtPwdConfirm.text.toString()
            val strDorm: String = mTxtDorm.text.toString()
            if (strPwd != strPwdConfirm) {
                Toast.makeText(this@RegisterActivity, "비밀번호를 확인하세요", Toast.LENGTH_SHORT).show()
            }
            else {
                mFirebaseAuth.createUserWithEmailAndPassword(strEmail, strPwd).addOnCompleteListener(this) {task: Task<AuthResult> ->
                    if (task.isSuccessful) {
                        val mFirebaseUser: FirebaseUser? = mFirebaseAuth.currentUser
                        val account = UserAccount()
                        account.emailId = mFirebaseUser?.email
                        account.idToken = mFirebaseUser?.uid
                        account.password = strPwd
                        account.nickname = strName
                        account.dorm = strDorm

                        mFirebaseUser?.uid?.let { it1 -> mDatabaseReference.child("UserAccount").child(it1).setValue(account) }

                        Toast.makeText(this@RegisterActivity, "회원가입에 성공하셨습니다", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    else {
                        Toast.makeText(this@RegisterActivity, "회원가입에 실패하셨습니다", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })

    }

    var selectedItemIndex = 0
    fun showCofirmationDialog(view: View) {
        val dormitories = arrayOf("세종관", "사랑관", "소망관", "성실관", "진리관", "아름관", "신뢰관", "지혜관", "갈릴레이관",
                "여울/나들관", "다솜/희망관", "원내아파트", "나래/미르관", "나눔관", "문지관", "화암관")
        var selectedDorm = dormitories[selectedItemIndex]
        MaterialAlertDialogBuilder(this).setTitle("기숙사 선택").setSingleChoiceItems(dormitories, selectedItemIndex) { _, which ->
            selectedItemIndex = which
            selectedDorm = dormitories[which]
        }.setPositiveButton("확인") { _, _ ->
            mTxtDorm.setText(selectedDorm)
        }.setNeutralButton("취소") { _, _ ->  }.show()

    }


}


