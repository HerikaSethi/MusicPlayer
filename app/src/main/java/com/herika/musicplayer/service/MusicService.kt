package com.herika.musicplayer.service

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModelProvider
import com.herika.musicplayer.ApplicationClass
import com.herika.musicplayer.MainActivity
import com.herika.musicplayer.PlayMusicActivity
import com.herika.musicplayer.R
import com.herika.musicplayer.receiver.NotificationReceiver
import com.herika.musicplayer.utils.HelperConstant
import com.herika.musicplayer.viewmodel.MyViewModel
import com.herika.musicplayer.viewmodel.ViewModelFactory

class MusicService: Service() {
    private var myBinder = MyBinder()
    var mediaPlayer: MediaPlayer? = null
    private lateinit var mediaSession: MediaSessionCompat

    private lateinit var mViewModel: MyViewModel

    override fun onBind(p0: Intent?): IBinder? {
        //initialize the media session object as soon as service is bind
        mediaSession = MediaSessionCompat(baseContext, "My Music")
        return myBinder
    }

    inner class MyBinder: Binder(){
        fun currentService(): MusicService {
            return  this@MusicService
        }
    }

    //for foreground service
    @SuppressLint("UnspecifiedImmutableFlag")
    fun showNotification(playPauseBtn: Int){

        val intent = Intent(baseContext, MainActivity::class.java)

        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val contentIntent = PendingIntent.getActivity(this, 0, intent, flag)

        val playIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(ApplicationClass.PLAY)
        val playPendingIntent = PendingIntent.getBroadcast(baseContext, 0, playIntent, flag)

        val exitIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(ApplicationClass.EXIT)
        val exitPendingIntent = PendingIntent.getBroadcast(baseContext, 0, exitIntent, flag)

        mViewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(application).create(MyViewModel::class.java)
        Log.d("testview", "showNotification:viewmodel  ${mViewModel.SongsAuthorLiveData.value.toString()}")



        /** Retrieve data from shared preferences*/
        val sharedPreferences = getSharedPreferences("SP_INFO", Context.MODE_PRIVATE)
        val name = sharedPreferences.getString(HelperConstant.AUTHOR,"")
        Log.d(PlayMusicActivity.TAG, "saveDataToSharedPreferences:::: ${name}")


        val notification = NotificationCompat.Builder(baseContext, ApplicationClass.CHANNEL_ID)
            .setContentIntent(contentIntent)
            .setContentTitle("$name")
            .setContentText("This is content text")
            .setSmallIcon(R.drawable.icon_music)
//            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.img_play))
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSession.sessionToken))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)  //only one notification for every music not a new notification for every music
            .addAction(R.drawable.icon_exit,"Exit", exitPendingIntent)
            .addAction(playPauseBtn,"Play",playPendingIntent)
            .build()

        startForeground(13, notification)


    }

}