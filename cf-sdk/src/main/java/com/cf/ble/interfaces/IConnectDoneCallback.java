package com.cf.ble.interfaces;

/**
 * 设备连接成功回调
 */
public interface IConnectDoneCallback {
    /**
     * @param pB true：为连接成功，反之连接失败
     */
    void onConnectDone(boolean pB);
}
