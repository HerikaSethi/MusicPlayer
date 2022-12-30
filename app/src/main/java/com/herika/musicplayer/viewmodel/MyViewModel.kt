package com.herika.musicplayer.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.herika.musicplayer.model.SongItems
import kotlinx.coroutines.CoroutineExceptionHandler

class MyViewModel: ViewModel() {

    companion object{
        const val TAG = "MyViewModel"
    }

    val handler = CoroutineExceptionHandler { _, exception ->
        Log.d(TAG, "exception MyViewModel: $exception ")
    }

    val SongsAuthorLiveData = MutableLiveData<String>()



}