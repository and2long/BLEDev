package com.and2long.bluetoothdev;

import android.content.Context;

import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.util.List;

/**
 * Created by and2long on 2018/3/5.
 */

public class LogAdapter extends CommonAdapter<String> {

    public LogAdapter(Context context, List<String> datas) {
        super(context, R.layout.item_log, datas);
    }

    @Override
    protected void convert(ViewHolder holder, String s, int position) {
        holder.setText(R.id.tv_log, s);
    }

}
