package com.and2long.chat

import android.bluetooth.BluetoothAdapter
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.fragment_bonded_devices.*

/**
 * 已配对蓝牙列表
 */
class BondedDevicesFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_bonded_devices, null)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val mArrayAdapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, mutableListOf<String>())
        val pairedDevices = BluetoothAdapter.getDefaultAdapter().bondedDevices
        // If there are paired devices
        if (pairedDevices.size > 0) {
            // Loop through paired devices
            for (device in pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                mArrayAdapter.add(device.name + "\n" + device.address)
            }
        }
        lv_bondedDevices.adapter = mArrayAdapter
    }
}