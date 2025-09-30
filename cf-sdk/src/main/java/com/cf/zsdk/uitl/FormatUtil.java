package com.cf.zsdk.uitl;

import android.text.TextUtils;

import java.nio.ByteBuffer;

public class FormatUtil {

    /**
     * hex字符串转byte数组
     *
     * @param pHexStr
     * @return
     */
    public static byte[] hexStrToByteArray(String pHexStr) {
        pHexStr = pHexStr.replace(" ", "");
        int byteNum = pHexStr.length() / 2;
        byte[] bytes = new byte[byteNum];
        for (int i = 0; i < byteNum; i++) {
            String byteStr = pHexStr.substring(i * 2, (i + 1) * 2);
            bytes[i] = (byte) Integer.parseInt(byteStr, 16);
        }
        return bytes;
    }

    public static String hexStrAddSpaceChar(String pString) {
        if (TextUtils.isEmpty(pString)) return "";
        pString = pString.replace(" ", "").trim();
        int length = pString.length();
        StringBuilder builder = new StringBuilder();
        if (length % 2 == 0) {
            for (int parseInt = 0; parseInt < length / 2; parseInt++) {
                String byteStr = pString.substring(parseInt * 2, (parseInt + 1) * 2);
                builder.append(byteStr);
                builder.append(" ");
            }
        } else {
            for (int parseInt = 0; parseInt < length / 2; parseInt++) {
                String byteStr = pString.substring(parseInt * 2, (parseInt + 1) * 2);
                builder.append(byteStr);
                builder.append(" ");
            }
            builder.append(pString.charAt(pString.length() - 1));
        }
        return builder.toString().trim().toUpperCase();
    }

    /**
     * byte数组转hex字符串
     *
     * @param pBytes
     * @return
     */
    public static String bytesToHexStr(byte[] pBytes) {
        if (pBytes == null) return "";
        StringBuilder stringBuilder = new StringBuilder();
        for (byte pByte : pBytes) {
            String format = String.format("%02X", pByte);
            stringBuilder.append(format).append(" ");
        }
        return stringBuilder.toString().trim().toUpperCase();
    }

    /**
     * int值转byte数组
     *
     * @param value
     * @return
     */
    public static byte[] intToByteArray(int value) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(value);
        return buffer.array();
    }

    /**
     * byte数组转int值
     *
     * @param bytes
     * @return
     */
    public static int byteArrayToInt(byte[] bytes) {
        int num = 0;
        for (byte b : bytes) {
            num = (num << 8) + (b & 0xFF);
        }
        return num;
    }
}
