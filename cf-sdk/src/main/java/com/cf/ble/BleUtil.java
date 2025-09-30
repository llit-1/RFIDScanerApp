package com.cf.ble;

import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.os.ParcelUuid;
import android.util.SparseArray;

import com.cf.zsdk.uitl.FormatUtil;

import java.util.List;

public class BleUtil {

    /**
     * 广播包中包含若干个广播数据单元，广播数据单元也称为 AD Structure。
     * 广播数据单元 = 长度值Length + AD type + AD Data。
     * 长度值Length只占一个字节，并且位于广播数据单元的第一个字节。
     *
     * @param pData ble蓝牙广播数据
     * @return 返回长度为2的company id的byte数组
     */
    public static byte[] getCompanyId(byte[] pData) {
        int index = 0;
        //假设公司ID为两个字节的byte数组
        byte[] data = new byte[]{0x00, 0x00};
        for (int i = 0; i < pData.length; i++) {
            if (index >= pData.length) break;
            //len
            int lenByte = pData[index];
            //长度位为零，停止操作
            if (lenByte == 0) break;
            //ad type
            byte typeByte = pData[index + 1];
            //ad data
            data = new byte[lenByte - 1];
            for (int i1 = 0; i1 < data.length; i1++) {
                data[i1] = pData[index + 2 + i1];
            }
            index += lenByte + 1;
            //厂商定义数据标识,标识后边的两个字节为厂商id，这里偷懒直接认为整个数据就是厂商id
            if (typeByte == (byte) 0xff) {
                break;
            }
        }
        return data;
    }

    /**
     * 判断是否为创方公司的设备
     */
    public static boolean isCfDevice(byte[] pBleBroadcastRawData) {
        byte[] companyId = getCompanyId(pBleBroadcastRawData);
        String companyIdStr = FormatUtil.bytesToHexStr(companyId);
        return "2795".equals(companyIdStr);
    }

    public static boolean isDeviceConnectable(ScanResult scanResult) {
        if (scanResult == null || scanResult.getScanRecord() == null) {
            return false;
        }

        ScanRecord scanRecord = scanResult.getScanRecord();

        List<ParcelUuid> serviceData = scanRecord.getServiceUuids();
        if (serviceData != null && !serviceData.isEmpty()) {
            return true;
        }

        SparseArray<byte[]> partialServiceData = scanRecord.getManufacturerSpecificData();
        if (partialServiceData != null && partialServiceData.size() > 0) {
            return true;
        }

        return false;
    }
}


