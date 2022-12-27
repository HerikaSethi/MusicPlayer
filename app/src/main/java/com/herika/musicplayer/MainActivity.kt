package com.herika.musicplayer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.herika.musicplayer.adapter.MusicListAdapter
import com.herika.musicplayer.model.MusicItems
import java.util.ArrayList

class MainActivity : AppCompatActivity() {
    lateinit var recyclerView: RecyclerView
    private var recyclerViewAdapter: MusicListAdapter? = null
    private val musicList: MutableList<MusicItems> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        initRecyclerView()

    }

    private fun initRecyclerView() {
        recyclerView = findViewById(R.id.rvMusic)
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        addRecyclerViewItems()
        recyclerViewAdapter = MusicListAdapter(this,musicList)
        recyclerView.adapter = recyclerViewAdapter
    }

    private fun addRecyclerViewItems() {
        musicList.add(MusicItems(R.drawable.musicimg1, "Song1"))
        musicList.add(MusicItems(R.drawable.musicimg2, "Song2"))
        musicList.add(MusicItems(R.drawable.musicimg3, "Song3"))
        musicList.add(MusicItems(R.drawable.musicimg4, "Song4"))
    }
}