package com.cf.zsdk;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

public class CfSdk {

    public static ExecutorService sSdkTp;

    private static final ReentrantLock sReentrantLock = new ReentrantLock();

    private static volatile BleCore sBleCore;
    private static volatile IpCore sIpCore;
    private static volatile UartCore sUartCore;
    private static volatile UsbCore sUsbCore;

    private CfSdk() {
    }

    public static void load() {
        load(Executors.newCachedThreadPool());
    }

    public static void load(ExecutorService pExecutorService) {
        sSdkTp = pExecutorService;
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> pClass) {
        try {
            //这里的代码比较冗余，可以优化一下，增加其拓展性
            if (BleCore.class.equals(pClass)) {
                if (sBleCore == null) {
                    sReentrantLock.lock();
                    sBleCore = new BleCore();
                }
                return (T) sBleCore;
            }
            if (UsbCore.class.equals(pClass)) {
                if (sUsbCore == null) {
                    sReentrantLock.lock();
                    sUsbCore = new UsbCore();
                }
                return (T) sUsbCore;
            }
            if (IpCore.class.equals(pClass)) {
                if (sIpCore == null) {
                    sReentrantLock.lock();
                    sIpCore = new IpCore();
                }
                return (T) sIpCore;
            }
            if (UartCore.class.equals(pClass)) {
                if (sUartCore == null) {
                    sReentrantLock.lock();
                    sUartCore = new UartCore();
                }
                return (T) sUartCore;
            } else {
                throw new IllegalArgumentException("Unknown data type");
            }
        } finally {
            if (sReentrantLock.isLocked()) {
                sReentrantLock.unlock();
            }
        }
    }

    public static void release() {
        if (!sSdkTp.isShutdown()) {
            sSdkTp.shutdown();
        }
    }
}
