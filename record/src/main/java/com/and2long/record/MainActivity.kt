package com.and2long.record

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.AudioManager.ACTION_SCO_AUDIO_STATE_CHANGED
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private val TAG = "BluetoothRecord"
    private var mFileName: String? = null
    private var mAudioManager: AudioManager? = null
    private var mRecorder: MediaRecorder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager?

        btn_start.setOnClickListener { startRecording() }
        btn_stop.setOnClickListener { stopRecording() }
        btn_play.setOnClickListener { startActivity(Intent(this, AudioPlayActivity::class.java)) }
    }

    private fun stopRecording() {
        Log.i(TAG, "stop record")
        //mAudioManager.stopBluetoothSco()
        mRecorder!!.stop()
        mRecorder!!.release()
        mRecorder = null
//        if (mAudioManager!!.isBluetoothScoOn) {
//            mAudioManager!!.isBluetoothScoOn = false
//            mAudioManager!!.stopBluetoothSco()
//        }
    }

    private fun startRecording() {
        mFileName = Environment.getExternalStorageDirectory().absolutePath
        mFileName += "/record.3gp"
        if (mRecorder == null) {
            mRecorder = MediaRecorder()
            mRecorder!!.setAudioSource(MediaRecorder.AudioSource.DEFAULT)
            mRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            mRecorder!!.setOutputFile(mFileName)
            mRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            try {
                mRecorder!!.prepare()
            } catch (e: Exception) {
                // TODO: handle exception
                Log.i(TAG, "prepare() failed!")
            }
        }

        if (!mAudioManager!!.isBluetoothScoAvailableOffCall) {
            Log.i(TAG, "系统不支持蓝牙录音")
            return
        }
        Log.i(TAG, "系统支持蓝牙录音")
//        mAudioManager!!.stopBluetoothSco()
        mAudioManager!!.startBluetoothSco()//蓝牙录音的关键，启动SCO连接，耳机话筒才起作用

        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1)

                if (AudioManager.SCO_AUDIO_STATE_CONNECTED == state) {
                    Log.i(TAG, "AudioManager.SCO_AUDIO_STATE_CONNECTED")
                    mAudioManager!!.isBluetoothScoOn = true  //打开SCO
                    Log.i(TAG, "Routing:" + mAudioManager!!.isBluetoothScoOn)
                    mAudioManager!!.mode = AudioManager.STREAM_MUSIC
                    mRecorder!!.start()//开始录音
                    Log.i(TAG, "start record")
                    unregisterReceiver(this)  //别遗漏
                } else {//等待一秒后再尝试启动SCO
                    try {
                        Thread.sleep(10000)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }

                    mAudioManager!!.startBluetoothSco()
                    Log.i(TAG, "再次startBluetoothSco()")

                }
            }
        }, IntentFilter(ACTION_SCO_AUDIO_STATE_CHANGED))
    }
}
