package com.and2long.client;

import android.util.Log;

import org.junit.Test;

import java.util.Calendar;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void time() throws Exception {
        int mYear = Calendar.getInstance().get(1) - 2000;
        int mMonth = Calendar.getInstance().get(2) + 1;
        int mDay = Calendar.getInstance().get(5);
        int mHours = Calendar.getInstance().get(11);
        int mMinutes = Calendar.getInstance().get(12);
        int mSeconds = Calendar.getInstance().get(13);
        byte[] SET_TIME = new byte[]{-125, (byte) mYear, (byte) mMonth, (byte) mDay, (byte) mHours, (byte) mMinutes, (byte) mSeconds, 0, 0, 0};
        for (byte b : SET_TIME) {
            System.out.println(b);
        }
        System.out.println("---------");
        byte[] bytes = sum_Check(SET_TIME);
        for (byte b : bytes) {
            System.out.println(b);
        }

    }

    private byte[] sum_Check(byte[] pack) {
        int CHECK_SUM = 0;
        int _size = pack.length - 1;

        for (int i = 0; i < _size; ++i) {
            CHECK_SUM += pack[i] & 255;
        }

        byte _return = (byte) (CHECK_SUM & 127);
        pack[pack.length - 1] = _return;
        return pack;
    }

    public static void printData(byte[] pack, int count) {
        Log.e("***********************", "************************");
        String _temp = "";

        for (int i = 0; i < count; ++i) {
            _temp = _temp + " " + Integer.toHexString(pack[i]);
        }

        Log.e("Data", _temp);
        Log.e("***********************", "************************");
    }

    @Test
    public void print() throws Exception {
//        byte[] bytes = {02, 30, 30, 32, 30, 30, 30, 30, 30, 30, 30, 30, 34, 39, 35, 35, 30, 30, 2D, 2D, 31, 38, 30, 32, 32, 36, 31, 30, 34, 34, 31, 34, 30, 30, 30, 30, 30, 30, 33, 30, 30, 4B, 35, 47, 35, 30, 30, 37, 59, 31, 34, 32, 31, 32, 36, 36, 39, 30, 30, 30, 30, 30, 32, 03, 36, 34, 0D, 0A};

    }

}