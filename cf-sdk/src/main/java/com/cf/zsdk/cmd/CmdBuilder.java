package com.cf.zsdk.cmd;

import android.text.TextUtils;

import com.cf.beans.AllParamBean;
import com.cf.beans.PermissionParamBean;
import com.cf.zsdk.uitl.FormatUtil;

/**
 * 用于生成相应操作的指令
 */
public class CmdBuilder {

    /**
     * 获取初始化模块指令
     *
     * @return
     */
    public static byte[] buildModuleInitCmd() {
        byte[] cmd = new byte[7];
        cmd[0] = (byte) 0xCF;
        cmd[1] = (byte) 0xFF;
        cmd[2] = 0x00;
        cmd[3] = 0x50;
        cmd[4] = 0x00;
        return setCrc16Cal(cmd);
    }

    /**
     * 获取恢复出厂设置指令
     *
     * @return
     */
    public static byte[] buildRebootCmd() {
        byte[] cmd = new byte[7];
        cmd[0] = (byte) 0xCF;
        cmd[1] = (byte) 0xFF;
        cmd[2] = 0x00;
        cmd[3] = 0x52;
        cmd[4] = 0x00;
        return setCrc16Cal(cmd);
    }

    /**
     * 获取设置射频输出功率指令
     *
     * @param pPower 输出功率，单位为：dBm，取值范围为：[0, 26]dBm，大于26 dBm均为26dBm。
     * @param pResv  系统预留字段，默认0x00；
     * @return
     */
    public static byte[] buildSetPwrCmd(byte pPower, byte pResv) {
        byte[] cmd = new byte[9];
        cmd[0] = (byte) 0xCF;
        cmd[1] = (byte) 0xFF;
        cmd[2] = 0x00;
        cmd[3] = 0x53;
        cmd[4] = 0x02;
        cmd[5] = pPower;
        cmd[6] = pResv;
        return setCrc16Cal(cmd);
    }

    /**
     * 设置/读取 模块支持的 RF 协议标准
     *
     * @param pOption 命令控制选项
     *                0x01：设置，其后接1Byte长度的RFID ；
     *                0x02：读取，其后不接RFID；
     *                其他值：无效；
     * @param pRfid   协议选项，0x00：ISO 18000-6C； 0x01：GB/T 29768；0x02：GJB 7377.1；当前仅支持 ISO 18000-6C。
     * @return
     */
    public static byte[] buildSetOrGetRfidCmd(byte pOption, byte pRfid) {
        byte[] cmd = new byte[9];
        cmd[0] = (byte) 0xCF;
        cmd[1] = (byte) 0xFF;
        cmd[2] = 0x00;
        cmd[3] = 0x59;
        cmd[4] = 0x02;
        cmd[5] = pOption;
        cmd[6] = pRfid;
        return setCrc16Cal(cmd);
    }

    /**
     * 获取设置所有可配置参数指令
     *
     * @param pBean 可配置参数bean类
     * @return
     */
    public static byte[] buildSetAllParamCmd(AllParamBean pBean) {
        byte[] cmd = new byte[32];
        cmd[0] = (byte) 0xCF;
        cmd[1] = (byte) 0xFF;
        cmd[2] = 0x00;
        cmd[3] = 0x71;
        cmd[4] = 0x19;
        cmd[5] = pBean.mAddr;
        cmd[6] = pBean.mRFIDPRO;
        cmd[7] = pBean.mWorkMode;
        cmd[8] = pBean.mInterface;
        cmd[9] = pBean.mBaudrate;
        cmd[10] = pBean.mWGSet;
        cmd[11] = pBean.mAnt;
        cmd[12] = pBean.mRfidFreq.mREGION;
        cmd[13] = pBean.mRfidFreq.mSTRATFREI[0];
        cmd[14] = pBean.mRfidFreq.mSTRATFREI[1];
        cmd[15] = pBean.mRfidFreq.mSTRATFRED[0];
        cmd[16] = pBean.mRfidFreq.mSTRATFRED[1];
        cmd[17] = pBean.mRfidFreq.mSTEPFRE[0];
        cmd[18] = pBean.mRfidFreq.mSTEPFRE[1];
        cmd[19] = pBean.mRfidFreq.mCN;
        cmd[20] = pBean.mRfidPower;
        cmd[21] = pBean.mInquiryArea;
        cmd[22] = pBean.mQValue;
        cmd[23] = pBean.mSession;
        cmd[24] = pBean.mAcsAddr;
        cmd[25] = pBean.mAcsDataLen;
        cmd[26] = pBean.mFilterTime;
        cmd[27] = pBean.mTriggerTime;
        cmd[28] = pBean.mBuzzerTime;
        cmd[29] = pBean.mPollingInterval;
        return setCrc16Cal(cmd);
    }

