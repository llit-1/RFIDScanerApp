package com.cf.beans;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Arrays;

/**
 * 标签操作
 */
public class TagOperationBean extends GeneralBean implements Serializable {
    /**
     * 标签状态
     */
    public byte mTagStatus;
    /**
     * 从哪个天线端口接收到的标签数据，值范围为：1~4，分别表示1~4号天线
     */
    public byte mAntenna;
    /**
     * 2个字节
     * 标签响应数据中的CRC数据
     */
    public byte[] mCRC;
    /**
     * 2个字节
     * 标签响应数据中的PC数据
     */
    public byte[] mPC;
    /**
     * 标签的EPC号码长度（字节）
     */
    public byte mEPCLen;
    /**
     * 标签的EPC号码；
     */
    public byte[] mEPCNum;

    //以下两个字段，只有读标签时才有值
    /**
     * 成功读取到的标签数据字个数；
     */
    public byte mWordCount;
    /**
     * N个字节
     * 成功读取到的标签数据，长度为WordCount×2个字节
     */
    public byte[] mData;

    @NonNull
    @Override
    public String toString() {
        return "TagOperationBean{ mTagStatus=" + mTagStatus + ", mAntenna=" + mAntenna + ", mCRC=" + Arrays.toString(mCRC) + ", mPC=" + Arrays.toString(mPC) + ", mEPCLen=" + mEPCLen + ", mEPCNum=" + Arrays.toString(mEPCNum) + ", mWordCount=" + mWordCount + ", mData=" + Arrays.toString(mData) + ", mStatus=" + mStatus + ", mMsg='" + mMsg + '\'' + '}';
    }
}
