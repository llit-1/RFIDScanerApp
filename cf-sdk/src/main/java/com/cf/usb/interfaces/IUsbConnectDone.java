package com.cf.usb.interfaces;

/**
 * usb连接完成回调
 */
public interface IUsbConnectDone {
    /**
     * @param pB true：表示连接成功，反之连接失败
     */
    void onUsbConnectDone(boolean pB);
}
