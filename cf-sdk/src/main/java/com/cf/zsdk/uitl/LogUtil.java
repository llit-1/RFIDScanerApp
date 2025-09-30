package com.cf.zsdk.uitl;

import android.util.Log;

public class LogUtil {

    public static final String TAG = "CF_SDK_LOG";

    /**
     * 日志开关，默认打开
     */
    private static boolean mLogSwitch = true;

    public static void setLogSwitch(boolean pB) {
        mLogSwitch = pB;
    }

    public static void d(String pMsg) {
        if (mLogSwitch) Log.d(TAG, pMsg);
    }

    public static void d(String pTag, String pMsg) {
        if (mLogSwitch) Log.d(pTag, pMsg);
    }

    public static void e(String pMsg) {
        if (mLogSwitch) Log.e(TAG, pMsg);
    }

    public static void e(String pTag, String pMsg) {
        if (mLogSwitch) Log.e(pTag, pMsg);
    }

}
