package com.cf.zsdk.cmd;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import com.cf.beans.AllParamBean;
import com.cf.beans.BatteryCapacityBean;
import com.cf.beans.DeviceInfoBean;
import com.cf.beans.DeviceNameBean;
import com.cf.beans.GeneralBean;
import com.cf.beans.KeyStateBean;
import com.cf.beans.OutputModeBean;
import com.cf.beans.PermissionParamBean;
import com.cf.beans.RemoteNetParaBean;
import com.cf.beans.TagInfoBean;
import com.cf.beans.TagOperationBean;

import org.jetbrains.annotations.Contract;

import java.nio.charset.Charset;

@Keep
public class CmdHandler {

    @Contract(pure = true)
    public static int getCmdType(@NonNull byte[] pCmd) {
        //判断是否是方创指令，不是直接不处理
        if (pCmd[0] != (byte) 0xCF) return -1;//-1表示非创方指令
        //提取CMD类型字节
        byte b1 = pCmd[2];
        byte b2 = pCmd[3];
        return ((b1 << 8) | (b2 & 0xFF)) & 0xffff;
    }

    /**
     * 判断指令类型
     *
     * @param pCmd
     */
    public static Object handleCmd(int pCmdType, byte[] pCmd) {
        switch (pCmdType) {
            case CmdType.TYPE_INVENTORY:
                return handleInventoryISOContinue(pCmd);
            case CmdType.TYPE_STOP_INVENTORY:
                return handleStopInventory(pCmd);
            case CmdType.TYPE_READ_TAG:
                return handleReadTagCmd(pCmd);
            case CmdType.TYPE_WRITE_TAG:
                return handleWriteTagCmd(pCmd);
            case CmdType.TYPE_LOCK_TAG:
                return handleLockTagCmd(pCmd);
            case CmdType.TYPE_KILL_TAG:
                return handleKillTagCmd(pCmd);
            case CmdType.TYPE_SELECT_MASK:
                return handleSelectMask(pCmd);
            case CmdType.TYPE_MODULE_INIT:
                return handleModuleInit(pCmd);
            case CmdType.TYPE_REBOOT:
                return handleReboot(pCmd);
            case CmdType.TYPE_REMOTE_NET_PARA:
                return handleRemoteNetPara(pCmd);
            case CmdType.TYPE_GET_DEVICE_INFO:
                return handleGetDeviceInfo(pCmd);
            case CmdType.TYPE_SET_ALL_PARAM:
                return handleSetAllParam(pCmd);
            case CmdType.TYPE_GET_ALL_PARAM:
                return handleGetAllParam(pCmd);
            case CmdType.TYPE_GET_OR_SET_PERMISSION:
                return handleGetOrSetPermissionParam(pCmd);
            case CmdType.TYPE_GET_BATTERY_CAPACITY:
                return handleGetBatteryCapacity(pCmd);
            case CmdType.TYPE_SET_OR_GET_BT_NAME:
                return handleSetOrGetBtName(pCmd);
            case CmdType.TYPE_OUT_MODE:
                return handleOutMode(pCmd);
            case CmdType.TYPE_KEY_STATE:
                return handleKeyState(pCmd);
            default:
                return null;
        }

    }

    private static Object handleRemoteNetPara(byte[] pCmd) {
        RemoteNetParaBean bean = new RemoteNetParaBean();
        bean.mStatus = pCmd[5];
        bean.mMsg = "远程网络参数：" + judgeState(pCmd[5]);
        bean.mOption = pCmd[6];

        if (bean.mOption == 0x01) {
            //设置应答
            return bean;
        } else {
            //获取应答
            byte[] bytes = new byte[4];
            System.arraycopy(pCmd, 7, bytes, 0, bytes.length);
            bean.mIpAddr = bytes;

            byte[] bytes1 = new byte[6];
            System.arraycopy(pCmd, 11, bytes1, 0, bytes1.length);
            bean.mMacAddr = bytes1;

            byte[] bytes2 = new byte[2];
            System.arraycopy(pCmd, 17, bytes2, 0, bytes2.length);
            bean.mPort = bytes2;

            byte[] bytes3 = new byte[4];
            System.arraycopy(pCmd, 19, bytes3, 0, bytes3.length);
            bean.mNetMask = bytes3;


            byte[] bytes4 = new byte[4];
            System.arraycopy(pCmd, 23, bytes4, 0, bytes4.length);
            bean.mGateWay = bytes4;

            return bean;
        }

    }


