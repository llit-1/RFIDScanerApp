package com.cf.socket.interfaces;

/**
 * socket数据回调
 */
public interface ISocketDataCallback {
    /**
     * @param pBytes socket返回数据
     */
    void onDataCallback(byte[] pBytes);
}
