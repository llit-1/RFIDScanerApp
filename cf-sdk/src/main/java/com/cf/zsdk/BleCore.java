package com.cf.zsdk;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;

import com.cf.beans.CmdData;
import com.cf.ble.interfaces.IBleDisConnectCallback;
import com.cf.ble.interfaces.IBtScanCallback;
import com.cf.ble.interfaces.IConnectDoneCallback;
import com.cf.ble.interfaces.IOnNotifyCallback;
import com.cf.zsdk.cmd.CmdHandler;
import com.cf.zsdk.uitl.FormatUtil;
import com.cf.zsdk.uitl.LogUtil;

import java.lang.reflect.Method;
import java.util.UUID;

public class BleCore {

    private BluetoothManager mBm;
    private volatile IOnNotifyCallback mOnNotifyCallback;
    private volatile IBleDisConnectCallback mIBleDisConnectCallback;
    private IConnectDoneCallback mIConnectDoneCallback;
    private BluetoothGatt mGatt;
    private BluetoothDevice mBluetoothDevice;
    private boolean mIsConnect = false;
    private IBtScanCallback mIBtScanCallback;

    protected BleCore() {
    }

    private final ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if (mIBtScanCallback == null || result == null) return;
            mIBtScanCallback.onBtScanResult( result);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            if (mIBtScanCallback == null) return;
            mIBtScanCallback.onBtScanFail(errorCode);
        }
    };

    private final BluetoothGattCallback mBtGattCallback = new BluetoothGattCallback() {


        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            LogUtil.d("BleCore.onConnectionStateChange result of connect operation == > " + status);
            LogUtil.d("BleCore.onConnectionStateChange status of connect state == > " + newState);

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mIsConnect = true;
                boolean b = gatt.discoverServices();
                LogUtil.d("BleCore.onConnectionStateChange == > run discover services operation == > " + b);
                mGatt = gatt;
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mIsConnect = false;
                disconnectedDevice();
                if (mIBleDisConnectCallback != null) {
                    mIBleDisConnectCallback.onBleDisconnect();
                }
            } else {
                LogUtil.e("BleCore.onConnectionStateChange unknown connect state == > " + newState);
                mIsConnect = false;
                disconnectedDevice();
                if (mIBleDisConnectCallback != null) {
                    mIBleDisConnectCallback.onBleDisconnect();
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            LogUtil.d("BleCore.onServicesDiscovered status of discover services == > " + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //直接申请最大值，让系统根据实际需求，实现动态调整
                gatt.requestMtu(512);
            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            LogUtil.d("BleCore.onMtuChanged status of change mtu size == > " + status);
            if (mIConnectDoneCallback != null) {
                mIConnectDoneCallback.onConnectDone(true);
                mIConnectDoneCallback = null;
            }
        }


        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            byte[] value = characteristic.getValue();
            LogUtil.d("ble answer == > " + FormatUtil.bytesToHexStr(value));

            CmdData cmdData = new CmdData();
            int cmdType = CmdHandler.getCmdType(value);
            if (cmdType == -1) return;
            Object obj = CmdHandler.handleCmd(cmdType, value);
            cmdData.setData(obj);

            //当前线程，持有接口副本，解决回调空指针问题
            IOnNotifyCallback onNotifyCallback = mOnNotifyCallback;
            if (onNotifyCallback != null) {
                onNotifyCallback.onNotify(cmdType, cmdData);
                onNotifyCallback.onNotify(value);
            }
        }
    };


    /**
     * 初始化
     *
     * @param pContext 上下文
     */
    public void init(Context pContext) {
        LogUtil.d("BleCore.init == > call");
        mBm = (BluetoothManager) pContext.getSystemService(Context.BLUETOOTH_SERVICE);
    }

    /**
     * 是否支持蓝牙模块
     *
     * @return true：表示支持，反之
     */
    public boolean isSupportBt() {
        LogUtil.d("BleCore.isSupportBt == > call");
        BluetoothAdapter adapter = mBm.getAdapter();
        return adapter != null;
    }

    /**
     * 蓝牙是否打开
     *
     * @return true：表示已经打开，反之
     */
    public boolean isEnabled() {
        LogUtil.d("BleCore.isEnabled == > call");
        return mBm.getAdapter().isEnabled();
    }

    /**
     * 是否在连接
     *
     * @return true表示在连接，false断开连接
     */
    public boolean isConnect() {
        return mIsConnect;
    }


    /**
     * 传统蓝牙打开方式
     * public boolean openBtV1() {
     * return mBm.getAdapter().enable();
     * }
     * <p>
     * Android 13后打开蓝牙界面接收，蓝牙打开状态信息
     * <p>
     * public void openBtV2(Activity pActivity, int pRequestCode) {
     * if (!mBm.getAdapter().isEnabled()) {
     * Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
     * pActivity.startActivityForResult(enableBtIntent, pRequestCode);
     * }
     * }
     */

    /**
     * 开始扫描ble设备
     *
     * @param pIBtScanCallback 扫描结果回调接口，扫描到的ble设备，通过该接口返回
     */
    public void startScan(IBtScanCallback pIBtScanCallback) {
        LogUtil.e("BleCore.startScan == > call");
        BluetoothLeScanner bluetoothLeScanner = mBm.getAdapter().getBluetoothLeScanner();
        if (bluetoothLeScanner == null) return;
        mIBtScanCallback = pIBtScanCallback;
        bluetoothLeScanner.startScan(mScanCallback);
    }

    /**
     * 停止扫描ble设备
     */
    public void stopScan() {
        LogUtil.d("Hello", "BleCore.stopScan == > call");
        mIBtScanCallback = null;
        if (mBm == null) return;
        BluetoothAdapter adapter = mBm.getAdapter();
        if (adapter == null) return;
        BluetoothLeScanner bluetoothLeScanner = adapter.getBluetoothLeScanner();
        if (bluetoothLeScanner == null) return;
        bluetoothLeScanner.stopScan(mScanCallback);
    }

    /**
     * 连接设备
     *
     * @param pDevice      要连接的ble设备
     * @param pContext     上下文对象
     * @param pAutoConnect 是否自动连接，true表示断开后自动连接，false表示不自动连接
     * @return 返回 {@link BluetoothGatt}对象
     */
    public BluetoothGatt connectDevice(BluetoothDevice pDevice, Context pContext, boolean pAutoConnect) {
        LogUtil.d("BleCore.connectDevice == > call");
        mBluetoothDevice = pDevice;
        return pDevice.connectGatt(pContext.getApplicationContext(), pAutoConnect, mBtGattCallback);
    }

    /**
     * 获取当前连接的ble device
     *
     * @return
     */
    public BluetoothDevice getConnectedDevice() {
        LogUtil.e("BleCore.getConnectedDevice == > call");
        return mBluetoothDevice;
    }

    /**
     * 打开或关闭通知
     *
     * @param pServiceUuid 服务uuid
     * @param pNotifyUuid  通知uuid
     * @param pOpen        true：打开通知，false：关闭通知
     * @return 返回Boolean值，true表示操作执行成功，false表示操作执行失败
     */
    public boolean setNotifyState(UUID pServiceUuid, UUID pNotifyUuid, boolean pOpen) {
        return setNotifyState(pServiceUuid, pNotifyUuid, pOpen, null);
    }


    /**
     * 打开或关闭通知
     *
     * @param pServiceUuid 服务uuid
     * @param pNotifyUuid  通知uuid
     * @param pOpen        true：打开通知，false：关闭通知
     * @param pCallback    打开结果回调
     * @return 返回Boolean值，true表示操作执行成功，false表示操作执行失败
     */
    public boolean setNotifyState(UUID pServiceUuid, UUID pNotifyUuid, boolean pOpen, IOnNotifyCallback pCallback) {
        LogUtil.e("BleCore.setNotifyState == > call");
        if (pOpen) {
            LogUtil.e("BleCore.setNotifyState == > open notify");
        } else {
            LogUtil.d("BleCore.setNotifyState == > close notify");
        }
        if (mGatt == null) return false;
        BluetoothGattService service = mGatt.getService(pServiceUuid);
        if (service == null) return false;
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(pNotifyUuid);
        if (characteristic == null) return false;
        //要是关闭通知回调，则直接将回调置空
        if (!pOpen) mOnNotifyCallback = null;
        if (mGatt.setCharacteristicNotification(characteristic, pOpen)) {
            for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
                if (pOpen) {
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                } else {
                    descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                }
                boolean b = mGatt.writeDescriptor(descriptor);
                LogUtil.d("set notification result == > " + b);
                if (b && pOpen && mOnNotifyCallback == null) {
                    mOnNotifyCallback = pCallback;
                }
                return b;
            }
        }
        return false;
    }

    /**
     * 设置通知回调，ble设备上报的数据通过IOnNotifyCallback回调，
     * 置空则为注销回调
     *
     * @param pCallback {@link IOnNotifyCallback}
     */
    public void setOnNotifyCallback(IOnNotifyCallback pCallback) {
        mOnNotifyCallback = pCallback;
    }

    /**
     * ble设备断开连接回调，置空则为注销回调
     *
     * @param pIBleDisConnectCallback {@link IBleDisConnectCallback}
     */
    public void setIBleDisConnectCallback(IBleDisConnectCallback pIBleDisConnectCallback) {
        mIBleDisConnectCallback = pIBleDisConnectCallback;
    }

    /**
     * 连接完成回调，置空则为注销回调
     *
     * @param pIConnectDoneCallback {@link IConnectDoneCallback}
     */
    public void setIConnectDoneCallback(IConnectDoneCallback pIConnectDoneCallback) {
        mIConnectDoneCallback = pIConnectDoneCallback;
    }

    /**
     * 写数据
     *
     * @param pServiceUuid        服务uuid
     * @param pCharacteristicUuid 写特征uuid
     * @param pCmd                数据
     * @return true：执行成功，反之
     */
    public boolean writeData(UUID pServiceUuid, UUID pCharacteristicUuid, byte[] pCmd) {
        LogUtil.d("BleCore.writeData == > " + FormatUtil.bytesToHexStr(pCmd));
        if (mGatt == null) return false;
        BluetoothGattService targetService = mGatt.getService(pServiceUuid);
        if (targetService == null) return false;
        BluetoothGattCharacteristic targetCharacteristic = targetService.getCharacteristic(pCharacteristicUuid);
        if (targetCharacteristic == null) return false;
        targetCharacteristic.setValue(pCmd);
        boolean b = mGatt.writeCharacteristic(targetCharacteristic);
        LogUtil.d("BleCore.writeData == > result of write data operation " + b);
        return b;
    }

    /**
     * 读取数据
     *
     * @param pServiceUuid        服务uuid
     * @param pCharacteristicUuid 要读取数据的特征udid
     * @return true：表示操作执行成功，反之
     */
    public boolean readData(UUID pServiceUuid, UUID pCharacteristicUuid) {
        LogUtil.e("BleCore.readData == > call");
        if (mGatt == null) return false;
        BluetoothGattService targetService = mGatt.getService(pServiceUuid);
        if (targetService == null) return false;
        BluetoothGattCharacteristic targetCharacteristic = targetService.getCharacteristic(pCharacteristicUuid);
        if (targetCharacteristic == null) return false;
        boolean b = mGatt.readCharacteristic(targetCharacteristic);
        LogUtil.d("Hello", "BleCore.readData == > result of read data operation " + b);
        return b;
    }

    /**
     * 断开已连接的设备
     */
    public synchronized void disconnectedDevice() {
        LogUtil.d("BleCore.disconnectedDevice == > call");
        if (mGatt != null) {
            try {
                //清空缓存
                Method localMethod = mGatt.getClass().getMethod("refresh");
                localMethod.invoke(mGatt);
            } catch (Exception localException) {
                localException.printStackTrace();
            }
            mGatt.disconnect();
            mGatt.close();
            mGatt = null;
        }
        mBluetoothDevice = null;
    }
}
