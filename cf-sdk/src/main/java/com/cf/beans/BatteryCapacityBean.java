package com.cf.beans;

import java.io.Serializable;

/**
 * 电池电量信息bean
 */
public class BatteryCapacityBean extends GeneralBean implements Serializable {
    /**
     * 电池电量百分比
     */
    public byte mBatteryCapacity;
}
