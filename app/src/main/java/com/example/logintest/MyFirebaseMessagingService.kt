package com.example.logintest

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_MULTIPLE_TASK
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

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)
        sendNotification(p0.data["title"], p0.data["body"]);
    }

    override fun onNewToken(token: String) {
        Log.d("token", "created new token: $token")
    }

    private fun sendNotification(title: String?, body: String?) {
        Log.d("noti", "reached")
        val intent = Intent(this, MessageActivity::class.java)
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
                .setSmallIcon(com.example.logintest.R.drawable.selector_bottom_navi_chat_icon_selected)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Since android Oreo notification channel is needed.

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