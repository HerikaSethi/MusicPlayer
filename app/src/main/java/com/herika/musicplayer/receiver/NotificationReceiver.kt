package com.herika.musicplayer.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.herika.musicplayer.ApplicationClass
import com.herika.musicplayer.PlayMusicActivity
import com.herika.musicplayer.R
import com.herika.musicplayer.service.MusicService
import kotlin.system.exitProcess

class NotificationReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        when(intent?.action){
            ApplicationClass.PLAY -> {
                if (PlayMusicActivity.isPlaying) {
                    pauseMusic()
                } else {
                    playMusic()
                }
            }
            ApplicationClass.EXIT -> {
                PlayMusicActivity.musicService?.stopForeground(true)
                PlayMusicActivity.musicService = null
                exitProcess(1)
            }
        }
    }

    private fun playMusic(){
        PlayMusicActivity.isPlaying = true
        PlayMusicActivity.musicService!!.mediaPlayer!!.start()
        PlayMusicActivity.musicService?.showNotification(R.drawable.icon_pause)

        //for btn synchronization in play music activity
        PlayMusicActivity.databinding.btnPlay.setBackgroundResource(R.drawable.img_pause)
    }

    private fun pauseMusic(){
        PlayMusicActivity.isPlaying = false
        PlayMusicActivity.musicService?.mediaPlayer?.pause()
        PlayMusicActivity.musicService?.showNotification(R.drawable.icon_play)

        //for btn synchronization in play music activity
        com.herika.musicplayer.PlayMusicActivity.databinding.btnPlay.setBackgroundResource(R.drawable.img_play)
    }
}