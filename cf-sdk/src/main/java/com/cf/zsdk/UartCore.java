package com.cf.zsdk;

import android.os.SystemClock;

import com.cf.serial.CfSerial;
import com.cf.uart.interfaces.ISerialDataCallback;
import com.cf.zsdk.uitl.LogUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class UartCore {

    private CfSerial mCfSerial;

    private volatile boolean mReceivingDataFlag;

    protected UartCore() {

    }

    /**
     * 初始化
     *
     * @param pPath     串口地址
     * @param pBaudRate 波特率
     * @return true：初始化成功，false初始化失败
     */
    public boolean init(String pPath, int pBaudRate) {
        LogUtil.d("UartCore.init == > call");
        try {
            mCfSerial = new CfSerial(pPath, pBaudRate, 0);
            return true;
        } catch (Exception pE) {
            pE.printStackTrace();
        }
        return false;
    }

    /**
     * 发送数据
     *
     * @param pBytes 要发送数据的byte数组
     * @return true：表示发送成功，false表示发送失败
     */
    public boolean sendData(byte[] pBytes) {
        LogUtil.d("UartCore.sendData == > call");
        if (mCfSerial == null) return false;
        OutputStream outputStream = mCfSerial.getOutputStream();
        try {
            outputStream.write(pBytes);
            outputStream.flush();
            return true;
        } catch (IOException pE) {
            pE.printStackTrace();
        }
        return false;
    }

    /**
     * 接收串口数据
     *
     * @param pSerialDataCallback 串口数据回调接口
     */
    public synchronized void receiverData(ISerialDataCallback pSerialDataCallback) {
        LogUtil.d("UartCore.receiverData == > call");
        if (mCfSerial == null || mReceivingDataFlag) return;
        CfSdk.sSdkTp.submit(() -> {
            mReceivingDataFlag = true;
            while (mReceivingDataFlag) {
                InputStream inputStream = mCfSerial.getInputStream();
                try {
                    int available = inputStream.available();
                    if (available <= 0) {
                        SystemClock.sleep(5);
                        continue;
                    }
                    byte[] bytes = new byte[available];
                    int read = inputStream.read(bytes);
                    if (pSerialDataCallback != null && read > 0) {
                        pSerialDataCallback.onDataCallback(bytes);
                    }
                } catch (IOException pE) {
                    pE.printStackTrace();
                }
            }
        });
    }

    /**
     * 停止接收数据
     */
    public void stopReceiverData() {
        LogUtil.d("UartCore.stopReceiverData == > call");
        mReceivingDataFlag = false;
    }

    /**
     * 释放串口
     */
    public void release() {
        LogUtil.d("UartCore.release == > call");
        stopReceiverData();
        if (mCfSerial != null) mCfSerial.closeSerial();
    }

}
