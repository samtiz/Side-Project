package com.example.logintest

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ManageAccountActivity : BasicActivity() {

        private lateinit var mFirebaseAuth: FirebaseAuth
        private lateinit var mDatabaseReference: DatabaseReference

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_manageaccount)

            mFirebaseAuth = FirebaseAuth.getInstance()
            mDatabaseReference = FirebaseDatabase.getInstance().getReference("logintest")

            val btnLogout: Button = findViewById(R.id.btn_logout)
            btnLogout.setOnClickListener{
                mFirebaseAuth.signOut()
                Toast.makeText(this@ManageAccountActivity, "로그아웃 되었습니다", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@ManageAccountActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }

            val btnSecession: Button = findViewById(R.id.btn_secession)
            btnSecession.setOnClickListener {
                val mFirebaseUser: FirebaseUser? = mFirebaseAuth.currentUser
                lateinit var  strPwd: String
                mFirebaseUser?.uid.let { it1 ->
                    if (it1 != null) {
                        strPwd = mDatabaseReference.child("UserAccount").child(it1).child("password").toString()
                        mDatabaseReference.child("UserAccount").child(it1).removeValue()


                        mFirebaseUser?.delete()?.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this@ManageAccountActivity, "성공적으로 계정을 삭제하였습니다.", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this@ManageAccountActivity, LoginActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            else {
                                Toast.makeText(this@ManageAccountActivity, "계정 삭제에 실패하였습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                                val account = UserAccount()
                                account.emailId = mFirebaseUser?.email
                                account.idToken = mFirebaseUser?.uid
                                account.password = strPwd

                                mFirebaseUser?.uid?.let { it2 -> mDatabaseReference.child("UserAccount").child(it2).setValue(account) }
                            }
                        }

                    }
                    else{
                        Toast.makeText(this@ManageAccountActivity, "사용자 데이터를 지우는데 실패하셨습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                    }
                }



            }

        }

}