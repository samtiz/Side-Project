package com.example.logintest

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.collection.LLRBNode

class ManageAccountListAdapter(private val context: Context): RecyclerView.Adapter<ManageAccountListAdapter.ViewHolder>() {

    private lateinit var mFirebaseAuth: FirebaseAuth
    private lateinit var mDatabaseReference: DatabaseReference

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val text: TextView = itemView.findViewById(R.id.txt_item_manage_account)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ManageAccountListAdapter.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_manage_account, parent, false)
        return ViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("UseCompatLoadingForDrawables", "ResourceAsColor")
    override fun onBindViewHolder(holder: ManageAccountListAdapter.ViewHolder, position: Int) {
        mFirebaseAuth = FirebaseAuth.getInstance()
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("logintest")
        when (position) {
            0 -> {
                holder.text.text = "기숙사 변경"
                val tintColor: Int = ContextCompat.getColor(context, android.R.color.black)
                val img: Drawable = context.resources.getDrawable(R.drawable.ic_my_location)
                img.setBounds(0, 0, 120, 120)
                holder.text.setCompoundDrawables(img, null, null, null)
                val dormitories = arrayOf("세종관", "사랑관", "소망관", "성실관", "진리관", "아름관", "신뢰관", "지혜관", "갈릴레이관",
                        "여울/나들관", "다솜/희망관", "원내아파트", "나래/미르관", "나눔관", "문지관", "화암관")
                val app = context.applicationContext as GlobalVariable
                var selectedItemIndex: Int? = null
                if (dormitories.contains(app.getUserLocation())) {
                    selectedItemIndex = dormitories.indexOf(app.getUserLocation())
                } else {
                    mDatabaseReference.child("UserAccount").child(mFirebaseAuth.currentUser?.uid!!).child("dorm").get().addOnSuccessListener {
                        val d = it.value as String
                        selectedItemIndex = dormitories.indexOf(d)
                    }
                }
                holder.itemView.setOnClickListener {
                    var selectedDorm = dormitories[selectedItemIndex!!]
                    MaterialAlertDialogBuilder(context).setTitle("기본 기숙사 변경").setSingleChoiceItems(dormitories, selectedItemIndex!!) { _, which ->
                        selectedItemIndex = which
                        selectedDorm = dormitories[which]
                    }.setPositiveButton("확인") { _, _ ->
                        mDatabaseReference.child("UserAccount").child(mFirebaseAuth.currentUser?.uid!!).child("dorm").setValue(selectedDorm).addOnSuccessListener {
                            Toast.makeText(context, "기본 기숙사를 ${selectedDorm}으로 변경하였습니다.", Toast.LENGTH_SHORT).show()
                        }.addOnFailureListener { Toast.makeText(context, "기숙사 변경에 실패하였습니다. 다시 시도하여주세요.", Toast.LENGTH_SHORT).show() }
                    }.setNeutralButton("취소") { _, _ ->  }.show()
                }
            }
            1 -> {
                holder.text.text = "닉네임 변경"
                val img: Drawable = context.resources.getDrawable(R.drawable.ic_drive_file_rename_outline)
                img.setBounds(0, 0, 120, 120)
                holder.text.setCompoundDrawables(img, null, null, null)
                holder.itemView.setOnClickListener {
                    val et: EditText = EditText(context)
                    et.setSingleLine()
                    val container: FrameLayout = FrameLayout(context)
                    val params: FrameLayout.LayoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    params.leftMargin = context.resources.getDimensionPixelSize(R.dimen.dialog_margin)
                    params.rightMargin = context.resources.getDimensionPixelSize(R.dimen.dialog_margin)
                    et.layoutParams = params
                    et.hint = "변경할 닉네임을 입력하세요"
                    container.addView(et)
                    val builder: AlertDialog.Builder = AlertDialog.Builder(context)
                    builder.setTitle("닉네임 변경")
                    builder.setView(container)
                    builder.setPositiveButton("확인") { _, _ ->
                        val uid = mFirebaseAuth.currentUser?.uid!!
                        var isSuccess: Boolean = true
                        mDatabaseReference.child("UserAccount").child(uid).child("nickname").setValue(et.text.toString()).addOnFailureListener {
                            Toast.makeText(context, "UserAccount nickname setValue failed", Toast.LENGTH_SHORT).show()
                            isSuccess = false
                        }
                        mDatabaseReference.child("UserAccount").child(uid).get().addOnSuccessListener {
                            if (it.hasChild("postId")) {
                                val userAccount = it.value as UserAccount
                                val postId = userAccount.postId as String
                                mDatabaseReference.child("Post").child(postId).child("users").child(uid).setValue(et.text.toString()).addOnFailureListener {
                                    Toast.makeText(context, "Post nickname setValue failed", Toast.LENGTH_SHORT).show()
                                    isSuccess = false
                                }
                            }
                        }.addOnFailureListener {
                            Toast.makeText(context, "postId of userAccount get failed", Toast.LENGTH_SHORT).show()
                            isSuccess = false
                        }
                        if (isSuccess) {
                            Toast.makeText(context, "닉네임이 ${et.text}으로 변경되었습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }.setNegativeButton("취소") { _, _ -> }.show()
                }
            }
            2 -> {
                holder.text.text = "비밀번호 변경"
                val img: Drawable = context.resources.getDrawable(R.drawable.ic_vpn_key)
                img.setBounds(0, 0, 120, 120)
                holder.text.setCompoundDrawables(img, null, null, null)
                holder.itemView.setOnClickListener {
                    val et: EditText = EditText(context)
                    et.setSingleLine()
                    val container: FrameLayout = FrameLayout(context)
                    val params: FrameLayout.LayoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    params.leftMargin = context.resources.getDimensionPixelSize(R.dimen.dialog_margin)
                    params.rightMargin = context.resources.getDimensionPixelSize(R.dimen.dialog_margin)
                    et.layoutParams = params
                    et.hint = "새 비밀번호를 입력하세요"
                    container.addView(et)
                    val builder: AlertDialog.Builder = AlertDialog.Builder(context)
                    builder.setTitle("비밀번호 변경")
                    builder.setView(container)
                    builder.setPositiveButton("확인") { _, _ ->
                        mDatabaseReference.child("UserAccount").child(mFirebaseAuth.currentUser?.uid!!).child("password").get().addOnSuccessListener {
                            val pw = it.value as String
                            val credential: AuthCredential = EmailAuthProvider.getCredential(mFirebaseAuth.currentUser?.email.toString(), pw)
                            mFirebaseAuth.currentUser?.reauthenticate(credential)?.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    mFirebaseAuth.currentUser!!.updatePassword(et.text.toString()).addOnCompleteListener { task2 ->
                                        if (task2.isSuccessful) {
                                            Toast.makeText(context, "비밀번호가 ${et.text}으로 변경되었습니다.", Toast.LENGTH_LONG).show()
                                            mDatabaseReference.child("UserAccount").child(mFirebaseAuth.currentUser?.uid!!).child("password").setValue(et.text.toString())
                                        }
                                        else {
                                            Toast.makeText(context, task2.exception.toString(), Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                                else {
                                    Toast.makeText(context, task.exception.toString(), Toast.LENGTH_LONG).show()
                                }
                            }
                        }


                    }
                    builder.setNegativeButton("취소") { _, _ -> }
                    builder.show()
                }
            }
            3 -> {
                holder.text.text = "개발자에게 문의하기"
                val img: Drawable = context.resources.getDrawable(R.drawable.ic_campaign)
                img.setBounds(0, 0, 120, 120)
                holder.text.setCompoundDrawables(img, null, null, null)
                holder.itemView.setOnClickListener {
                    val builder: AlertDialog.Builder = AlertDialog.Builder(context)
                    builder.setTitle("피드백 무조건 환영 >_<")
                    builder.setMessage("samtiz@kaist.ac.kr\n위 주소로 메일주세요!")
                    builder.setPositiveButton("확인") { _, _ -> }
                    builder.show()
                }
            }
            4 -> {
                holder.text.text = "앱 사용 가이드"
                val img: Drawable = context.resources.getDrawable(R.drawable.ic_library_books)
                img.setBounds(0, 0, 120, 120)
                holder.text.setCompoundDrawables(img, null, null, null)
                holder.itemView.setOnClickListener {
                    val builder: AlertDialog.Builder = AlertDialog.Builder(context)
                    builder.setTitle("사용 가이드")
                    builder.setMessage("1. 어쩌구 저쩌구") //TODO
                    builder.setPositiveButton("확인") { _, _ -> }
                    builder.show()
                }
            }
            5 -> {
                holder.text.text = "로그아웃"
                val img: Drawable = context.resources.getDrawable(R.drawable.ic_logout)
                img.setBounds(0, 0, 120, 120)
                holder.text.setCompoundDrawables(img, null, null, null)
                holder.itemView.setOnClickListener {
                    //TODO logout 할거냐 dialog
                    mFirebaseAuth.signOut()
                    Toast.makeText(context, "로그아웃 되었습니다", Toast.LENGTH_SHORT).show()
                    MySharedPreferences.clearUser(context)
                    val intent = Intent(context, LoginActivity::class.java)
                    context.startActivity(intent)
                    val activity: ManageAccountActivity = context as ManageAccountActivity
                    activity.finishAffinity()
                }
            }
            6 -> {
                holder.text.text = "회원 탈퇴"
                val img: Drawable = context.resources.getDrawable(R.drawable.ic_delete_forever)
                img.setBounds(0, 0, 120, 120)
                holder.text.setCompoundDrawables(img, null, null, null)
                holder.itemView.setOnClickListener {
                    //TODO 채팅방나가주고 게시물 지워줘야함, 정말로 탈퇴할거냐 dialog, 비번변경처럼 reauth

                    val mFirebaseUser: FirebaseUser? = mFirebaseAuth.currentUser
                    lateinit var  strPwd: String
                    mFirebaseUser?.uid.let { it1 ->
                        if (it1 != null) {
                            strPwd = mDatabaseReference.child("UserAccount").child(it1).child("password").toString()
                            mDatabaseReference.child("UserAccount").child(it1).removeValue()
                            mFirebaseUser?.delete()?.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(context, "성공적으로 계정을 탈퇴하였습니다.", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(context, LoginActivity::class.java)
                                    context.startActivity(intent)
                                    val activity: ManageAccountActivity = context as ManageAccountActivity
                                    activity.finishAffinity()
                                }
                                else {
                                    Toast.makeText(context, "계정 삭제에 실패하였습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                                    val account = UserAccount()
                                    account.emailId = mFirebaseUser.email
                                    account.idToken = mFirebaseUser.uid
                                    account.password = strPwd

                                    mFirebaseUser.uid.let { it2 -> mDatabaseReference.child("UserAccount").child(it2).setValue(account) }
                                }
                            }

                        }
                        else{
                            Toast.makeText(context, "사용자 데이터를 지우는데 실패하셨습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return 7
    }

}

