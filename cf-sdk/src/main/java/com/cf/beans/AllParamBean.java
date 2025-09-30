package com.cf.beans;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Arrays;

/**
 * 所有参数bean类
 */
public class AllParamBean extends GeneralBean implements Serializable {
    /**
     * 设备的通信地址，默认为0x00。这个地址不能为0xFF。如果设置为0xFF，则读写模块将返回参数出错信息。
     */
    public byte mAddr;
    /**
     * 设备射频RFID的协议标准规范，0x00：ISO 18000-6C； 0x01：GB/T 29768；0x02：GJB 7377.1；当前仅支持 ISO 18000-6C。
     */
    public byte mRFIDPRO;
    /**
     * 设备的工作模式，默认值0
     * 0	应答模式
     * 1	主动模式
     * 2	触发模式
     */
    public byte mWorkMode;
    /**
     * 设备的通信接口，默认值0x80，具体释义如下：
     * 0x80	RS232
     * 0x40	RS485
     * 0x20	RJ45
     * 0x10	WiFi
     * 0x01	USB
     * 0x02	keyboard
     * 0x04	CDC_COM
     */
    public byte mInterface;
    /**
     * 串口波特率，默认值为4，具体释义如下：
     * 0	9600bps
     * 1	19200 bps
     * 2	38400 bps
     * 3	57600 bps
     * 4	115200 bps
     */
    public byte mBaudrate;
    /**
     * 韦根数据输出接口的配置参数，默认值0x00，具体释义如下：
     * WGSet      Bit7             Bit6     Bit5        Bit4	Bit3	Bit2	Bit1	Bit0
     * Bit位释义   0：关闭韦根输出    0：wg26   0：低位在前   备用    备用     备用     备用     备用
     * -----------1：开启韦根输出    1：wg34   1：高位在前
     */
    public byte mWGSet;
    /**
     * 设备所有的天线号，按位表示选择使用的天线，对应Bit位值为1则表示使用该天线，值为0则表示不使用该天线；
     * 从低位开始，第0位表示1号天线，第1位表示2号天线，以此类推，最多能表示8个天线；
     * 不同模块支持不同个天线，视具体情况而定；
     * 默认值0x01，表示1号天线。
     */
    public byte mAnt;
    /**
     * 设备的RFID频率相关参数，用于选择频段及各频段中的上限频点和下限频点，长度8Bytes
     */
    public RfidFreq mRfidFreq;
    /**
     * 设备的RFID输出功率，单位为：dBm，取值范围为：[0, 33]dBm，其他无效。
     */
    public byte mRfidPower;
    /**
     * 设备要访问标签的存储区区域。
     * 0x00：保留区；
     * 0x01(默认)：EPC存储区；
     * 0x02：TID存储区；
     * 0x03：USER存储区；
     * 0x04：EPC+TID；
     * 0x05：EPC+USER；
     * 0x06：EPC+TID+USER；
     * 其它值保留。若命令中出现了其它值，将返回参数出错的消息。
     */
    public byte mInquiryArea;
    /**
     * 询查EPC标签时使用的初始Q值，
     * Q值的设置应为场内的标签数量约等于2Q。
     * Q值的默认值为4，Q值的范围为0～15，
     * 若命令中出现了其它值，将返回参数出错的消息。
     */
    public byte mQValue;
    /**
     * 询查EPC标签时使用的Session值，
     * 默认为0，取值范围[0，3]，其它值，
     * 将返回参数出错的消息。
     * 0	Session使用S0
     * 1	Session使用S1
     * 2	Session使用S2
     * 3	Session使用S3
     */
    public byte mSession;
    /**
     * 设备要访问标签存储区的起始地址，单位 ：Byte，
     * 默认值：0x00：
     * 访问EPC区时，0x00表示访问EPC区除CRC和PC段的EPC号码段起始地址；
     * 访问其他存储区时，0x00表示该存储区的起始地址。
     */
    public byte mAcsAddr;
    /**
     * 设备要访问标签存储区的数据长度，单位 ：Byte，默认值：0x00.
     */
    public byte mAcsDataLen;
    /**
     * 过滤时间，在读取成功一张标签数据后的该值时间内，
     * 过滤掉有相同数据的标签。单位为：S，
     * 取值范围为：[0, 255]，其他无效；默认值为0，没有过滤。
     */
    public byte mFilterTime;
    /**
     * 设备收到触发信号后的询查时长，单位为：S，默认值为1，取值范围为：[0, 255]，其他无效。
     */
    public byte mTriggerTime;
    /**
     * 设备执行成功后蜂鸣器鸣叫时长，单位为：10ms，
     * 取值范围为：[0, 255]，其他无效；默认为1，
     * 当为0时，表示蜂鸣器不鸣叫。
     */
    public byte mBuzzerTime;
    /**
     * 询查间隔时间，单位为：10ms，取值范围为：[0, 255]，其他无效，默认为1。
     */
    public byte mPollingInterval;

    @NonNull
    @Override
    public String toString() {
        return "AllParamBean{" + "mAddr=" + mAddr + ", mRFIDPRO=" + mRFIDPRO + ", mWorkMode=" + mWorkMode + ", mInterface=" + mInterface + ", mBaudrate=" + mBaudrate + ", mWGSet=" + mWGSet + ", mAnt=" + mAnt + ", mRfidFreq=" + mRfidFreq + ", mRfidPower=" + mRfidPower + ", mInquiryArea=" + mInquiryArea + ", mQValue=" + mQValue + ", mSession=" + mSession + ", mAcsAddr=" + mAcsAddr + ", mAcsDataLen=" + mAcsDataLen + ", mFilterTime=" + mFilterTime + ", mTriggerTime=" + mTriggerTime + ", mBuzzerTime=" + mBuzzerTime + ", mPollingInterval=" + mPollingInterval + '}';
    }

    public static class RfidFreq {
        /**
         * 各国频段范围：
         * 0x00:  用户根据需求自定义；
         * 0x01：US [902.75~927.25]
         * 0x02：Korea [917.1~923.5]
         * 0x03：EU [865.1~868.1]
         * 0x04：JAPAN [952.2~953.6]
         * 0x05：MALAYSIA [919.5~922.5]
         * 0x06：EU3 [865.7~867,5]
         * 0x07：CHINA_BAND1 [840.125~844.875]
         * 0x08：CHINA_BAND2 [920.125~924.875]
         */
        public byte mREGION;
        /**
         * 长度两位
         * 兆赫兹起始频率的整数部分；如920.125MHz，STRATFREI = 920 = 0x0398，高字节=0x03，低字节=0x98；
         */
        public byte[] mSTRATFREI;
        /**
         * 长度两位
         * 兆赫兹起始频率的小数部分；如 920.125MHz，STRATFRED =125，高字节=0x00，低字节=0x7D
         */
        public byte[] mSTRATFRED;
        /**
         * 长度两位
         * 频率步进（KHz），需参考各频段计算公式；如125KHz，STEPFRE =125，高字节=0x00，低字节=0x7D；
         */
        public byte[] mSTEPFRE;
        /**
         * 信道数；
         */
        public byte mCN;

        @NonNull
        @Override
        public String toString() {
            return "RfidFreq{" + "mREGION=" + mREGION + ", mSTRATFREI=" + Arrays.toString(mSTRATFREI) + ", mSTRATFRED=" + Arrays.toString(mSTRATFRED) + ", mSTEPFRE=" + Arrays.toString(mSTEPFRE) + ", mCN=" + mCN + '}';
        }
    }
}