    /**
     * 获取所有可配置参数
     *
     * @return
     */
    public static byte[] buildGetAllParamCmd() {
        byte[] cmd = new byte[7];
        cmd[0] = (byte) 0xCF;
        cmd[1] = (byte) 0xFF;
        cmd[2] = 0x00;
        cmd[3] = 0x72;
        cmd[4] = 0x00;
        return setCrc16Cal(cmd);
    }

    /**
     * 获取电池电量指令
     *
     * @return
     */
    public static byte[] buildGetBatteryCapacityCmd() {
        byte[] cmd = new byte[7];
        cmd[0] = (byte) 0xCF;
        cmd[1] = (byte) 0xFF;
        cmd[2] = 0x00;
        cmd[3] = (byte) 0x83;
        cmd[4] = 0x00;
        return setCrc16Cal(cmd);
    }

    /**
     * 设置/获取蓝牙设备名称指令
     *
     * @param pOption 命令控制选项
     *                0x01：设置，其后接1Byte长度的RFID ；
     *                0x02：读取，其后不接RFID；
     *                其他值：无效；
     * @param pBtName 蓝牙名称
     * @return
     */
    public static byte[] buildSetOrGetBtNameCmd(byte pOption, String pBtName) {
        byte[] bytes;
        if (TextUtils.isEmpty(pBtName)) {
            bytes = new byte[0];
        } else {
            byte[] bytes1 = pBtName.getBytes();
            if (bytes1.length > 20) {
                byte[] bytes2 = new byte[20];
                System.arraycopy(bytes1, 0, bytes2, 0, bytes2.length);
                bytes = bytes2;
            } else {
                bytes = bytes1;
            }
        }
        byte[] cmd = new byte[8 + bytes.length];
        cmd[0] = (byte) 0xCF;
        cmd[1] = (byte) 0xFF;
        cmd[2] = 0x00;
        cmd[3] = (byte) 0x86;
        cmd[4] = (byte) (bytes.length + 1);
        cmd[5] = pOption;
        System.arraycopy(bytes, 0, cmd, 6, bytes.length);
        return setCrc16Cal(cmd);
    }

    /**
     * 构建设置蓝牙输出模式指令
     *
     * @param pMode 0x00：蓝牙HID输出；0x01：蓝牙透传输出
     * @return
     */
    public static byte[] buildSetOutputModeCmd(byte pMode) {
        byte[] cmd = new byte[9];
        cmd[0] = (byte) 0xCF;
        cmd[1] = (byte) 0xFF;
        cmd[2] = 0x00;
        cmd[3] = (byte) 0x88;
        cmd[4] = 0x02;
        cmd[5] = 0x01;
        cmd[6] = pMode;
        return setCrc16Cal(cmd);
    }