    private static KeyStateBean handleKeyState(byte[] pCmd) {
        KeyStateBean keyStateBean = new KeyStateBean();
        keyStateBean.mKeyState = pCmd[5];
        return keyStateBean;
    }

    private static Object handleKillTagCmd(byte[] pCmd) {

        TagOperationBean bean = new TagOperationBean();
        bean.mStatus = pCmd[5];
        bean.mMsg = "灭活标签：" + judgeState(pCmd[5]);

        if (pCmd[4] == 1 || pCmd[4] == 2) {
            return bean;
        }

        bean.mTagStatus = pCmd[6];
        bean.mAntenna = pCmd[7];
        byte[] crc = new byte[2];
        System.arraycopy(pCmd, 8, crc, 0, crc.length);
        bean.mCRC = crc;
        byte[] pc = new byte[2];
        System.arraycopy(pCmd, 10, pc, 0, pc.length);
        bean.mPC = pc;
        bean.mEPCLen = pCmd[12];
        byte[] epcNum = new byte[bean.mEPCLen];
        System.arraycopy(pCmd, 13, epcNum, 0, epcNum.length);
        bean.mEPCNum = epcNum;
        return bean;
    }

    private static Object handleLockTagCmd(byte[] pCmd) {

        TagOperationBean bean = new TagOperationBean();
        bean.mStatus = pCmd[5];
        bean.mMsg = "锁定标签：" + judgeState(pCmd[5]);

        if (pCmd[4] == 1 || pCmd[4] == 2) {
            return bean;
        }

        bean.mTagStatus = pCmd[6];
        bean.mAntenna = pCmd[7];
        byte[] crc = new byte[2];
        System.arraycopy(pCmd, 8, crc, 0, crc.length);
        bean.mCRC = crc;
        byte[] pc = new byte[2];
        System.arraycopy(pCmd, 10, pc, 0, pc.length);
        bean.mPC = pc;
        bean.mEPCLen = pCmd[12];
        byte[] epcNum = new byte[bean.mEPCLen];
        System.arraycopy(pCmd, 13, epcNum, 0, epcNum.length);
        bean.mEPCNum = epcNum;
        return bean;
    }

    private static Object handleWriteTagCmd(byte[] pCmd) {
        TagOperationBean bean = new TagOperationBean();
        bean.mStatus = pCmd[5];
        bean.mMsg = "写标签：" + judgeState(pCmd[5]);

        if (pCmd[4] == 1 || pCmd[4] == 2) {
            return bean;
        }

        bean.mTagStatus = pCmd[6];
        bean.mAntenna = pCmd[7];
        byte[] crc = new byte[2];
        System.arraycopy(pCmd, 8, crc, 0, crc.length);
        bean.mCRC = crc;
        byte[] pc = new byte[2];
        System.arraycopy(pCmd, 10, pc, 0, pc.length);
        bean.mPC = pc;
        bean.mEPCLen = pCmd[12];
        byte[] epcNum = new byte[bean.mEPCLen];
        System.arraycopy(pCmd, 13, epcNum, 0, epcNum.length);
        bean.mEPCNum = epcNum;
        return bean;
    }

