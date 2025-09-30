package com.cf.zsdk;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.SystemClock;
import android.util.Pair;

import androidx.core.content.ContextCompat;

import com.cf.usb.interfaces.IReadDataCallback;
import com.cf.usb.interfaces.IUsbConnectDone;
import com.cf.zsdk.uitl.LogUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

public class UsbCore {

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    /**
     * usb权限广播接收器
     */
    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false) && device != null) {
                        connectDevice(context, device, mIUsbConnectDone);
                    } else {
                        LogUtil.d("permission denied for device");
                        mIUsbConnectDone.onUsbConnectDone(false);
                    }
                    mIUsbConnectDone = null;
                }
            }
            context.unregisterReceiver(this);
        }
    };

    private volatile boolean mIsReceivingData = false;
    private volatile IReadDataCallback mIReadDataCallback;
    private IUsbConnectDone mIUsbConnectDone;

    private final byte[] sBuffer = new byte[64];

    private UsbManager mUsbManager;
    private UsbDeviceConnection mUsbDeviceConnection;
    private UsbInterface mInterface;
    private UsbEndpoint mInEndpoint;
    private UsbEndpoint mOutEndpoint;


    protected UsbCore() {

    }

    /**
     * 初始化
     *
     * @param pContext 上下文对象
     */
    public void init(Context pContext) {
        LogUtil.d("UsbCore.init == > call");
        mUsbManager = (UsbManager) pContext.getApplicationContext().getSystemService(Context.USB_SERVICE);
    }

    /**
     * 获取所有usb设备的pid和vid
     *
     * @return 所有设备的pid和vid设备列表
     */
    public ArrayList<Pair<Integer, Integer>> getAllDevicePidAndVid() {
        LogUtil.d("UsbCore.getAllDevicePidAndVid == > call");
        if (mUsbManager == null) return null;
        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        Iterator<UsbDevice> iterator = deviceList.values().iterator();
        ArrayList<Pair<Integer, Integer>> pidAndVidList = new ArrayList<>();
        while (iterator.hasNext()) {
            UsbDevice usbDevice = iterator.next();
            int productId = usbDevice.getProductId();
            int vendorId = usbDevice.getVendorId();
            Pair<Integer, Integer> integerIntegerPair = new Pair<>(productId, vendorId);
            pidAndVidList.add(integerIntegerPair);
        }
        return pidAndVidList;
    }

    /**
     * 根据提供的pid和vid寻找目标设备
     *
     * @param pPid
     * @param pVid
     * @return 返回命中的usb设备，否则返回null
     */
    public UsbDevice findTargetDevice(int pPid, int pVid) {
        LogUtil.d("UsbCore.findTargetDevice == > call");
        if (mUsbManager == null) return null;
        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        UsbDevice targetDevice = null;
        for (UsbDevice next : deviceList.values()) {
            if (next.getProductId() != pPid || next.getVendorId() != pVid) continue;
            targetDevice = next;
            break;
        }
        return targetDevice;
    }

    /**
     * 连接设备
     *
     * @param pContext         上下文对象
     * @param pUsbDevice       连接的目标设备
     * @param pIUsbConnectDone usb设备连接完成回调
     */
    public void connectDevice(Context pContext, UsbDevice pUsbDevice, IUsbConnectDone pIUsbConnectDone) {
        LogUtil.d("UsbCore.connectDevice == > call");
        if (mUsbManager == null) {
            LogUtil.d("please call the init function first");
            return;
        }

        if (mUsbDeviceConnection != null) {
            LogUtil.d("a device has been connect,connect the other one please unconnected the current and retry");
            return;
        }

        mIUsbConnectDone = pIUsbConnectDone;

        if (!mUsbManager.hasPermission(pUsbDevice)) {
            IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
            ContextCompat.registerReceiver(pContext, usbReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
            PendingIntent permissionIntent = PendingIntent.getBroadcast(pContext, 0, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_MUTABLE);
            mUsbManager.requestPermission(pUsbDevice, permissionIntent);
            return;
        }

        mUsbDeviceConnection = mUsbManager.openDevice(pUsbDevice);

        UsbInterface targetInterface = null;
        for (int i = 0; i < pUsbDevice.getInterfaceCount(); i++) {
            int interfaceClass = pUsbDevice.getInterface(i).getInterfaceClass();
            if (interfaceClass != UsbConstants.USB_CLASS_HID) continue;
            targetInterface = pUsbDevice.getInterface(i);
            break;
        }

        if (targetInterface == null) {
            mIUsbConnectDone.onUsbConnectDone(false);
            mUsbDeviceConnection = null;
            mIUsbConnectDone = null;
            return;
        }

        mInterface = targetInterface;

        for (int i1 = 0; i1 < targetInterface.getEndpointCount(); i1++) {
            UsbEndpoint endpoint = targetInterface.getEndpoint(i1);
            if (endpoint.getType() != UsbConstants.USB_ENDPOINT_XFER_INT) continue;
            int direction = endpoint.getDirection();
            if (direction == UsbConstants.USB_DIR_IN) {
                mInEndpoint = endpoint;
            } else if (direction == UsbConstants.USB_DIR_OUT) {
                mOutEndpoint = endpoint;
            }
        }

        if (mUsbDeviceConnection != null) {
            mUsbDeviceConnection.claimInterface(mInterface, true);
            mIUsbConnectDone.onUsbConnectDone(true);
            mIUsbConnectDone = null;
        }
    }

    /**
     * 写数据
     *
     * @param pData    要发送数据byte数组
     * @param pTimeOUt 超时单位ms
     * @return true：写出成功，反之写出失败
     */
    public boolean writeData(byte[] pData, int pTimeOUt) {
        LogUtil.d("UsbCore.writeData == > call");
        if (mUsbDeviceConnection == null) {
            return false;
        }
        int i = mUsbDeviceConnection.bulkTransfer(mOutEndpoint, pData, pData.length, pTimeOUt);
        return i == pData.length;
    }

    /**
     * 同步获取usb数据
     *
     * @param pTimeOut 获取数据超时
     * @return 返回usb读取的数据
     */
    public byte[] readDataSync(int pTimeOut) {
        LogUtil.d("UsbCore.readDataSync == > call");
        if (mUsbDeviceConnection == null || mInEndpoint == null) return null;
        Arrays.fill(sBuffer, (byte) 0);
        if (mUsbDeviceConnection.bulkTransfer(mInEndpoint, sBuffer, sBuffer.length, pTimeOut) <= 0) {
            return null;
        }
        return Arrays.copyOf(sBuffer, sBuffer.length);
    }

    /**
     * 异步读取usb数据
     *
     * @param pTimeOut 单次读取数据超时
     */
    public synchronized void readDataAsync(int pTimeOut) {
        LogUtil.d("UsbCore.readDataAsync == > call");
        if (mUsbDeviceConnection == null || mInEndpoint == null || mIsReceivingData) return;
        CfSdk.sSdkTp.submit(() -> {
            long currentTimeMillis = 0;
            mIsReceivingData = true;
            while (mIsReceivingData) {
                Arrays.fill(sBuffer, (byte) 0);
                if (mUsbDeviceConnection.bulkTransfer(mInEndpoint, sBuffer, sBuffer.length, pTimeOut) <= 0) {
                    SystemClock.sleep(5);
                    continue;
                }
                if (mIReadDataCallback == null) continue;
                mIReadDataCallback.onDataBack(Arrays.copyOf(sBuffer, sBuffer.length));
                if (System.currentTimeMillis() - currentTimeMillis > 1000) {
                    //控制日志输出频率，一秒输入一次
                    LogUtil.d("usb receive data thread no." + Thread.currentThread().getName());
                    currentTimeMillis = System.currentTimeMillis();
                }
            }
        });
    }

    /**
     * 设置usb设备数据回调，置空则为注销回调
     *
     * @param pIReadDataCallback
     */
    public void setIReadDataCallback(IReadDataCallback pIReadDataCallback) {
        LogUtil.d("UsbCore.setIReadDataCallback == > call");
        mIReadDataCallback = pIReadDataCallback;
    }

    /**
     * 释放usb设备
     */
    public void release() {
        LogUtil.d("UsbCore.release == > call");
        mIsReceivingData = false;
        if (mUsbDeviceConnection == null) return;
        if (mInterface != null) {
            mUsbDeviceConnection.releaseInterface(mInterface);
        }
        mUsbDeviceConnection.close();
        mUsbDeviceConnection = null;
    }
}
