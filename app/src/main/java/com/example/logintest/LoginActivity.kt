package com.example.logintest

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : BasicActivity() {

    private lateinit var mFirebaseAuth: FirebaseAuth            // Firebase certification
    private lateinit var mDatabaseReference: DatabaseReference  // Firebase real time database
    private lateinit var mEtEmail: EditText                     // email text for sign-in
    private lateinit var mEtPwd: EditText                       // pwd text for sign-in

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mFirebaseAuth = FirebaseAuth.getInstance()
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("logintest")

        mEtEmail = findViewById(R.id.edit_email)
        mEtPwd = findViewById(R.id.edit_pwd)

        if(MySharedPreferences.getUserEmail(this).isNullOrBlank()
            || MySharedPreferences.getUserPass(this).isNullOrBlank()) {
            login(mEtEmail, mEtPwd)
        }
        else{
            Toast.makeText(this, "${MySharedPreferences.getUserEmail(this)}로 자동 로그인 되었습니다.", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        val btnRegister: Button = findViewById(R.id.btn_register)
        btnRegister.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)

        }
    }
    fun login(mEtEmail: EditText, mEtPwd: EditText) {

        val btnLogin: Button = findViewById(R.id.btn_login)

        btnLogin.setOnClickListener {
            val strEmail: String = this.mEtEmail.text.toString()
            val strPwd: String = this.mEtPwd.text.toString()

            mFirebaseAuth.signInWithEmailAndPassword(strEmail, strPwd).addOnCompleteListener(this@LoginActivity) {task: Task<AuthResult> ->
                if (task.isSuccessful) {
                    Toast.makeText(this@LoginActivity, "로그인에 성공하셨습니다", Toast.LENGTH_SHORT).show()
                    MySharedPreferences.setUserEmail(this, strEmail)
                    MySharedPreferences.setUserPass(this, strPwd)
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                else {
                    Toast.makeText(this@LoginActivity, "로그인에 실패하셨습니다", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}