package com.cf.beans;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

/**
 * 标签信息类
 */
public class TagInfoBean extends GeneralBean implements Serializable {
    /**
     * 标签ACK响应的RSSI，单位为dBm，带符号数，负数使用补码格式；
     */
    public int mRSSI;
    /**
     * 从哪个天线端口接收到的标签数据，值范围为：1~4，分别表示1~4号天线
     */
    public int mAntenna;
    /**
     * 从哪个信道接收到的标签数据，值从0开始，0表示0信道，1表示1信道，以此类推；
     */
    public int mChannel;
    /**
     * 标签的EPC号码长度（字节）；
     */
    public int mEPCLen;
    /**
     * 标签的EPC号码；
     */
    public byte[] mEPCNum;

    @Override
    public boolean equals(Object pO) {
        if (this == pO) return true;
        if (pO == null || getClass() != pO.getClass()) return false;
        TagInfoBean bean = (TagInfoBean) pO;
        return mAntenna == bean.mAntenna && mChannel == bean.mChannel && mEPCLen == bean.mEPCLen && Objects.deepEquals(mEPCNum, bean.mEPCNum);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mStatus, mRSSI, mAntenna, mChannel, mEPCLen, Arrays.hashCode(mEPCNum));
    }

    @NonNull
    @Override
    public String toString() {
        return "TagInfoBean{" + "mRSSI=" + mRSSI + ", mAntenna=" + mAntenna + ", mChannel=" + mChannel + ", mEPCLen=" + mEPCLen + ", mEPCNum=" + Arrays.toString(mEPCNum) + ", mStatus=" + mStatus + ", mMsg='" + mMsg + '\'' + '}';
    }
}