    private static TagOperationBean handleReadTagCmd(byte[] pCmd) {
        TagOperationBean bean = new TagOperationBean();
        bean.mStatus = pCmd[5];
        bean.mMsg = "读取标签" + judgeState(pCmd[5]);

        if (pCmd[4] == 1 || pCmd[4] == 2) {
            return bean;
        }

        bean.mTagStatus = pCmd[6];
        bean.mAntenna = pCmd[7];

        byte[] bytes = new byte[2];
        System.arraycopy(pCmd, 8, bytes, 0, bytes.length);
        bean.mCRC = bytes;

        byte[] bytes1 = new byte[2];
        System.arraycopy(pCmd, 10, bytes1, 0, bytes1.length);
        bean.mPC = bytes1;
        bean.mEPCLen = pCmd[12];

        byte[] bytes2 = new byte[bean.mEPCLen];
        System.arraycopy(pCmd, 13, bytes2, 0, bytes2.length);
        bean.mEPCNum = bytes2;

        bean.mWordCount = pCmd[13 + bean.mEPCLen];

        byte[] bytes3 = new byte[bean.mWordCount * 2];
        for (int i = 0; i < bytes3.length; i++) {
            bytes3[i] = pCmd[14 + bean.mEPCLen + i];
        }
        bean.mData = bytes3;
        return bean;
    }

    private static GeneralBean handleSelectMask(byte[] pCmd) {
        GeneralBean bean = new GeneralBean();
        bean.mStatus = pCmd[5];
        bean.mMsg = "选择标签：" + judgeState(pCmd[5]);
        return bean;
    }

    private static PermissionParamBean handleGetOrSetPermissionParam(byte[] pCmd) {
        PermissionParamBean bean = new PermissionParamBean();
        bean.mStatus = pCmd[5];
        bean.mOption = pCmd[6];
        if (bean.mOption == 0x01) {
            //设置
            bean.mMsg = "设置权限参数：" + judgeState(pCmd[5]);
            return bean;
        } else {
            //读取
            bean.mMsg = "读取权限参数：" + judgeState(pCmd[5]);
        }
        bean.mCodeEN = pCmd[7];
        byte[] bytes = new byte[4];
        System.arraycopy(pCmd, 8, bytes, 0, bytes.length);
        bean.mCodes = bytes;
        bean.mMaskEN = pCmd[12];
        bean.mStartAdd = pCmd[13];
        bean.mMaskLen = pCmd[14];
        byte[] bytes1 = new byte[31];
        System.arraycopy(pCmd, 15, bytes1, 0, bytes1.length);
        bean.mMaskData = bytes1;
        bean.mMaskCondition = pCmd[46];
        return bean;
    }

    private static OutputModeBean handleOutMode(byte[] pCmd) {
        OutputModeBean outputModeBean = new OutputModeBean();
        outputModeBean.mStatus = pCmd[5];
        //指令长度为7是设置指令的应答，长度为9是获取指令的应答
        if (pCmd[4] == 1) {
            //设置
            outputModeBean.mOption = 0x01;
            outputModeBean.mMsg = "设置蓝牙输出模式：" + judgeState(pCmd[5]);
        } else {
            //获取
            outputModeBean.mOption = 0x02;
            outputModeBean.mMode = pCmd[6];
            outputModeBean.mMsg = "获取蓝牙输出模式：" + judgeState(pCmd[5]);
        }
        return outputModeBean;
    }

    private static DeviceInfoBean handleGetDeviceInfo(byte[] pCmd) {
        DeviceInfoBean deviceInfoBean = new DeviceInfoBean();
        deviceInfoBean.mStatus = pCmd[5];
        deviceInfoBean.mMsg = "获取设备信息：" + judgeState(pCmd[5]);

        if (pCmd[4] == 1) return deviceInfoBean;

        //hardware version
        byte[] bytes = new byte[32];
        System.arraycopy(pCmd, 6, bytes, 0, bytes.length);
        deviceInfoBean.mHwVer = new String(bytes, Charset.defaultCharset()).trim();

        //Firmware version
        byte[] bytes1 = new byte[32];
        System.arraycopy(pCmd, 38, bytes1, 0, bytes1.length);
        deviceInfoBean.mFirmVer = new String(bytes1, Charset.defaultCharset()).trim();

        //sn
        byte[] bytes2 = new byte[12];
        System.arraycopy(pCmd, 70, bytes2, 0, bytes2.length);
        deviceInfoBean.mSn = new String(bytes2, Charset.defaultCharset()).trim();

        //RFID mode version
        byte[] bytes3 = new byte[32];
        System.arraycopy(pCmd, 82, bytes3, 0, bytes3.length);
        deviceInfoBean.mRFIDModeVer = new String(bytes3, Charset.defaultCharset()).trim();

        //RFID mode name
        byte[] bytes4 = new byte[32];
        System.arraycopy(pCmd, 114, bytes4, 0, bytes4.length);
        deviceInfoBean.mRFIDModeName = new String(bytes4, Charset.defaultCharset()).trim();

        //RFID mode sn
        byte[] bytes5 = new byte[12];
        System.arraycopy(pCmd, 146, bytes5, 0, bytes5.length);
        deviceInfoBean.mRFIDModeSn = new String(bytes5, Charset.defaultCharset()).trim();

        return deviceInfoBean;
    }