    /**
     * 构建获取蓝牙输出模式指令
     *
     * @return
     */
    public static byte[] buildGetOutputModeCmd() {
        byte[] cmd = new byte[8];
        cmd[0] = (byte) 0xCF;
        cmd[1] = (byte) 0xFF;
        cmd[2] = 0x00;
        cmd[3] = (byte) 0x88;
        cmd[4] = 0x01;
        cmd[5] = 0x02;
        return setCrc16Cal(cmd);
    }

    /**
     * 该命令上报按键动作开始结束状态。
     *
     * @param pKeyState 0x01：开始；
     *                  0x02：结束；
     *                  其它值：无效。
     * @return
     */
    public static byte[] buildReportKeyStateCmd(byte pKeyState) {
        byte[] cmd = new byte[8];
        cmd[0] = (byte) 0xCF;
        cmd[1] = (byte) 0x00;
        cmd[2] = 0x00;
        cmd[3] = (byte) 0x89;
        cmd[4] = 0x01;
        cmd[5] = 0x02;
        cmd[6] = pKeyState;
        return setCrc16Cal(cmd);
    }

    /**
     * 设置设备读取模式。
     *
     * @param pReadMode 扫描使能参数，1Byte。
     *                  0x01：扫描头模式 ；
     *                  0x00：RFID模式；
     *                  其他值：无效；
     * @param pRecev    保留，7Bytes
     * @return
     */
    public static byte[] buildSetReadModeCmd(byte pReadMode, byte[] pRecev) {
        byte[] cmd = new byte[16];
        cmd[0] = (byte) 0xCF;
        cmd[1] = (byte) 0xFF;
        cmd[2] = 0x00;
        cmd[3] = (byte) 0x8E;
        cmd[4] = 0x09;
        cmd[5] = 0x01;
        cmd[6] = pReadMode;
        System.arraycopy(pRecev, 0, cmd, 7, pRecev.length);
        return setCrc16Cal(cmd);
    }

    /**
     * 获取设备读取模式
     *
     * @return
     */
    public static byte[] buildGetReadModeCmd() {
        byte[] cmd = new byte[8];
        cmd[0] = (byte) 0xCF;
        cmd[1] = (byte) 0xFF;
        cmd[2] = 0x00;
        cmd[3] = (byte) 0x8E;
        cmd[4] = 0x01;
        cmd[5] = 0x02;
        return setCrc16Cal(cmd);
    }

    /**
     * @param pInvType  盘点方式：
     *                  0x00：按时间盘点标签，在执行指定时间后停止盘点或接收到停止盘点命令后停止盘点；
     *                  0x01: 按照循环次数盘点，在执行指定次数的轮询后或者接到停止盘点指令后停止盘点；
     * @param pInvParam 盘点方式参数：
     *                  1.若InvType为0x00：
     *                  InvParam表示盘点时间，单位为：秒，如果该值为0，则表示持续盘点标签，直到接收到停止盘点命令；
     *                  2.若InvType为0x01：
     *                  InvParam表示盘点次数，单位为：次，该值必须大于0；
     * @return
     */
    public static byte[] buildInventoryISOContinueCmd(byte pInvType, int pInvParam) {
        byte[] cmd = new byte[12];
        cmd[0] = (byte) 0xCF;
        cmd[1] = (byte) 0xFF;
        cmd[2] = 0x00;
        cmd[3] = (byte) 0x01;
        cmd[4] = 0x05;
        cmd[5] = pInvType;
        byte[] bytes = FormatUtil.intToByteArray(pInvParam);
        System.arraycopy(bytes, 0, cmd, 6, bytes.length);
        return setCrc16Cal(cmd);
    }

    /**
     * 停止盘点
     *
     * @return
     */
    public static byte[] buildStopInventoryCmd() {
        byte[] cmd = new byte[7];
        cmd[0] = (byte) 0xCF;
        cmd[1] = (byte) 0xFF;
        cmd[2] = 0x00;
        cmd[3] = (byte) 0x02;
        cmd[4] = 0x00;
        return setCrc16Cal(cmd);
    }

