package com.herika.musicplayer

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaPlayer
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.herika.musicplayer.databinding.ActivityPlayMusicBinding
import com.herika.musicplayer.manager.formatDuration
import com.herika.musicplayer.service.MusicService
import com.herika.musicplayer.utils.HelperConstant
import com.herika.musicplayer.viewmodel.MyViewModel
import com.herika.musicplayer.viewmodel.ViewModelFactory


class PlayMusicActivity : AppCompatActivity(), ServiceConnection {

    companion object{
        const val TAG = "PlayMusicActivity"
        var isPlaying = false
        var musicService: MusicService? = null

        @SuppressLint("StaticFieldLeak")
        lateinit var databinding: ActivityPlayMusicBinding
    }


    private lateinit var mViewModel: MyViewModel
    //var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        databinding = ActivityPlayMusicBinding.inflate(layoutInflater)
        setContentView(databinding.root)

        //receive intent from recyclerView Adapter
        val title = intent.extras?.get(HelperConstant.TITLE)?.toString()

        setUpViewModel()
        subscribeToLiveData()

        //for starting service
        val intent = Intent(this, MusicService::class.java)
        bindService(intent, this, BIND_AUTO_CREATE)
        startService(intent)

       // fetchRemoteConfigSongListItem(title)

        mViewModel.fetchDataFromFirebase(title)

        //createMediaPlayer()
        databinding.btnPlay.setOnClickListener {
            if (isPlaying) pauseMusic()
            else playMusic()
        }

        /** Seekbar onChangeListener */
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

    private fun subscribeToLiveData() {
        //Show or hide Progressbar
        mViewModel.showLoading.observe(this, Observer {
            if (!it){
                databinding.lConstraint.visibility = View.VISIBLE
                databinding.progressbar.visibility = View.INVISIBLE
            }else{
                databinding.lConstraint.visibility = View.INVISIBLE
                databinding.progressbar.visibility = View.VISIBLE
            }
        })

        mViewModel.saveDataToSharePreferenceResponse.observe(this, Observer {
            Log.d(TAG, "subscribeToLiveData:url ${it.url}, author${it.author}, title${it.title}")
            databinding.tvSongTitle.text = it.title
            databinding.tvSongAuthor.text = it.author

            saveDataToSharedPreferences(it.url,it.author,it.title,it.startTime,it.endTime)
        })
    }


    private fun setUpViewModel() {
        val viewModelFactory = ViewModelFactory()
        mViewModel = ViewModelProvider(this, viewModelFactory).get(MyViewModel::class.java)
    }


//    private fun setUpUI() {
//        songList.map {
//                //mViewModel.SongsAuthorLiveData.postValue(it.author)
//            intent.putExtra("AUTH", it.author)
//
//            saveDataToSharedPreferences(it.url, it.author, it.title, it.startTime, it.endTime)
//        }
//    }

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

            /** Seekbar setUp */
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