    /**
     * 解析获取/设置设备名称
     *
     * @param pCmd
     * @return
     */
    private static DeviceNameBean handleSetOrGetBtName(byte[] pCmd) {
        DeviceNameBean deviceNameBean = new DeviceNameBean();
        deviceNameBean.mStatus = pCmd[5];
        byte option = pCmd[6];
        deviceNameBean.mOption = option;
        if (option == 0x01) {
            deviceNameBean.mMsg = "设置设备名称：" + judgeState(pCmd[5]);
        } else if (option == 0x02) {
            deviceNameBean.mMsg = "获取设备名称：" + judgeState(pCmd[5]);
            //获取设备名称
            byte[] bytes = new byte[pCmd.length - 9];
            System.arraycopy(pCmd, 7, bytes, 0, bytes.length);
            deviceNameBean.mDeviceName = new String(bytes, Charset.defaultCharset()).trim();
        } else {
            deviceNameBean.mMsg = "设置/获取设备名称：未知操作指令";
        }
        return deviceNameBean;
    }

    /**
     * 处理获取电池电量信息
     *
     * @param pCmd
     * @return
     */
    private static BatteryCapacityBean handleGetBatteryCapacity(byte[] pCmd) {
        BatteryCapacityBean batteryCapacityBean = new BatteryCapacityBean();
        batteryCapacityBean.mStatus = pCmd[5];
        batteryCapacityBean.mMsg = "获取电量：" + judgeState(pCmd[5]);
        batteryCapacityBean.mBatteryCapacity = pCmd[6];
        return batteryCapacityBean;
    }

    /**
     * 解析获取所有参数指令
     *
     * @param pCmd
     * @return
     */
    private static AllParamBean handleGetAllParam(byte[] pCmd) {
        AllParamBean allParamBean = new AllParamBean();

        allParamBean.mStatus = pCmd[5];
        allParamBean.mMsg = "获取所有参数：" + judgeState(pCmd[5]);

        if (pCmd[4] == 1) {
            return allParamBean;
        }

        //addr信息
        allParamBean.mAddr = pCmd[6];
        //RFIDPRO信息
        allParamBean.mRFIDPRO = pCmd[7];
        //work mode信息
        allParamBean.mWorkMode = pCmd[8];
        //interface信息
        allParamBean.mInterface = pCmd[9];
        //Baud rate信息
        allParamBean.mBaudrate = pCmd[10];
        //WGSet信息
        allParamBean.mWGSet = pCmd[11];
        //Ant信息
        allParamBean.mAnt = pCmd[12];

        //RfidFreq信息
        AllParamBean.RfidFreq rfidFreq = new AllParamBean.RfidFreq();
        rfidFreq.mREGION = pCmd[13];

        byte[] bytes1 = new byte[2];
        System.arraycopy(pCmd, 14, bytes1, 0, bytes1.length);
        rfidFreq.mSTRATFREI = bytes1;

        byte[] bytes2 = new byte[2];
        System.arraycopy(pCmd, 16, bytes2, 0, bytes2.length);
        rfidFreq.mSTRATFRED = bytes2;

        byte[] bytes3 = new byte[2];
        System.arraycopy(pCmd, 18, bytes3, 0, bytes3.length);
        rfidFreq.mSTEPFRE = bytes3;

        rfidFreq.mCN = pCmd[20];

        allParamBean.mRfidFreq = rfidFreq;


        //RfidPower
        allParamBean.mRfidPower = pCmd[21];
        //InquiryArea
        allParamBean.mInquiryArea = pCmd[22];
        //QValue
        allParamBean.mQValue = pCmd[23];
        //Session
        allParamBean.mSession = pCmd[24];
        //AcsAddr
        allParamBean.mAcsAddr = pCmd[25];
        //AcsDataLen
        allParamBean.mAcsDataLen = pCmd[26];
        //FilterTime
        allParamBean.mFilterTime = pCmd[27];
        //TriggerTime
        allParamBean.mTriggerTime = pCmd[28];
        //BuzzerTime
        allParamBean.mBuzzerTime = pCmd[29];
        //Polling Interval
        allParamBean.mPollingInterval = pCmd[30];
        return allParamBean;
    }

