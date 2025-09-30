package com.cf.zsdk.cmd;

public class CmdType {
    /**
     * 盘点标签
     */
    public static final int TYPE_INVENTORY = 0x0001;
    /**
     * 停止盘点标签
     */
    public static final int TYPE_STOP_INVENTORY = 0x0002;
    /**
     * 读取标签
     */
    public static final int TYPE_READ_TAG = 0x0003;
    /**
     * 写标签
     */
    public static final int TYPE_WRITE_TAG = 0x0004;
    /**
     * 锁定标签
     */
    public static final int TYPE_LOCK_TAG = 0x0005;
    /**
     * 灭活标签
     */
    public static final int TYPE_KILL_TAG = 0x0006;
    /**
     * 选定标签
     */
    public static final int TYPE_SELECT_MASK = 0x0007;
    /**
     * 模块初始化
     */
    public static final int TYPE_MODULE_INIT = 0x0050;
    /**
     * 恢复出厂设置
     */
    public static final int TYPE_REBOOT = 0x0052;
    /**
     * 网络参数接口参数
     */
    public static final int TYPE_REMOTE_NET_PARA = 0x005F;
    /**
     * 获取设备信息
     */
    public static final int TYPE_GET_DEVICE_INFO = 0x0070;
    /**
     * 设置所有参数
     */
    public static final int TYPE_SET_ALL_PARAM = 0x0071;
    /**
     * 获取所有参数
     */
    public static final int TYPE_GET_ALL_PARAM = 0x0072;
    /**
     * 设置或获取权限参数
     */
    public static final int TYPE_GET_OR_SET_PERMISSION = 0x0076;
    /**
     * 获取电池信息
     */
    public static final int TYPE_GET_BATTERY_CAPACITY = 0x0083;
    /**
     * 设置或获取设备名称
     */
    public static final int TYPE_SET_OR_GET_BT_NAME = 0x0086;
    /**
     * 输出模式
     */
    public static final int TYPE_OUT_MODE = 0x0088;
    /**
     * 按键状态
     */
    public static final int TYPE_KEY_STATE = 0x0089;
}
