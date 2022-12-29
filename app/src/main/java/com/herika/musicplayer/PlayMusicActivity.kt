package com.herika.musicplayer

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.herika.musicplayer.model.SongItems
import com.herika.musicplayer.service.MusicService
import com.herika.musicplayer.utils.HelperConstant
import org.json.JSONObject


class PlayMusicActivity : AppCompatActivity(), ServiceConnection {

    companion object{
        const val TAG = "PlayMusicActivity"
    }

    private lateinit var remoteConfig: FirebaseRemoteConfig
    private lateinit var runnable:Runnable
    private var handler: Handler = Handler()
    private var pause:Boolean = false
    lateinit var btnPlay: Button
    lateinit var seekBar: SeekBar
    var isPlaying = false
    //var mediaPlayer: MediaPlayer? = null

    lateinit var tvTitle:TextView
    lateinit var tvAuthor: TextView

    var musicService: MusicService? = null
    private val songList: MutableList<SongItems> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_music)
        tvTitle = findViewById(R.id.tvSongTitle)
        tvAuthor = findViewById(R.id.tvSongAuthor)



        val title = intent.extras?.get(HelperConstant.TITLE)?.toString()
        Log.d(TAG, "onCreate: title ${title}")

        //for starting service
        val intent = Intent(this, MusicService::class.java)
        bindService(intent, this, BIND_AUTO_CREATE)
        startService(intent)

        fetchRemoteConfigSongListItem(title)

        btnPlay = findViewById(R.id.btnPlay)
        //createMediaPlayer()
        btnPlay.setOnClickListener {
            if (isPlaying) pauseMusic()
            else playMusic()
        }


    }

    private fun fetchRemoteConfigSongListItem(title: String?) {
        Log.d(TAG, "onCreate: title from remote config ${title}")
        remoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)

        remoteConfig.fetchAndActivate()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val updated = task.result
                    Log.d(MusicDetailActivity.TAG, "Config params updated: $updated")

                    val fetchJsonFromKey =
                        remoteConfig.getString(resources.getString(R.string.music_list))
                    val convertedObject = JSONObject(fetchJsonFromKey)
                    val arr = convertedObject.getJSONArray(resources.getString(R.string.type))

                    for (i in 0 until arr.length()) {
                        val ob = arr.getJSONObject(i)

                     when(title){
                         "Song1" -> {
                             if (ob.has(resources.getString(R.string.song1))) {
                                 val firstSongUrl =
                                     ob.getJSONObject(resources.getString(R.string.song1))
                                         .getString(resources.getString(R.string.audiourl))
                                 Log.d(
                                     TAG,
                                     "onCreate: squareEndPoint:: $firstSongUrl"
                                 )

                                 val firstSongTitle =
                                     ob.getJSONObject(resources.getString(R.string.song1))
                                         .getString(resources.getString(R.string.title))
                                 Log.d(TAG, "fetchRemoteConfigSongListItem:: title: $firstSongTitle")

                                 //tvTitle.text = firstSongTitle

                                 val firstSongAuthor =
                                     ob.getJSONObject(resources.getString(R.string.song1))
                                         .getString(resources.getString(R.string.author))
                                 Log.d(TAG, "fetchRemoteConfigSongListItem:: author: $firstSongAuthor")

                                 val firstSongStartTime =
                                     ob.getJSONObject(resources.getString(R.string.song1))
                                         .getString(resources.getString(R.string.startTime))
                                 Log.d(
                                     TAG,
                                     "fetchRemoteConfigSongListItem:: startTime: $firstSongStartTime"
                                 )

                                 val firstSongEndTime =
                                     ob.getJSONObject(resources.getString(R.string.song1))
                                         .getString(resources.getString(R.string.endTime))
                                 Log.d(
                                     TAG,
                                     "fetchRemoteConfigSongListItem:: endTime: $firstSongEndTime"
                                 )

                                 songList.add(SongItems(firstSongUrl,firstSongTitle,firstSongAuthor,firstSongStartTime,firstSongEndTime))
                                 setUpUI()
                             }

                         }

                         "Song2" -> {
                             if (ob.has(resources.getString(R.string.song2))) {
                                 val secondSongUrl =
                                     ob.getJSONObject(resources.getString(R.string.song2))
                                         .getString(resources.getString(R.string.audiourl))
                                 Log.d(
                                     TAG,
                                     "onCreate: squareEndPoint:: $secondSongUrl"
                                 )

                                 val secondSongTitle =
                                     ob.getJSONObject(resources.getString(R.string.song2))
                                         .getString(resources.getString(R.string.title))
                                 Log.d(TAG, "fetchRemoteConfigSongListItem:: title: $secondSongTitle")
                                 //tvTitle.text = secondSongTitle



                                 val secondSongAuthor =
                                     ob.getJSONObject(resources.getString(R.string.song2))
                                         .getString(resources.getString(R.string.author))
                                 Log.d(
                                     TAG,
                                     "fetchRemoteConfigSongListItem:: author: $secondSongAuthor"
                                 )

                                 val secondSongStartTime =
                                     ob.getJSONObject(resources.getString(R.string.song2))
                                         .getString(resources.getString(R.string.startTime))
                                 Log.d(
                                     TAG,
                                     "fetchRemoteConfigSongListItem:: startTime: $secondSongStartTime"
                                 )

                                 val secondSongEndTime =
                                     ob.getJSONObject(resources.getString(R.string.song2))
                                         .getString(resources.getString(R.string.endTime))
                                 Log.d(
                                     TAG,
                                     "fetchRemoteConfigSongListItem:: endTime: $secondSongEndTime"
                                 )
                                 songList.add(SongItems(secondSongUrl,secondSongTitle,secondSongAuthor,secondSongStartTime,secondSongEndTime))
                                 setUpUI()
                             }
                         }
                     }

                    }
                    Toast.makeText(
                        this, "Fetch and activate succeeded",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this, "Fetch failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

    }

    private fun setUpUI() {
        songList.map {
            Log.d(TAG, "setUpUI: title ${it.title}  author: ${it.author}")
            tvTitle.text = it.title
            tvAuthor.text = it.author
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