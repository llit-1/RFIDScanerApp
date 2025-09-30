package com.cf.beans;

import androidx.annotation.NonNull;

import java.io.Serializable;

/**
 * 设置参数返回的通用类
 */
public class GeneralBean implements Serializable {

    /**
     * 状态码
     */
    public int mStatus;
    /**
     * 状态信息
     */
    public String mMsg;

    @NonNull
    @Override
    public String toString() {
        return "GeneralBean{" + "mCode=" + mStatus + ", mMsg='" + mMsg + '\'' + '}';
    }
}
