package com.cf.ble.interfaces;

import android.bluetooth.le.ScanResult;

/**
 * 扫描ble设备回调接口
 */
public interface IBtScanCallback {
    /**
     * 扫描结果
     *
     * @param pResult 扫描结果
     */
    void onBtScanResult(ScanResult pResult);

    /**
     * 扫描失败，默认实现有需要的开发者自行实现
     *
     * @param pErrorCode 错误码
     */
    default void onBtScanFail(int pErrorCode) {
    }
}
