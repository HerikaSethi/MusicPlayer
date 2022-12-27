package com.herika.musicplayer

import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.herika.musicplayer.utils.HelperConstant
import org.json.JSONObject


class MusicDetailActivity : AppCompatActivity() {

    companion object{
        const val TAG = "MusicDetailActivity"
    }

    private lateinit var remoteConfig: FirebaseRemoteConfig
    private lateinit var runnable:Runnable
    private var handler: Handler = Handler()
    private var pause:Boolean = false
    lateinit var btnPlay: Button
    lateinit var btnPause: Button
    lateinit var seekBar: SeekBar
    var mediaPlayer: MediaPlayer = MediaPlayer()
    var song: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music_detail)
        btnPlay = findViewById(R.id.btnPlay)
        btnPause = findViewById(R.id.btnPause)
        seekBar = findViewById(R.id.seekBar)

        //Log.d(TAG, "onCreate: ${intent.getStringExtra(HelperConstant.IMAGE)}")
        Log.d(TAG, "onCreate: ${intent.getStringExtra(HelperConstant.TITLE)}")


        fetchRemoteConfigSongListItem(intent.getStringExtra(HelperConstant.TITLE))

        btnPlay.setOnClickListener {
            setUpPlayClickListener()
        }

        btnPause.setOnClickListener {
            setUpPauseClickListener()
        }

    }

    private fun setUpPauseClickListener() {
        btnPause.visibility = View.INVISIBLE
        btnPlay.visibility = View.VISIBLE
        //check media player if audio is playing or not
       if (mediaPlayer.isPlaying){
           //pausing the media player if media is playing

           mediaPlayer.stop()
           mediaPlayer.reset()
           //mediaPlayer.release()
       }else{
           Toast.makeText(this, "Audio has not played", Toast.LENGTH_SHORT).show();
       }
    }

    private fun setUpPlayClickListener() {
        btnPlay.visibility = View.INVISIBLE
        btnPause.visibility = View.VISIBLE

           // mediaPlayer = MediaPlayer()
           // mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
        if (pause){
            mediaPlayer.seekTo(mediaPlayer.currentPosition)
            mediaPlayer.start()
            pause = false
        }else {
            try {
                //set url to media player
                mediaPlayer.setDataSource("https://www.learningcontainer.com/wp-content/uploads/2020/02/Kalimba.mp3")

                //prepare and start media player
                mediaPlayer.prepare()
                mediaPlayer.start()
                Toast.makeText(this, "Audio started playing", Toast.LENGTH_SHORT).show()


            } catch (e: Exception) {
                Log.d(TAG, "setUpPlayClickListener: exception caught in media player ${e.message}")
            }
        }
        initializeSeekBar()
    }

    private fun initializeSeekBar() {
        seekBar.max = mediaPlayer.seconds

    }

    //extension property to get the media player time duration in seconds
    val MediaPlayer.seconds:Int
        get() {
            return this.duration / 1000
        }

    //extension property to get media player current position in seconds
    val MediaPlayer.currentSeconds:Int
        get() {
            return this.currentPosition/1000
        }

    private fun fetchRemoteConfigSongListItem(songTitleIntent: String?) {
        remoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)

        remoteConfig.fetchAndActivate()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val updated = task.result
                    Log.d(TAG, "Config params updated: $updated")

                    val fetchJsonFromKey =
                        remoteConfig.getString("music_list")
                    val convertedObject = JSONObject(fetchJsonFromKey)
                    val arr = convertedObject.getJSONArray(resources.getString(R.string.type))

                    for (i in 0 until arr.length()) {
                        val ob = arr.getJSONObject(i)
                        if (ob.has(resources.getString(R.string.song1))) {

                            val firstSongUrl =
                                ob.getJSONObject(resources.getString(R.string.song1))
                                    .getString(resources.getString(R.string.audiourl))
                            Log.d(TAG, "onCreate: circleEndPoint:: $firstSongUrl")

                            //setUpUi(circleEndPoint)

                        }
                        if (ob.has(resources.getString(R.string.song2))) {
                            val secondSongUrl =
                                ob.getJSONObject(resources.getString(R.string.song2))
                                    .getString(resources.getString(R.string.audiourl))
                            Log.d(TAG, "onCreate: squareEndPoint:: $secondSongUrl")
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


}