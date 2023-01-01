package com.herika.musicplayer

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
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
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.herika.musicplayer.databinding.ActivityPlayMusicBinding
import com.herika.musicplayer.manager.formatDuration
import com.herika.musicplayer.model.SongItems
import com.herika.musicplayer.service.MusicService
import com.herika.musicplayer.utils.HelperConstant
import com.herika.musicplayer.viewmodel.MyViewModel
import com.herika.musicplayer.viewmodel.ViewModelFactory
import org.json.JSONObject


class PlayMusicActivity : AppCompatActivity(), ServiceConnection {

    companion object{
        const val TAG = "PlayMusicActivity"
        var isPlaying = false
        var musicService: MusicService? = null

        @SuppressLint("StaticFieldLeak")
        lateinit var databinding: ActivityPlayMusicBinding
    }

private lateinit var remoteConfig: FirebaseRemoteConfig
    private lateinit var runnable:Runnable
    private var handler: Handler = Handler()
    private var pause:Boolean = false



    //var mediaPlayer: MediaPlayer? = null


    lateinit var tvTitle:TextView
    lateinit var tvAuthor: TextView
    private lateinit var mViewModel: MyViewModel



    private val songList: MutableList<SongItems> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        databinding = ActivityPlayMusicBinding.inflate(layoutInflater)
        setContentView(databinding.root)
//        databinding = DataBindingUtil.setContentView(this, R.layout.activity_play_music)
//        setContentView(R.layout.activity_play_music)


        tvTitle = findViewById(R.id.tvSongTitle)
        tvAuthor = findViewById(R.id.tvSongAuthor)


        //receive intent from recyclerView Adapter
        val title = intent.extras?.get(HelperConstant.TITLE)?.toString()

        setUpViewModel()

        //for starting service
        val intent = Intent(this, MusicService::class.java)
        bindService(intent, this, BIND_AUTO_CREATE)
        startService(intent)

        fetchRemoteConfigSongListItem(title)

        //btnPlay = findViewById(R.id.btnPlay)
        //createMediaPlayer()
        databinding.btnPlay.setOnClickListener {
            if (isPlaying) pauseMusic()
            else playMusic()
        }

