package com.cf.beans;

import androidx.annotation.NonNull;

import java.io.Serializable;

/**
 * 输出模式
 */
public class OutputModeBean extends GeneralBean implements Serializable {
    /**
     * 操作类型
     * 0x01 设置
     * 0x02 获取
     */
    public byte mOption;
    /**
     * 输出模式
     * 0x00：蓝牙HID输出
     * 0x01：蓝牙透传输出
     */
    public byte mMode = -1;

    @NonNull
    @Override
    public String toString() {
        return "OutputModeBean{" + "mOption=" + mOption + ", mMode=" + mMode + ", mCode=" + mStatus + ", mMsg='" + mMsg + '\'' + '}';
    }
}
