package com.and2long.a2dp

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.SeekBar
import kotlinx.android.synthetic.main.activity_audio_play.*
import java.io.IOException


/**
 * Created by and2long on 2018/05/10.
 */

class AudioPlayActivity : AppCompatActivity(), MediaPlayer.OnPreparedListener, View.OnClickListener, MediaPlayer.OnBufferingUpdateListener, SeekBar.OnSeekBarChangeListener, MediaPlayer.OnErrorListener {

    private val TAG = "AudioPlayActivity"

    private lateinit var mMediaPlayer: MediaPlayer
    private var handler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_play)
        setFinishOnTouchOutside(false)

        handler = Handler()
        seekbar.setOnSeekBarChangeListener(this)
        iv_play.setOnClickListener(this)

        initMediaPlayer()
    }

    private fun initMediaPlayer() {
        mMediaPlayer = MediaPlayer()
        try {
            val file = resources.openRawResourceFd(R.raw.sample)
            mMediaPlayer.setDataSource(file.fileDescriptor, file.startOffset, file.length)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        mMediaPlayer.prepareAsync()
        mMediaPlayer.setOnPreparedListener(this)
        mMediaPlayer.setOnBufferingUpdateListener(this)
        mMediaPlayer.setOnErrorListener(this)
    }

    override fun onPrepared(mp: MediaPlayer) {
        mp.start()
        iv_play.setBackgroundResource(R.mipmap.btn_pause)
        seekbar.max = mp.duration
        val duration = formatMediaDuration(mMediaPlayer.duration)
        tv_duration.text = duration
        updatePlayProgress()
    }

    private fun updatePlayProgress() {
        val runnable = object : Runnable {
            override fun run() {
                seekbar.progress = mMediaPlayer.currentPosition
                handler!!.postDelayed(this, 500)
                tv_current.text = formatMediaDuration(mMediaPlayer.currentPosition)
            }
        }
        handler!!.post(runnable)

    }

    override fun onDestroy() {
        super.onDestroy()
        mMediaPlayer.release()
        handler!!.removeCallbacksAndMessages(null)
    }

    override fun onClick(v: View) {

        if (mMediaPlayer.isPlaying) {
            mMediaPlayer.pause()
            iv_play.setBackgroundResource(R.mipmap.btn_play)
        } else {
            mMediaPlayer.start()
            iv_play.setBackgroundResource(R.mipmap.btn_pause)
        }
    }

    override fun onBufferingUpdate(mp: MediaPlayer, percent: Int) {
        seekbar.secondaryProgress = percent
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        if (fromUser) {
            mMediaPlayer.seekTo(progress)
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {

    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {

    }

    override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        Log.e(TAG, "onError: $what")
        /*switch (what) {
            case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
        }*/
        return true
    }

    fun formatMediaDuration(duration: Int): String {
        val HOUR = 60 * 60 * 1000
        val MINUTE = 60 * 1000
        val SECOND = 1000

        val hour = duration / HOUR
        var remainTime = duration % HOUR

        val minute = remainTime / MINUTE
        remainTime %= MINUTE

        val second = remainTime / SECOND
        return if (hour == 0) {
            String.format("%02d:%02d", minute, second)
        } else {
            String.format("%02d:%02d:%02d", hour, minute, second)
        }
    }
}
