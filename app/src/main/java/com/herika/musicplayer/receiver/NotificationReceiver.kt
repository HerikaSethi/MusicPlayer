package com.herika.musicplayer.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.herika.musicplayer.ApplicationClass

class NotificationReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when(intent?.action){
            ApplicationClass.PLAY -> Toast.makeText(context, "Play btn clicked from notification", Toast.LENGTH_SHORT).show()
        }
    }
}