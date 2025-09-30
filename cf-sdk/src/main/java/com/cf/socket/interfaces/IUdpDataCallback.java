package com.cf.socket.interfaces;

import java.net.DatagramPacket;

/**
 * udp广播数据回调
 */
public interface IUdpDataCallback {
    /**
     * @param pPacket udp广播应答包
     */
    void onReceiveUdpData(DatagramPacket pPacket);
}