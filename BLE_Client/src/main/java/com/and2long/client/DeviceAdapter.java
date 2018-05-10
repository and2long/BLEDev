package com.and2long.client;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Color;
import android.widget.TextView;

import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.util.List;

/**
 * Created by and2long on 2018/3/1.
 * 设备列表适配器
 */

public class DeviceAdapter extends CommonAdapter<DevicesBean> {

    public DeviceAdapter(Context context, List<DevicesBean> datas) {
        super(context, R.layout.item_ble, datas);
    }

    @Override
    protected void convert(ViewHolder holder, DevicesBean bean, int position) {
        BluetoothDevice device = bean.getDevice();
        String deviceName = device.getName();
        String deviceAddress = device.getAddress();
        holder.setText(R.id.tv1, deviceName);
        holder.setText(R.id.tv2, deviceAddress);
        TextView tvState = holder.getView(R.id.tv_state);
        int state = bean.getState();
        switch (state) {
            case Constants.STATE_DISCONNECTED:
                tvState.setText(mContext.getString(R.string.state_disconnected));
                tvState.setTextColor(Color.GRAY);
                break;
            case Constants.STATE_CONNECTING:
                tvState.setText(mContext.getString(R.string.state_connecting));
                tvState.setTextColor(Color.parseColor("#FFD700"));
                break;
            case Constants.STATE_CONNECTED:
                tvState.setText(mContext.getString(R.string.state_connected));
                tvState.setTextColor(Color.GREEN);
                break;
            default:

                break;
        }
    }
}