    /**
     * 获取设备信息
     *
     * @return
     */
    public static byte[] buildGetDeviceInfoCmd() {
        byte[] cmd = new byte[7];
        cmd[0] = (byte) 0xCF;
        cmd[1] = (byte) 0xFF;
        cmd[2] = 0x00;
        cmd[3] = (byte) 0x70;
        cmd[4] = 0x00;
        return setCrc16Cal(cmd);
    }

    /**
     * 构建选择标签指令
     *
     * @param pMask
     * @return
     */
    public static byte[] buildSelectMaskCmd(byte[] pMask) {
        byte[] cmd = new byte[10 + pMask.length];
        cmd[0] = (byte) 0xCF;
        cmd[1] = (byte) 0xFF;
        cmd[2] = 0x00;
        cmd[3] = (byte) 0x07;
        cmd[4] = (byte) (3 + pMask.length);
        cmd[5] = 0x00;
        cmd[6] = 0x00;
        cmd[7] = (byte) (pMask.length * 8);
        System.arraycopy(pMask, 0, cmd, 8, pMask.length);
        return setCrc16Cal(cmd);
    }


    /**
     * 读取标签数据
     *
     * @param pAccPwd    访问口令，用于标签进入安全态，默认0x00000000；
     * @param pMemBank   所要读取标签的存储区，值列表如下：
     *                   0x00：Reserved； 0x01：EPC； 0x02：TID； 0x03：User；
     * @param pWordPtr   指向逻辑存储区的读取起始地址(字)；
     * @param pWordCount 需要读取的字个数，不能为0，默认为4，取值范围[1,120]
     * @return
     */
    public static byte[] buildReadISOTagCmd(byte[] pAccPwd, byte pMemBank, byte[] pWordPtr, byte pWordCount) {
        byte[] cmd = new byte[16];
        cmd[0] = (byte) 0xCF;
        cmd[1] = (byte) 0xFF;
        cmd[2] = 0x00;
        cmd[3] = (byte) 0x03;
        cmd[4] = 0x09;
        cmd[5] = 0x00;
        System.arraycopy(pAccPwd, 0, cmd, 6, pAccPwd.length);
        cmd[10] = pMemBank;
        System.arraycopy(pWordPtr, 0, cmd, 11, pWordPtr.length);
        cmd[13] = pWordCount;
        return setCrc16Cal(cmd);
    }

    /**
     * 写入标签数据
     *
     * @param pAccPwd    访问口令，用于标签进入安全态，默认0x00000000；
     * @param pMemBank   所要读取标签的存储区，值列表如下：
     *                   0x00：Reserved； 0x01：EPC； 0x02：TID； 0x03：User；
     * @param pWordPtr   指向逻辑存储区的读取起始地址（字）；
     * @param pWordCount 需要写入标签的数据字个数(1个字为两个字节)，必须大于0；
     * @param pData      需要写入标签的数据，长度必须为字的整数倍，长度为1 ~ WordCount个字
     * @return
     */
    public static byte[] buildWriteISOTagCmd(byte[] pAccPwd, byte pMemBank, byte[] pWordPtr, byte pWordCount, byte[] pData) {
        byte[] cmd = new byte[16 + pData.length];
        cmd[0] = (byte) 0xCF;
        cmd[1] = (byte) 0xFF;
        cmd[2] = 0x00;
        cmd[3] = (byte) 0x04;
        cmd[4] = (byte) (9 + pData.length);
        cmd[5] = 0x00;
        System.arraycopy(pAccPwd, 0, cmd, 6, pAccPwd.length);
        cmd[10] = pMemBank;
        System.arraycopy(pWordPtr, 0, cmd, 11, pWordPtr.length);
        cmd[13] = pWordCount;
        System.arraycopy(pData, 0, cmd, 14, pData.length);
        return setCrc16Cal(cmd);
    }

