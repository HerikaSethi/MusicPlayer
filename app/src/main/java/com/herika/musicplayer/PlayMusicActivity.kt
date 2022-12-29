package com.herika.musicplayer

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.widget.Button
import android.widget.SeekBar
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.herika.musicplayer.service.MusicService

class PlayMusicActivity : AppCompatActivity(), ServiceConnection {

    private lateinit var remoteConfig: FirebaseRemoteConfig
    private lateinit var runnable:Runnable
    private var handler: Handler = Handler()
    private var pause:Boolean = false
    lateinit var btnPlay: Button
    lateinit var seekBar: SeekBar
    var isPlaying = false
    //var mediaPlayer: MediaPlayer? = null

    var musicService: MusicService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_music)

        //for starting service
        val intent = Intent(this, MusicService::class.java)
        bindService(intent, this, BIND_AUTO_CREATE)
        startService(intent)

        btnPlay = findViewById(R.id.btnPlay)
        //createMediaPlayer()
        btnPlay.setOnClickListener {
            if (isPlaying) pauseMusic()
            else playMusic()
        }


    }

    private fun createMediaPlayer() {
        try {
            if(musicService?.mediaPlayer == null) musicService?.mediaPlayer = MediaPlayer()
            musicService?.mediaPlayer!!.reset()
            musicService?.mediaPlayer!!.setDataSource("https://www.learningcontainer.com/wp-content/uploads/2020/02/Kalimba.mp3")

            musicService?.mediaPlayer!!.prepare()
            musicService?.mediaPlayer!!.start()
            isPlaying = true
            btnPlay.setBackgroundResource(R.drawable.img_pause)
        }catch(e:Exception){
            Log.d("test", "createMediaPlayer: ${e.message}")
        }

    }

    private fun playMusic(){
        btnPlay.setBackgroundResource(R.drawable.img_pause)
        isPlaying = true
        musicService?.mediaPlayer?.start()
    }
    private fun pauseMusic(){
        btnPlay.setBackgroundResource(R.drawable.img_play)
        isPlaying = false
        musicService?.mediaPlayer?.pause()
    }

    override fun onServiceConnected(p0: ComponentName?, service: IBinder?) {
        val binder = service as MusicService.MyBinder
        musicService = binder.currentService()
        createMediaPlayer()
        musicService!!.showNotification()
    }

    override fun onServiceDisconnected(p0: ComponentName?) {
        musicService = null
    }
}