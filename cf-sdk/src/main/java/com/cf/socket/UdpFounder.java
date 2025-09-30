package com.cf.socket;

import android.os.SystemClock;
import android.text.TextUtils;

import com.cf.socket.interfaces.IUdpDataCallback;
import com.cf.zsdk.CfSdk;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;

public class UdpFounder {

    private static final UdpFounder sUdpFounder = get();

    private volatile boolean mReceivingDataFlag = false;
    private volatile IUdpDataCallback mIUdpDataCallback;
    private volatile byte[] mSentData = new byte[0];
    private volatile boolean mSendingBroadcastFlag = false;

    private static InetAddress BROADCAST_ADDRESS;

    static {
        try {
            BROADCAST_ADDRESS = InetAddress.getByName("255.255.255.255");
        } catch (UnknownHostException pE) {
            pE.printStackTrace();
        }
    }

    public synchronized static UdpFounder get() {
        synchronized (UdpFounder.class) {
            if (sUdpFounder == null) {
                return new UdpFounder();
            }
            return sUdpFounder;
        }
    }


    /**
     * 发送单次广播
     *
     * @param pData 广播数据
     * @param pPort 目标端口号
     */
    public void sendBroadcast(String pData, int pPort) {
        if (TextUtils.isEmpty(pData)) return;
        sendBroadcast(pData.getBytes(), pPort, 0);
    }

    /**
     * 发送广播
     *
     * @param pData       广播数据
     * @param pTargetPort 目标端口号
     * @param pTimeOut    发送广播时长，在当前该时间内会间隔200ms
     *                    持续发送广播，超过该时长则自定停止发送udp
     *                    广播，当时长设置为0则为发送单次广播
     */
    public synchronized void sendBroadcast(byte[] pData, int pTargetPort, int pTimeOut) {
        if (mSendingBroadcastFlag) return;
        mSentData = pData;
        CfSdk.sSdkTp.submit(() -> {
            mSendingBroadcastFlag = true;
            try (DatagramSocket socket = new DatagramSocket()) {
                socket.setBroadcast(true);
                DatagramPacket datagramPacket = new DatagramPacket(pData, 0, pData.length);
                datagramPacket.setAddress(BROADCAST_ADDRESS);
                datagramPacket.setPort(pTargetPort);
                //多次发送广播
                long currentTimeMillis = System.currentTimeMillis();
                while (mSendingBroadcastFlag) {
                    socket.send(datagramPacket);
                    if (System.currentTimeMillis() - currentTimeMillis >= pTimeOut || !mSendingBroadcastFlag) {
                        break;
                    }
                    SystemClock.sleep(200);
                    if (System.currentTimeMillis() - currentTimeMillis >= pTimeOut || !mSendingBroadcastFlag) {
                        break;
                    }
                }
            } catch (Exception pException) {
                pException.printStackTrace();
            } finally {
                mSendingBroadcastFlag = false;
            }
        });
    }

    /**
     * 停止发送关播
     */
    public void stopSendBroadcast() {
        mSendingBroadcastFlag = false;
    }

    /**
     * 接收目标端口号数据
     *
     * @param pPort 目标端口号
     */
    public synchronized void receiveData(int pPort) {
        if (mReceivingDataFlag) return;
        CfSdk.sSdkTp.submit(() -> {
            mReceivingDataFlag = true;
            try (DatagramSocket mReceiveSocket = new DatagramSocket(new InetSocketAddress(pPort))) {
                while (mReceivingDataFlag) {
                    // TODO: 2024-8-10 这里大量new数据是个bad code需要优化一下
                    byte[] buffer = new byte[64];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    try {
                        mReceiveSocket.setSoTimeout(100);
                        mReceiveSocket.receive(packet);
                    } catch (IOException ignored) {
                        continue;
                    }
                    if (packet.getLength() <= 0) continue;
                    //过滤本机发送的广播
                    byte[] bytes = Arrays.copyOf(packet.getData(), packet.getLength());
                    if (Arrays.equals(mSentData, bytes)) continue;
                    if (mIUdpDataCallback != null) {
                        packet.setData(bytes);
                        mIUdpDataCallback.onReceiveUdpData(packet);
                    }
                }
            } catch (SocketException pE) {
                pE.printStackTrace();
            } finally {
                mReceivingDataFlag = false;
            }
        });
    }

    /**
     * 设置udp广播数据回调，置空为注销回调
     *
     * @param pIUdpDataCallback udp数据返回接口
     */
    public void setIUdpDataCallback(IUdpDataCallback pIUdpDataCallback) {
        mIUdpDataCallback = pIUdpDataCallback;
    }

    /**
     * 释放
     */
    public void stopReceiveData() {
        mReceivingDataFlag = false;
    }
}
