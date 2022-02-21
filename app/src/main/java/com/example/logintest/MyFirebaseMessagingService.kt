package com.example.logintest

import android.R
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_MULTIPLE_TASK
import android.graphics.drawable.Drawable
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class MyFirebaseMessagingService: FirebaseMessagingService() {
    private val TAG = "MyFirebaseMsgService"
    private val mFirebaseAuth = FirebaseAuth.getInstance()
    private val mDatabaseReference = FirebaseDatabase.getInstance().getReference("logintest")

    private var icon : Drawable? = null



    override fun onMessageReceived(p0: RemoteMessage) {

//        if (android.os.Build.VERSION.SDK_INT >= 28) {   // Android P
//            icon = R.drawable.ic_white_noti;
//        }

        super.onMessageReceived(p0)
        if (p0.data["flag"] == "chat") {
            sendChatNotification(p0.data["title"], p0.data["body"])
        }
        else if (p0.data["flag"] == "deletePost") {
            sendDeletePostNotification(p0.data["title"], p0.data["body"])
        }
        else {
            sendCommentNotification(p0.data["title"], p0.data["body"], p0.data["postId"])
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "created new token: $token")
    }

    @SuppressLint("UnspecifiedImmutableFlag", "ObsoleteSdkInt")
    private fun sendDeletePostNotification(title: String?, body: String?) {
        if (mFirebaseAuth.currentUser?.uid != null) {
            val pendingIntent = PendingIntent.getActivity(
                this,0,
                Intent(this, MainActivity::class.java).also {
                    it.action = Intent.ACTION_MAIN
                    it.addCategory(Intent.CATEGORY_LAUNCHER)
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }, PendingIntent.FLAG_UPDATE_CURRENT
            )

            val channelId = "delete_post_notification_channel"
            val defaultSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(com.example.logintest.R.drawable.ic_stat_name)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Since android Oreo notification channel is needed.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "delete post notification channel",
                    NotificationManager.IMPORTANCE_HIGH
                )
                notificationManager.createNotificationChannel(channel)
            }

            notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun sendCommentNotification(title: String?, body: String?, postId: String?) {
        if (mFirebaseAuth.currentUser?.uid != null) {
            val intent = Intent(this, PostDetailActivity::class.java)
            intent.putExtra("postId", postId)
            intent.putExtra("uid", mFirebaseAuth.currentUser?.uid!!)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

            val pendingIntent = TaskStackBuilder.create(this).run {
                // Add the intent, which inflates the back stack
                addNextIntentWithParentStack(intent)
                // Get the PendingIntent containing the entire back stack
                getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
            }

            val channelId = "comment_notification_channel"
            val defaultSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(com.example.logintest.R.drawable.ic_stat_name)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Since android Oreo notification channel is needed.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "comment notification channel",
                    NotificationManager.IMPORTANCE_HIGH
                )
                notificationManager.createNotificationChannel(channel)
            }

            notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun sendChatNotification(title: String?, body: String?) {
        val intent = Intent(this, MessageActivity::class.java)
        if (mFirebaseAuth.currentUser?.uid != null) {
            mDatabaseReference.child("UserAccount").child(mFirebaseAuth.currentUser?.uid!!).child("postId").get().addOnSuccessListener {
                intent.putExtra("postId", it.value.toString())
                // intent.action = Intent.ACTION_MAIN
                // intent.addCategory(Intent.CATEGORY_LAUNCHER)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                // intent.addFlags(Intent.FLAG_ACTIVITY_S)


                val pendingIntent = TaskStackBuilder.create(this).run {
                    // Add the intent, which inflates the back stack
                    addNextIntentWithParentStack(intent)
                    // Get the PendingIntent containing the entire back stack
                    getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
                }

                val channelId = "chat_notification_channel"
                val defaultSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(com.example.logintest.R.drawable.ic_stat_name)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent)

                val notificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                // Since android Oreo notification channel is needed.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel(
                        channelId,
                        "chatting notification channel",
                        NotificationManager.IMPORTANCE_HIGH
                    )
                    notificationManager.createNotificationChannel(channel)
                }

                notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
            }
        }
    }

}