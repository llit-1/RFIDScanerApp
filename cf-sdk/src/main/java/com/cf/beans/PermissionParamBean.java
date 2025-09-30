package com.cf.beans;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Arrays;

/**
 * 权限参数
 */
public class PermissionParamBean extends GeneralBean implements Serializable {
    /**
     * 命令控制选项，0x01设置，0x02 读取，其它值无效。
     */
    public byte mOption;
    /**
     * :密码功能使能参数，长度1字节。0x01启用，0x00 不启用。默认0x00。
     */
    public byte mCodeEN;
    /**
     * 标签的访问密码，长度4字节。默认[0x00,0x00,0x00,0x00]。
     */
    public byte[] mCodes;
    /**
     * 掩码功能使能参数，长度1字节。0x01启用，0x00 不启用。默认0x00。
     */
    public byte mMaskEN;
    /**
     * 掩码起始地址，长度1字节，单位字节。默认0x00。
     */
    public byte mStartAdd;
    /**
     * 掩码长度，长度1字节，单位字节，最大12。默认 0x00。
     */
    public byte mMaskLen;
    /**
     * 掩码数据，长度 31字节，掩码长度不足 31字节时，后面字节数据以0补充。
     * 默认数据都为0x00。
     */
    public byte[] mMaskData;
    /**
     * :掩码条件，长度1字节。
     * 0x00:密码或者掩码符合;
     * 0x01:密码和掩码同时符合。
     * 默认0x00。
     */
    public byte mMaskCondition;

    @NonNull
    @Override
    public String toString() {
        return "PermissionParamBean{" + "mOption=" + mOption + ", mCodeEN=" + mCodeEN + ", mCodes=" + Arrays.toString(mCodes) + ", mMaskEN=" + mMaskEN + ", mStartAdd=" + mStartAdd + ", mMaskLen=" + mMaskLen + ", mMaskData=" + Arrays.toString(mMaskData) + ", mMaskCondition=" + mMaskCondition + ", mStatus=" + mStatus + ", mMsg='" + mMsg + '\'' + '}';
    }
}
