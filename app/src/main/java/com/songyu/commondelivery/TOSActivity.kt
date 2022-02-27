package com.songyu.commondelivery

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.widget.AppCompatCheckBox
import kotlinx.android.synthetic.main.activity_tosactivity.*

class TOSActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tosactivity)

        val btnRegister: Button = findViewById(R.id.btn_go_to_register)

        var termAgree1: Boolean = false
        var termAgree2: Boolean = false

        val btnTerm1: AppCompatCheckBox = findViewById(R.id.join_check_1)
        val btnTerm2: AppCompatCheckBox = findViewById(R.id.join_check_2)
        val btnTermAll: AppCompatCheckBox = findViewById(R.id.join_check_3)

        btnTerm1.setOnCheckedChangeListener{_, isChecked->
            termAgree1 = isChecked
            if(!isChecked){
                btnTermAll.isChecked = false

            }
        }

        btnTerm2.setOnCheckedChangeListener{ _, isChecked->
            termAgree2 = isChecked
            if(!isChecked){
                btnTermAll.isChecked = false
            }
        }

        btnTermAll.setOnCheckedChangeListener{ _, isChecked->
            if(isChecked){
                btnTerm1.isChecked = true
                btnTerm2.isChecked = true

            }
            else{
                if(btnTerm1.isChecked && btnTerm2.isChecked){
                    btnTerm1.isChecked = false
                    btnTerm2.isChecked = false
                }
            }
        }

        btnRegister.setOnClickListener(View.OnClickListener {
            if(termAgree1 && termAgree2){
                val intent = Intent(this@TOSActivity, RegisterActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.none, R.anim.none)
            }
            else{
                Toast.makeText(applicationContext, "약관에 모두 동의해주세요", Toast.LENGTH_SHORT).show()
            }
        })
    }
}