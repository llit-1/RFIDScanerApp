package com.cf.beans;

import java.io.Serializable;

/**
 * 设备名称
 */
public class DeviceNameBean extends GeneralBean implements Serializable {
    /**
     * 操作类型
     * 0x01：设置
     * 0x02：读取
     */
    public byte mOption;
    /**
     * 设备名称
     */
    public String mDeviceName;
}
