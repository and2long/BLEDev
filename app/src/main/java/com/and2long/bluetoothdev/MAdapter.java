package com.and2long.bluetoothdev;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.util.List;

/**
 * Created by and2long on 2018/3/1.
 */

public class MAdapter extends CommonAdapter<BluetoothDevice> {

    public MAdapter(Context context, List<BluetoothDevice> datas) {
        super(context, R.layout.item_blt, datas);
    }

    @Override
    protected void convert(ViewHolder holder, BluetoothDevice device, int position) {
        String text = device.getName() + "\n" + device.getAddress();
        holder.setText(R.id.tv, text);
    }
}
