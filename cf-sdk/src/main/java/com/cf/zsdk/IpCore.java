package com.cf.zsdk;

import android.os.SystemClock;

import androidx.annotation.WorkerThread;

import com.cf.socket.interfaces.ISocketDataCallback;
import com.cf.zsdk.uitl.IOUtil;
import com.cf.zsdk.uitl.LogUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

public class IpCore {


    private Socket mSocket;
    private final AtomicBoolean mAtomicBoolean = new AtomicBoolean(false);

    protected IpCore() {
    }

    /**
     * 连接
     *
     * @param pIpAddress ip地址
     * @param pPort      端口号
     * @return true：表示连接成功，反之连接失败
     */
    public boolean connect(String pIpAddress, int pPort) {
        LogUtil.d("IpCore.connect == > call");
        try {
            mSocket = new Socket(pIpAddress, pPort);
            return true;
        } catch (IOException pE) {
            pE.printStackTrace();
        }
        return false;
    }

    /**
     * 写数据
     *
     * @param pBytes 数据byte数组
     * @return true：写出成功，false写出失败
     */
    @WorkerThread
    public boolean writeData(byte[] pBytes) {
        LogUtil.d("IpCore.writeData == > call");
        if (mSocket == null) return false;
        try {
            OutputStream outputStream = mSocket.getOutputStream();
            outputStream.write(pBytes);
            return true;
        } catch (IOException pE) {
            pE.printStackTrace();
        }
        return false;
    }

    /**
     * 子线程接收数据，
     *
     * @param pCallback 数据回调接口{@link ISocketDataCallback}
     */
    public synchronized void receiveData(ISocketDataCallback pCallback) {
        LogUtil.d("IpCore.receiveData == > call");
        if (mSocket == null || mAtomicBoolean.get()) return;
        CfSdk.sSdkTp.submit(() -> {
            mAtomicBoolean.set(true);
            while (mAtomicBoolean.get()) {
                try {
                    InputStream inputStream = mSocket.getInputStream();
                    int available = inputStream.available();
                    if (available <= 0) {
                        SystemClock.sleep(5);
                        continue;
                    }
                    byte[] bytes = new byte[available];
                    inputStream.read(bytes);
                    if (pCallback != null) {
                        pCallback.onDataCallback(bytes);
                    }
                } catch (IOException pE) {
                    pE.printStackTrace();
                }
            }
        });
    }

    /**
     * 断开连接
     */
    public void disConnect() {
        LogUtil.d("IpCore.disConnect == > call");
        mAtomicBoolean.set(false);
        try {
            if (mSocket != null) {
                OutputStream outputStream = mSocket.getOutputStream();
                InputStream inputStream = mSocket.getInputStream();
                IOUtil.close(outputStream);
                IOUtil.close(inputStream);
                mSocket.close();
            }
        } catch (IOException pE) {
            throw new RuntimeException(pE);
        }
    }
}
