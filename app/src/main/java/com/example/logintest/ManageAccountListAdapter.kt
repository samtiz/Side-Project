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
import com.google.firebase.database.*
import com.google.firebase.database.collection.LLRBNode
import com.google.firebase.database.ktx.getValue
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ManageAccountListAdapter(private val context: Context, private val username : String, private val postId: String): RecyclerView.Adapter<ManageAccountListAdapter.ViewHolder>() {

    private lateinit var mFirebaseAuth: FirebaseAuth
    private lateinit var mDatabaseReference: DatabaseReference

    private fun exitMasterPost(postId : String, uid : String, chatusers : HashMap<String, ChatModel.userStat>) {
        val time = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("hh:mm")
        val AMPM = if(SimpleDateFormat("a").format(Date(time)).toString() == "AM"){"오전"}else{"오후"}
        val curTime = AMPM + " " +dateFormat.format(Date(time)).toString()
        var nextMaster : String? = null
        val currentUsersId = ArrayList<String>()
        // Post에 접근하여 uid 제거하기
        mDatabaseReference.child("Post").child(postId).child("users").child(uid).removeValue().addOnSuccessListener {
            mDatabaseReference.child("Post").child(postId).child("users").addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    for (data in snapshot.children){
                        val key = data.key
                        currentUsersId.add(key!!)
                    }
                    if (currentUsersId.isEmpty()){//  다 나갔으면 채팅방 폭파
                        println("유저가 아무도 안남음!!!")
                        //post 제거
                        mDatabaseReference.child("Post").child(postId).removeValue()
                        //chatroom 제거
                        mDatabaseReference.child("chatrooms").child(postId).removeValue()
                        // 사진 파일 제거
                        FirebaseStorage.getInstance().reference.child("ChatImages").child(postId).delete()
                        return
                    }
                    var maxIndex = 1000
                    for (cuid in currentUsersId){
                        if(chatusers.get(cuid)?.index!! < maxIndex){
                            maxIndex = chatusers.get(cuid)?.index!!
                            nextMaster = cuid
                        }
                    }
                    mDatabaseReference.child("Post").child(postId.toString()).child("uid").setValue(nextMaster)
                    // 퇴장 알림
                    val exitAlarm1 = ChatModel.Comment("Admin", "${username}님(방장)이 퇴장하셨습니다.", curTime, false)
                    val exitAlarm2 = ChatModel.Comment("Admin", "방장이 ${chatusers.get(nextMaster!!)?.nickname}님으로 변경되었습니다.", curTime, false)
                    mDatabaseReference.child("chatrooms").child(postId.toString()).child("comments").push().setValue(exitAlarm1)
                    mDatabaseReference.child("chatrooms").child(postId.toString()).child("comments").push().setValue(exitAlarm2)
                }
            })
        }

    }

    private fun exitPost(uid: String) {
        val time = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("hh:mm")
        val AMPM = if(SimpleDateFormat("a").format(Date(time)).toString() == "AM"){"오전"}else{"오후"}
        val curTime = AMPM + " " +dateFormat.format(Date(time)).toString()
        // Post에 접근하여 uid 제거하기
        mDatabaseReference.child("Post").child(postId.toString()).child("users").child(uid.toString()).removeValue()
        // UserAccount에 접근하여 postId 제거하기
        mDatabaseReference.child("UserAccount").child(uid!!).child("postId").removeValue()

        // 퇴장 알림
        val exitAlarm = ChatModel.Comment("Admin", "${username}님이 퇴장하셨습니다.", curTime, false)
        mDatabaseReference.child("chatrooms").child(postId.toString()).child("comments").push().setValue(exitAlarm)

    }

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
                            val app = context.applicationContext as GlobalVariable
                            app.setUserLocation(selectedDorm)
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
                        mDatabaseReference.child("UserAccount").child(uid).child("postId").get().addOnSuccessListener {
                            if (it.value!=null) {
                                val postId = it.value.toString()
                                mDatabaseReference.child("chatrooms").child(postId).child("users").child(uid).child("nickname").setValue(et.text.toString())
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
                            val con = context as ManageAccountActivity
                            val txtName = con.findViewById<TextView>(R.id.txt_manage_username)
                            txtName.text = et.text.toString()
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
                    params.topMargin = context.resources.getDimensionPixelSize(R.dimen.dialog_margin)
                    params.bottomMargin = context.resources.getDimensionPixelSize(R.dimen.dialog_margin)
                    et.layoutParams = params
                    et.hint = "새 비밀번호를 입력하세요"
                    container.addView(et)
                    val builder: AlertDialog.Builder = AlertDialog.Builder(context)
                    builder.setTitle("비밀번호 변경")
                    builder.setView(container)
                    builder.setPositiveButton("확인") { _, _ ->
                        if (!Pattern.matches("^(?=.*[A-Za-z])(?=.*[0-9]).{8,16}.\$", et.text)){
                            Toast.makeText(context, "비밀번호는 8~16자 숫자, 문자를 사용하세요", Toast.LENGTH_SHORT).show()
                        }
                        else{
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
                    builder.setMessage("\nsamtiz@kaist.ac.kr\n위 주소로 메일주세요!\n")
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
                    builder.setMessage("\n1. 홈\n" +
                            "홈 화면에서 원하는 음식점 배달 모집 글이 있나 찾아보세요. " +
                            "배달 지역과 음식 카테고리를 설정하여 모집 글을 찾을 수 있으며 왼쪽 상단의 현재 위치 표시를 눌러 현재 위치를 변경할 수 있습니다. " +
                            "원하는 음식점이 있다면 오른쪽 상단의 검색을 활용해보세요\n" +
                            "\n2. 모집 글 작성\n" +
                            "원하는 음식점 모집 글이 존재하지 않는다면 직접 모집 글을 작성하여 보세요! " +
                            "하단의 모집하기 버튼을 눌러 모집 글 작성을 시작할 수 있습니다. " +
                            "단, 한 번에 하나의 모집 글만 작성할 수 있다는 점 명심해주세요.\n" +
                            "\n3. 채팅\n" +
                            "원하는 모집 글을 찾은 경우 홈 화면에서 모집글을 눌러 자세히 볼 수 있습니다. " +
                            "배달 수령 위치, 모집 완료 시간 등을 확인한 후, 배달 팟에 참여하고 싶다면 채팅방 바로가기 버튼을 눌러 게시물의 채팅방에 참여하세요. " +
                            "다른 사람들과 채팅으로 주문 시간, 주문 메뉴, 배달 수령 위치 등릉 세부적으로 조정하시면 됩니다. " +
                            "이 또한 한 번에 하나의 채팅방에만 참여할 수 있으며, 참여하고 있는 채팅방의 경우 하단의 채팅방 버튼을 통해 바로 들어가실 수 있습니다. (작성자의 경우 하단 버튼을 통해 자신이 쓴 모집글의 채팅방에 바로 들어가실 수 있습니다. " +
                            "배달이 완료되어 모두가 음식을 받았다면 채팅방을 나가시면 되고, 모집글 작성자는 채팅방을 터트려주시면 됩니다.") //TODO
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
                    MaterialAlertDialogBuilder(context).setMessage("로그아웃 하시겠습니까? 작성하신 모집글이나 참여중인 채팅방은 유지됩니다.")
                            .setPositiveButton("확인") { _, _ ->
                                mFirebaseAuth.signOut()
                                Toast.makeText(context, "로그아웃 되었습니다", Toast.LENGTH_SHORT).show()
                                MySharedPreferences.clearUser(context)
                                val intent = Intent(context, LoginActivity::class.java)
                                context.startActivity(intent)
                                val activity: ManageAccountActivity = context as ManageAccountActivity
                                activity.finishAffinity()
                            }.setNegativeButton("취소") { _, _ -> }.show()

                }
            }
            6 -> {
                holder.text.text = "회원 탈퇴"
                val img: Drawable = context.resources.getDrawable(R.drawable.ic_delete_forever)
                img.setBounds(0, 0, 120, 120)
                holder.text.setCompoundDrawables(img, null, null, null)
                holder.itemView.setOnClickListener {
                    //TODO 채팅방나가주고 게시물 지워줘야함, 정말로 탈퇴할거냐 dialog, 비번변경처럼 reauth
                    MaterialAlertDialogBuilder(context).setMessage("정말로 탈퇴하시겠습니까? 회원 정보 및 모집글, 참여중인 채팅방 정보가 모두 삭제됩니다.")
                            .setPositiveButton("확인") { _, _ ->
                                val mFirebaseUser: FirebaseUser? = mFirebaseAuth.currentUser
                                val strEmail = mFirebaseUser?.email.toString()
                                val uid = mFirebaseUser?.uid
                                lateinit var  strPwd: String
                                mFirebaseUser?.uid.let { it1 ->
                                    if (it1 != null) {
                                        mDatabaseReference.child("UserAccount").child(it1).child("password").get().addOnSuccessListener {
                                            strPwd = it.value.toString()
                                            val credential: AuthCredential = EmailAuthProvider.getCredential(strEmail, strPwd)
                                            mFirebaseAuth.currentUser?.reauthenticate(credential)?.addOnCompleteListener { task0 ->
                                                if (task0.isSuccessful) {
                                                    mFirebaseUser?.delete()?.addOnCompleteListener { task1 ->
                                                        if (task1.isSuccessful) {
                                                            if (postId != "null") { // 참가하는 post 존재한다면
                                                                mDatabaseReference.child("Post").child(postId).child("uid").get().addOnSuccessListener { // 방장 체크
                                                                    if (it.value == uid){ // 방장이면
                                                                        // chatusers 가져오기
                                                                        val chatusers = HashMap<String, ChatModel.userStat>()
                                                                        mDatabaseReference.child("chatrooms").child(postId.toString()).child("users").addListenerForSingleValueEvent(object : ValueEventListener{
                                                                            override fun onCancelled(error: DatabaseError) {
                                                                            }

                                                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                                                chatusers.clear()
                                                                                for (data in snapshot.children){
                                                                                    val key = data.key
                                                                                    val item = data.getValue<ChatModel.userStat>()
                                                                                    chatusers.put(key.toString(), item!!)
                                                                                }
                                                                                uid?.let { it2 -> exitMasterPost(postId, it2, chatusers) } // 방장 나가기
                                                                            }
                                                                        })
                                                                    }else{ // 방장이 아니면
                                                                        uid?.let { it2 -> exitPost(it2) } // 방장 아닌 사람 나가기
                                                                    }
                                                                }
                                                            }

                                                            mDatabaseReference.child("UserAccount").child(it1).removeValue().addOnCompleteListener { task2 ->
                                                                if (task2.isSuccessful) {
                                                                    MySharedPreferences.clearUser(context)
                                                                    Toast.makeText(context, "성공적으로 계정을 탈퇴하였습니다.", Toast.LENGTH_SHORT).show()
                                                                    val intent = Intent(context, LoginActivity::class.java)
                                                                    context.startActivity(intent)
                                                                    val activity: ManageAccountActivity = context as ManageAccountActivity
                                                                    activity.finishAffinity()
                                                                }
                                                                else {
                                                                    Toast.makeText(context, "firebase useraccount removeValue failed", Toast.LENGTH_SHORT).show()
                                                                    mFirebaseAuth.createUserWithEmailAndPassword(strEmail, strPwd).addOnCompleteListener{ task3 ->
                                                                        if (task3.isSuccessful) { }
                                                                        else {
                                                                            Toast.makeText(context, "Do not reach here. Plz contact developer.", Toast.LENGTH_SHORT).show()
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                        else {
                                                            Toast.makeText(context, "계정 삭제에 실패하였습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                }
                                                else {
                                                    Toast.makeText(context, "firebase reauthenticate failed", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }.addOnFailureListener { Toast.makeText(context, "get user passwd failed", Toast.LENGTH_SHORT).show() }
                                    }
                                    else{
                                        Toast.makeText(context, "사용자 데이터를 지우는데 실패하셨습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }.setNegativeButton("취소") {_, _ -> }.show()
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return 7
    }

}

