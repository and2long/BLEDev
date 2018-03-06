package com.and2long.bluetoothdev;

import android.app.Application;
import android.content.Context;

/**
 * Created by and2long on 2018/3/6.
 */

public class App extends Application {

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        CrashExceptionHandler.getInstance().init(this);

    }

    public static Context getContext() {
        return mContext;
    }

}
