package com.cf.beans;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Arrays;

public class RemoteNetParaBean extends GeneralBean implements Serializable {
    /**
     * 操作项
     * 0x01：设置； 0x02：获取
     */
    public byte mOption;
    /**
     * 4个字节
     * 设备IP地址，若IP地址为192.168.1.1则数据为[0xC0,A8,0x01,0x01]
     */
    public byte[] mIpAddr;
    /**
     * 6个字节
     * Mac地址，如果多个设备在同一现场工作，请使用不同的MAC地址；
     * 例如：mac[6]={0x00,0x08,0xdc,0x11,0x11,0x11}，mac地址为00-08-DC-11-11-11
     */
    public byte[] mMacAddr;
    /**
     * 2个字节
     * 监听端口，取值为[0,65536]，默认为5000；
     */
    public byte[] mPort;
    /**
     * 4个字节
     * 子网掩码，默认为[0xFF,0xFF,0xFF,0x00]
     */
    public byte[] mNetMask;
    /**
     * 4个字节
     * 默认网关，默认为[0xC0,0xA8,0x01,0x01]
     */
    public byte[] mGateWay;

    @NonNull
    @Override
    public String toString() {
        return "RemoteNetParaBean{" + "mOption=" + mOption + ", mIpAddr=" + Arrays.toString(mIpAddr) + ", mMacAddr=" + Arrays.toString(mMacAddr) + ", mPort=" + Arrays.toString(mPort) + ", mNetMask=" + Arrays.toString(mNetMask) + ", mGateWay=" + Arrays.toString(mGateWay) + ", mStatus=" + mStatus + ", mMsg='" + mMsg + '\'' + '}';
    }
}
