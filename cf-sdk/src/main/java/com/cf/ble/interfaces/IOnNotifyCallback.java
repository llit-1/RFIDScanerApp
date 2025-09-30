package com.cf.ble.interfaces;


import com.cf.beans.CmdData;

/**
 * ble设备应答回调接口
 */
public interface IOnNotifyCallback {

    /**
     * 创方指令解析后返回接口，改指令解析只适用于
     * 我司设备的返回指令的解析处理，其他厂商
     * 设备返回的数据，需开发者自行解析处理
     *
     * @param pCmdType 指令类型
     * @param pCmdData 数据集
     */
    void onNotify(int pCmdType, CmdData pCmdData);

    /**
     * 默认实现，有需要的开发者可自行实现并处理原数据
     *
     * @param pBytes ble应答原始数据
     */
    default void onNotify(byte[] pBytes) {
    }
}
