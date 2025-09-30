package com.cf.beans;

import androidx.annotation.NonNull;

import java.io.Serializable;

/**
 * 设备信息
 */
public class DeviceInfoBean extends GeneralBean implements Serializable {
    /**
     * 硬件版本
     */
    public String mHwVer;
    /**
     * 固件版本
     */
    public String mFirmVer;
    /**
     * 序列号
     */
    public String mSn;
    /**
     * RFID模块版本
     */
    public String mRFIDModeVer;
    /**
     * RFID模块名称
     */
    public String mRFIDModeName;
    /**
     * RFID模块序列号
     */
    public String mRFIDModeSn;

    @NonNull
    @Override
    public String toString() {
        return "DeviceInfoBean{" + "mCode=" + mStatus + ", mHwVer='" + mHwVer + '\'' + ", mFirmVer='" + mFirmVer + '\'' + ", mSn='" + mSn + '\'' + ", mRFIDModeVer='" + mRFIDModeVer + '\'' + ", mRFIDModeName='" + mRFIDModeName + '\'' + ", mRFIDModeSn='" + mRFIDModeSn + '\'' + ", mMsg='" + mMsg + '\'' + '}';
    }
}