    /**
     * 锁定标签数据
     *
     * @param pAccPwd 访问口令，用于标签进入安全态，默认0x00000000
     * @param pArea   需要锁定的区域，值列表如下：
     *                0x00：灭活密码区；0x01：访问密码区； 0x02：EPC； 0x03：TID； 0x04：User；
     * @param pAction 锁定操作类型，值列表如下：
     *                0x00：开放； 0x01：永久开放； 0x02：锁定； 0x03：永久锁定；
     */
    public static byte[] buildLockISOTagCmd(byte[] pAccPwd, byte pArea, byte pAction) {
        byte[] cmd = new byte[13];
        cmd[0] = (byte) 0xCF;
        cmd[1] = (byte) 0xFF;
        cmd[2] = 0x00;
        cmd[3] = (byte) 0x05;
        cmd[4] = 0x06;
        System.arraycopy(pAccPwd, 0, cmd, 5, pAccPwd.length);
        cmd[9] = pArea;
        cmd[10] = pAction;
        return setCrc16Cal(cmd);
    }

    /**
     * 灭活标签
     *
     * @param pKLENIPwd
     * @return
     */
    public static byte[] buildKlenlISOTagCmd(byte[] pKLENIPwd) {
        byte[] cmd = new byte[11];
        cmd[0] = (byte) 0xCF;
        cmd[1] = (byte) 0xFF;
        cmd[2] = 0x00;
        cmd[3] = (byte) 0x06;
        cmd[4] = 0x04;
        System.arraycopy(pKLENIPwd, 0, cmd, 5, pKLENIPwd.length);
        return setCrc16Cal(cmd);
    }

    /**
     * 获取权限参数
     *
     * @return
     */
    public static byte[] buildGetPermissionParamCmd() {
        byte[] cmd = new byte[8];
        cmd[0] = (byte) 0xCF;
        cmd[1] = (byte) 0xFF;
        cmd[2] = 0x00;
        cmd[3] = (byte) 0x76;
        cmd[4] = 0x01;
        cmd[5] = 0x02;
        return setCrc16Cal(cmd);
    }

    /**
     * 设置权限参数
     *
     * @param pBean
     * @return
     */
    public static byte[] buildSetPermissionParamCmd(PermissionParamBean pBean) {
        byte[] cmd = new byte[48];
        cmd[0] = (byte) 0xCF;
        cmd[1] = (byte) 0xFF;
        cmd[2] = 0x00;
        cmd[3] = (byte) 0x76;
        cmd[4] = 0x29;
        cmd[5] = 0x01;
        cmd[6] = pBean.mCodeEN;
        System.arraycopy(pBean.mCodes, 0, cmd, 7, pBean.mCodes.length);
        cmd[11] = 0x01;
        cmd[12] = pBean.mStartAdd;
        cmd[13] = pBean.mMaskLen;
        System.arraycopy(pBean.mMaskData, 0, cmd, 14, pBean.mMaskData.length);
        cmd[45] = pBean.mMaskCondition;
        return setCrc16Cal(cmd);
    }

    /**
     * crc校验
     *
     * @param pCmd
     * @return
     */
    public static byte[] setCrc16Cal(byte[] pCmd) {
        int crc = 0xFFFF;
        for (int i = 0; i < pCmd.length - 2; i++) {
            if (pCmd[i] < 0) {
                crc ^= (int) pCmd[i] + 256;
            } else {
                crc ^= pCmd[i];
            }
            for (int j = 8; j != 0; j--) {
                if ((crc & 0x0001) != 0) {
                    crc >>= 1;
                    crc ^= 0x8408;
                } else {
                    crc >>= 1;
                }
            }
        }
        //高八位的CRC校验值
        pCmd[pCmd.length - 2] = (byte) ((crc >> 8) & 0xff);
        //低八位的CRC校验值
        pCmd[pCmd.length - 1] = (byte) ((byte) crc & 0xff);
        return pCmd;
    }


}
