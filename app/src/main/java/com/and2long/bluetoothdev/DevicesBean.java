package com.and2long.bluetoothdev;

import android.bluetooth.BluetoothDevice;

/**
 * Created by and2long on 2018/3/8.
 * 设备数据实体
 */

public class DevicesBean {

    private BluetoothDevice device;
    private int state;

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
