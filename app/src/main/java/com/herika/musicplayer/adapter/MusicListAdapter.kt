package com.herika.musicplayer.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.herika.musicplayer.MusicDetailActivity
import com.herika.musicplayer.R
import com.herika.musicplayer.model.MusicItems
import com.herika.musicplayer.utils.HelperConstant

class MusicListAdapter(private val context: Context, private val musicItems: List<MusicItems>): RecyclerView.Adapter<MusicListAdapter.MusicListViewHolder>() {
    class MusicListViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var musicImage:ImageView
        var musicTitle: TextView
        var lMainLayout: ConstraintLayout
        init {
            musicImage = itemView.findViewById(R.id.musicImageView)
            musicTitle = itemView.findViewById(R.id.musicTitle)
            lMainLayout = itemView.findViewById(R.id.lMainLayout)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicListViewHolder {
        return MusicListViewHolder(LayoutInflater.from(context).inflate(R.layout.each_item_music, parent, false))
    }

    override fun onBindViewHolder(holder: MusicListViewHolder, position: Int) {
        holder.musicImage.setImageResource(musicItems[position].songImage)
        holder.musicTitle.text = musicItems[position].songTitle

        holder.lMainLayout.setOnClickListener {
            val intent = Intent(context, MusicDetailActivity::class.java)
            intent.putExtra(HelperConstant.IMAGE, musicItems[position].songImage)
            intent.putExtra(HelperConstant.TITLE, musicItems[position].songTitle)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return musicItems.size
    }

}