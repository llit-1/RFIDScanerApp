package com.cf.uart.interfaces;

/**
 * 串口数据回调
 */
public interface ISerialDataCallback {
    /**
     * @param pBytes 串口数据
     */
    void onDataCallback(byte[] pBytes);
}
