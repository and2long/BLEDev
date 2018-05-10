package com.and2long.a2dp

import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.simpleName

    private lateinit var listAdapter: ArrayAdapter<String>
    private lateinit var mBTAdapter: BluetoothAdapter
    private val pairedDevicesList = mutableListOf<BluetoothDevice>()
    private var mA2dp: BluetoothA2dp? = null

    private val mListener: BluetoothProfile.ServiceListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceDisconnected(profile: Int) {
            if (profile == BluetoothProfile.A2DP) {
                mA2dp = null
            }
        }

        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
            if (profile == BluetoothProfile.A2DP) {
                mA2dp = proxy as BluetoothA2dp //转换
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mBTAdapter = BluetoothAdapter.getDefaultAdapter()
        if (!mBTAdapter.isEnabled) {
            //弹出对话框提示用户是否打开
            val enabler = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enabler, 1)
        }

        //获取A2DP代理对象
        mBTAdapter.getProfileProxy(this, mListener, BluetoothProfile.A2DP);

        listAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
        lv.adapter = listAdapter
        lv.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val device = pairedDevicesList[position]
            connectA2dp(device)
        }

        initReceiver()
        showBondedList()

        btn_play.setOnClickListener { startActivity(Intent(this, AudioPlayActivity::class.java)) }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mReceiver)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.i_refresh -> showBondedList()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showBondedList() {
        val pairedDevices = mBTAdapter.bondedDevices
        pairedDevicesList.clear()
        listAdapter.clear()
        // If there are paired devices
        if (pairedDevices.size > 0) {
            // Loop through paired devices
            for (device in pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                listAdapter.add(device.name + "\n" + device.address)
                pairedDevicesList.add(device)
            }
        }
        listAdapter.notifyDataSetChanged()
    }


    private fun connectA2dp(device: BluetoothDevice) {
        setPriority(device, 100) //设置priority
        try {
            //通过反射获取BluetoothA2dp中connect方法（hide的），进行连接。
            val connectMethod = BluetoothA2dp::class.java.getMethod("connect",
                    BluetoothDevice::class.java)
            connectMethod.invoke(mA2dp, device)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun setPriority(device: BluetoothDevice, priority: Int) {
        if (mA2dp == null) return
        try {//通过反射获取BluetoothA2dp中setPriority方法（hide的），设置优先级
            val connectMethod = BluetoothA2dp::class.java.getMethod("setPriority",
                    BluetoothDevice::class.java, Int::class.javaPrimitiveType)
            connectMethod.invoke(mA2dp, device, priority)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun disConnectA2dp(device: BluetoothDevice) {
        setPriority(device, 0)
        try {
            //通过反射获取BluetoothA2dp中connect方法（hide的），断开连接。
            val connectMethod = BluetoothA2dp::class.java.getMethod("disconnect",
                    BluetoothDevice::class.java)
            connectMethod.invoke(mA2dp, device)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * 注册广播接收者监听状态改变
     */
    private fun initReceiver() {
        val filter = IntentFilter(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED)
        filter.addAction(BluetoothA2dp.ACTION_PLAYING_STATE_CHANGED)
        registerReceiver(mReceiver, filter)
    }

    private val mReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            Log.i(TAG, "onReceive action=" + action!!)
            //A2DP连接状态改变
            if (action == BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED) {
                val state = intent.getIntExtra(BluetoothA2dp.EXTRA_STATE, BluetoothA2dp.STATE_DISCONNECTED)
                Log.i(TAG, "connect state=$state")
            } else if (action == BluetoothA2dp.ACTION_PLAYING_STATE_CHANGED) {
                //A2DP播放状态改变
                val state = intent.getIntExtra(BluetoothA2dp.EXTRA_STATE, BluetoothA2dp.STATE_NOT_PLAYING)
                Log.i(TAG, "play state=$state")
            }
        }
    }

}
