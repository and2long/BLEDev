package com.and2long.client;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.SystemClock;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by and2long on 2017/11/1.
 * 异常捕获
 */

public class CrashExceptionHandler implements Thread.UncaughtExceptionHandler {

    private static CrashExceptionHandler mInstance;
    private Context mContext;
    private static String versionName;
    private static int versionCode;

    public static CrashExceptionHandler getInstance() {
        if (mInstance == null) {
            synchronized (CrashExceptionHandler.class) {
                if (mInstance == null) {
                    mInstance = new CrashExceptionHandler();
                }
            }
        }
        return mInstance;
    }

    public void init(Context context) {
        this.mContext = context;
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * 核心方法，当程序crash 会回调此方法， Throwable中存放这错误日志
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        saveCrashInfo2File(ex);
        try {
            ApplicationInfo info = mContext.getApplicationInfo();
            if ((info.flags & ApplicationInfo.FLAG_DEBUGGABLE) == 0) {
                SystemClock.sleep(3000);
            }
        } catch (Exception e) {
            saveCrashInfo2File(e);
        }
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    private void saveCrashInfo2File(Throwable ex) {
        File logFile = new File(mContext.getExternalCacheDir(), "crash.txt");
        //系统版本等信息
        try {
            PackageInfo pi = App.getContext().getPackageManager().getPackageInfo(App.getContext().getPackageName(), 0);
            if (pi != null) {
                versionName = pi.versionName;
                versionCode = pi.versionCode;
            }
            String CRASH_HEAD = "************* Crash Log Head ****************" +
                    "\nDevice Manufacturer: " + Build.MANUFACTURER +// 设备厂商
                    "\nDevice Model       : " + Build.MODEL +// 设备型号
                    "\nAndroid Version    : " + Build.VERSION.RELEASE +// 系统版本
                    "\nAndroid SDK        : " + Build.VERSION.SDK_INT +// SDK 版本
                    "\nApp VersionName    : " + versionName +
                    "\nApp VersionCode    : " + versionCode +
                    "\nMobile Processor   : " + Build.BOARD +
                    "\n************* Crash Log Head ****************\n\n";
            writeFile(logFile, CRASH_HEAD);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        StringWriter sw = new StringWriter();
        String timeStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        sw.append(timeStr).append('\n');
        ex.printStackTrace(new PrintWriter(sw));
        writeFile(logFile, sw.toString());
        writeFile(logFile, "\n-----------------------Crash Info End-----------------------\n");
    }

    private void writeFile(File file, String msg) {
        FileWriter fw = null;
        try {
            File dir = file.getParentFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            fw = new FileWriter(file, true);
            fw.append(msg);
            fw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fw != null)
                    fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