        //seekbar
        databinding.seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, progress: Int, isFromUser: Boolean) {
                if (isFromUser){
                    musicService?.mediaPlayer?.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })


    }

    private fun setUpViewModel() {
        val viewModelFactory = ViewModelFactory()
        mViewModel = ViewModelProvider(this, viewModelFactory).get(MyViewModel::class.java)
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

                         "Song3" -> {
                             if (ob.has(resources.getString(R.string.song3))) {
                                 val thirdSongUrl =
                                     ob.getJSONObject(resources.getString(R.string.song3))
                                         .getString(resources.getString(R.string.audiourl))
                                 Log.d(
                                     TAG,
                                     "onCreate: squareEndPoint:: $thirdSongUrl"
                                 )

                                 val thirdSongTitle =
                                     ob.getJSONObject(resources.getString(R.string.song3))
                                         .getString(resources.getString(R.string.title))
                                 Log.d(TAG, "fetchRemoteConfigSongListItem:: title: $thirdSongTitle")
                                 //tvTitle.text = secondSongTitle



                                 val thirdSongAuthor =
                                     ob.getJSONObject(resources.getString(R.string.song3))
                                         .getString(resources.getString(R.string.author))
                                 Log.d(
                                     TAG,
                                     "fetchRemoteConfigSongListItem:: author: $thirdSongAuthor"
                                 )

                                 val thirdSongStartTime =
                                     ob.getJSONObject(resources.getString(R.string.song3))
                                         .getString(resources.getString(R.string.startTime))
                                 Log.d(
                                     TAG,
                                     "fetchRemoteConfigSongListItem:: startTime: $thirdSongStartTime"
                                 )

                                 val thirdSongEndTime =
                                     ob.getJSONObject(resources.getString(R.string.song3))
                                         .getString(resources.getString(R.string.endTime))
                                 Log.d(
                                     TAG,
                                     "fetchRemoteConfigSongListItem:: endTime: $thirdSongEndTime"
                                 )
                                 songList.add(SongItems(thirdSongUrl,thirdSongTitle,thirdSongAuthor,thirdSongStartTime,thirdSongEndTime))
                                 setUpUI()
                             }
                         }

                         "Song4" -> {
                             if (ob.has(resources.getString(R.string.song4))) {
                                 val fourthSongUrl =
                                     ob.getJSONObject(resources.getString(R.string.song4))
                                         .getString(resources.getString(R.string.audiourl))
                                 Log.d(
                                     TAG,
                                     "onCreate: squareEndPoint:: $fourthSongUrl"
                                 )

                                 val fourthSongTitle =
                                     ob.getJSONObject(resources.getString(R.string.song4))
                                         .getString(resources.getString(R.string.title))
                                 Log.d(TAG, "fetchRemoteConfigSongListItem:: title: $fourthSongTitle")
                                 //tvTitle.text = secondSongTitle



                                 val fourthSongAuthor =
                                     ob.getJSONObject(resources.getString(R.string.song4))
                                         .getString(resources.getString(R.string.author))
                                 Log.d(
                                     TAG,
                                     "fetchRemoteConfigSongListItem:: author: $fourthSongAuthor"
                                 )

                                 val fourthSongStartTime =
                                     ob.getJSONObject(resources.getString(R.string.song4))
                                         .getString(resources.getString(R.string.startTime))
                                 Log.d(
                                     TAG,
                                     "fetchRemoteConfigSongListItem:: startTime: $fourthSongStartTime"
                                 )

                                 val fourthSongEndTime =
                                     ob.getJSONObject(resources.getString(R.string.song4))
                                         .getString(resources.getString(R.string.endTime))
                                 Log.d(
                                     TAG,
                                     "fetchRemoteConfigSongListItem:: endTime: $fourthSongEndTime"
                                 )
                                 songList.add(SongItems(fourthSongUrl,fourthSongTitle,fourthSongAuthor,fourthSongStartTime,fourthSongEndTime))
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

            HelperConstant.SET_SONG_URL = it.url
            HelperConstant.SET_SONG_AUTHOR = it.author

            mViewModel.SongsAuthorLiveData.postValue(it.author)
            intent.putExtra("AUTH", it.author)

            saveDataToSharedPreferences(it.url, it.author, it.title, it.startTime, it.endTime)
        }
    }

    private fun saveDataToSharedPreferences(
        url: String,
        author: String,
        title: String,
        startTime: String,
        endTime: String
    ) {

        val sharedPreferences = getSharedPreferences("SP_INFO",Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(HelperConstant.URL, url)
        editor.putString(HelperConstant.AUTHOR, author)
        editor.putString(HelperConstant.SONGTITLE, title)
        editor.putString(HelperConstant.STARTTIME, startTime)
        editor.putString(HelperConstant.ENDTIME, endTime)
        editor.apply()
    }


    private fun createMediaPlayer() {
        try {
            if(musicService?.mediaPlayer == null) musicService?.mediaPlayer = MediaPlayer()
            musicService?.mediaPlayer!!.reset()

            musicService?.mediaPlayer!!.setDataSource("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-7.mp3")

            musicService?.mediaPlayer!!.prepare()
            musicService?.mediaPlayer!!.start()

            isPlaying = true
            databinding.btnPlay.setBackgroundResource(R.drawable.img_pause)

            //seekbar setup
            Log.d(TAG, "createMediaPlayer:seekbar startTime ${formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())}")
            databinding.tvSongStartTime.text = formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
            Log.d(TAG, "createMediaPlayer:seekbar endTime ${formatDuration(musicService!!.mediaPlayer!!.duration.toLong())}")
            databinding.tvSongEndTime.text = formatDuration(musicService!!.mediaPlayer!!.duration.toLong())

            databinding.seekBar.progress = 0
            databinding.seekBar.max = musicService!!.mediaPlayer!!.duration

        }catch(e:Exception){
            Log.d("test", "createMediaPlayer: ${e.message}")
        }

    }

    private fun playMusic(){
        databinding.btnPlay.setBackgroundResource(R.drawable.img_pause)
        musicService?.showNotification(R.drawable.icon_pause)
        isPlaying = true
        musicService?.mediaPlayer?.start()
    }
    private fun pauseMusic(){
        databinding.btnPlay.setBackgroundResource(R.drawable.img_play)
        musicService?.showNotification(R.drawable.icon_play)
        isPlaying = false
        musicService?.mediaPlayer?.pause()
    }

    override fun onServiceConnected(p0: ComponentName?, service: IBinder?) {
        val binder = service as MusicService.MyBinder
        musicService = binder.currentService()
        createMediaPlayer()
        musicService!!.showNotification(R.drawable.icon_pause)
        musicService!!.setUpSeekBar()
    }

    override fun onServiceDisconnected(p0: ComponentName?) {
        musicService = null
    }

}