    /**
     * 解析设置所有参数回调
     *
     * @param pCmd
     * @return
     */
    public static GeneralBean handleSetAllParam(byte[] pCmd) {
        GeneralBean generalBean = new GeneralBean();
        generalBean.mStatus = pCmd[5];
        generalBean.mMsg = "设置所有参数：" + judgeState(pCmd[5]);
        return generalBean;
    }

    /**
     * 处理初始化模块指令
     */
    public static GeneralBean handleModuleInit(byte[] pCmd) {
        GeneralBean generalBean = new GeneralBean();
        generalBean.mStatus = pCmd[5];
        generalBean.mMsg = "初始化设备：" + judgeState(pCmd[5]);
        return generalBean;
    }

    /**
     * 处理恢复出厂设置指令
     *
     * @param pCmd
     * @return
     */
    public static GeneralBean handleReboot(byte[] pCmd) {
        GeneralBean generalBean = new GeneralBean();
        generalBean.mStatus = pCmd[5];
        generalBean.mMsg = "恢复出厂设置：" + judgeState(pCmd[5]);
        return generalBean;
    }

    /**
     * 处理设置射频输出功率
     *
     * @param pCmd
     * @return CmdData.GeneralBean
     */
    public static GeneralBean handleSetPWR(byte[] pCmd) {
        byte b = pCmd[5];
        GeneralBean generalBean = new GeneralBean();
        switch (b) {
            case 0x00:
                generalBean.mStatus = 0;
                generalBean.mMsg = "模块初始化成功";
                break;
            default:
                generalBean.mStatus = b;
                generalBean.mMsg = "未知模块初始化状态";
                break;
        }
        return generalBean;
    }

    public static TagInfoBean handleInventoryISOContinue(byte[] pCmd) {
        TagInfoBean bean = new TagInfoBean();
        bean.mStatus = pCmd[5];
        bean.mMsg = "盘点标签：" + judgeState((byte) bean.mStatus);

        if (pCmd[4] == 1) return bean;

        bean.mRSSI = ((pCmd[6] << 8) | (pCmd[7] & 0xFF)) / 10;

        bean.mAntenna = pCmd[8];
        bean.mChannel = pCmd[9];
        bean.mEPCLen = pCmd[10];
        byte[] bytes = new byte[pCmd.length - 13];
        System.arraycopy(pCmd, 11, bytes, 0, bytes.length);
        bean.mEPCNum = bytes;
        return bean;
    }

    public static String judgeState(byte pB) {
        switch (pB) {
            case 0x00:
                return "执行成功";
            case 0x01:
                return "参数值错误或越界，或者模块不支持该参数值 ";
            case 0x02:
                return "由于模块内部错误导致的命令执行失败";
            case 0x03:
                return "保留";
            case 0x12:
                return "没有盘点到标签或整个盘点命令执行完成";
            case 0x14:
                return "标签响应超时";
            case 0x15:
                return "解调标签响应错误";
            case 0x16:
                return "协议认证失败";
            case 0x17:
                return "口令错误";
            case (byte) 0xFF:
                return "没有更多数据了";
            default:
                return "未知状态";
        }
    }

    public static GeneralBean handleStopInventory(byte[] pCmd) {
        GeneralBean generalBean = new GeneralBean();
        generalBean.mStatus = pCmd[5];
        generalBean.mMsg = "停止盘点：" + judgeState(pCmd[5]);
        return generalBean;
    }

}
