package com.cf.usb.interfaces;

/**
 * usb读取数据回调
 */
public interface IReadDataCallback {
    /**
     * @param pBytes usb设备返回的数据
     */
    void onDataBack(byte[] pBytes);
}