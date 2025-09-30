package com.cf.beans;

import androidx.annotation.NonNull;

import java.io.Serializable;

/**
 * 键盘状态类
 */
public class KeyStateBean implements Serializable {

    /**
     * 按键状态
     * 0x01：开始；
     * 0x02：结束；
     */
    public byte mKeyState;

    @NonNull
    @Override
    public String toString() {
        return "KeyStateBean{" + "mKeyState=" + mKeyState + '}';
    }
}
