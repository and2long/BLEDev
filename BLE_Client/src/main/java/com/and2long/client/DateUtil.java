package com.and2long.client;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by and2long on 2017/8/23.
 * 时间格式化工具类
 */

public class DateUtil {

    /**
     * 当前时间的格式化体现
     *
     * @return
     */
    public static String getCurrentDateFormat() {
        return new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss:sss", Locale.getDefault()).format(new Date());
    }


    /**
     * 当前时间毫秒值，用作数据库的hash_key
     *
     * @return
     */
    public static String getCurrentTimeMillis() {
        return System.currentTimeMillis() + "";
    }



}
