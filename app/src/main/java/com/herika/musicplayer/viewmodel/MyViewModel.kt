package com.herika.musicplayer.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.herika.musicplayer.MusicDetailActivity
import com.herika.musicplayer.PlayMusicActivity
import com.herika.musicplayer.R
import com.herika.musicplayer.model.SongItems
import com.herika.musicplayer.utils.HelperConstant
import kotlinx.coroutines.CoroutineExceptionHandler
import org.json.JSONObject

class MyViewModel: ViewModel() {

    companion object{
        const val TAG = "MyViewModel"
    }

    private lateinit var remoteConfig: FirebaseRemoteConfig
    private val songList: MutableList<SongItems> = ArrayList()

    var songUrlLiveData = MutableLiveData<String>()
    val saveDataToSharePreferenceResponse = MutableLiveData<SongItems>()
    var showLoading = MutableLiveData<Boolean>()

    val handler = CoroutineExceptionHandler { _, exception ->
        Log.d(TAG, "exception MyViewModel: $exception ")
    }


    fun fetchDataFromFirebase(title: String?){
        remoteConfig = Firebase.remoteConfig

        showLoading.value = true

        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)

        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val updated = task.result
                    Log.d(TAG, "Config params updated: $updated")

                    val fetchJsonFromKey =
                        remoteConfig.getString("music_list")
                    val convertedObject = JSONObject(fetchJsonFromKey)
                    val arr = convertedObject.getJSONArray("type")

                    showLoading.value = false

                    for (i in 0 until arr.length()) {
                        val ob = arr.getJSONObject(i)

                        when(title){
                            "Song1" -> {

                                retrieveData("song1",ob)
                            }

                            "Song2" -> {
                                retrieveData("song2",ob)
                            }

                            "Song3" -> {
                                retrieveData("song3",ob)
                            }

                            "Song4" -> {
                                retrieveData("song4",ob)
                            }
                        }
                    }

                    Log.d(TAG, "fetchDataFromFirebase: Fetch and activate succeeded")
                } else {
                    showLoading.value = true
                    Log.d(TAG, "fetchDataFromFirebase: Fetch failed")
                }
            }

    }

    private fun retrieveData(song: String, ob: JSONObject) {
        if (ob.has(song)) {
            val songUrl =
                ob.getJSONObject(song)
                    .getString("audiourl")
            Log.d(
                TAG,
                "onCreate: EndPoint:: $songUrl"
            )

            val songTitle =
                ob.getJSONObject(song)
                    .getString("title")
            Log.d(TAG, "fetchRemoteConfigSongListItem:: title: $songTitle")


            val songAuthor =
                ob.getJSONObject(song)
                    .getString("author")
            Log.d(TAG, "fetchRemoteConfigSongListItem:: author: $songAuthor")

            val songStartTime =
                ob.getJSONObject(song)
                    .getString("startTime")
            Log.d(
                TAG,
                "fetchRemoteConfigSongListItem:: startTime: $songStartTime"
            )

            val songEndTime =
                ob.getJSONObject(song)
                    .getString("endTime")
            Log.d(
                TAG,
                "fetchRemoteConfigSongListItem:: endTime: $songEndTime"
            )

            songList.add(SongItems(songUrl,songTitle,songAuthor,songStartTime,songEndTime))
            setUpUI()
        }
    }


    private fun setUpUI() {
        songList.map {
            Log.d(TAG, "setUpUI: title ${it.title}  author: ${it.author}")
            songUrlLiveData.postValue(it.url)
            saveDataToSharePreferenceResponse.postValue(it)
        }
    }




}