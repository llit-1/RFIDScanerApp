package com.cf.beans;

import java.lang.reflect.Field;

public class CmdData {

    /**
     * 数据类型
     */
    private Class<?> mDataType;

    /**
     * 注意，若是增加了一个bean类，需要声明成员变量，否则调用setData无法设置数据
     */
    private GeneralBean mGeneralBean;
    private TagInfoBean mTagInfoBean;
    private AllParamBean mAllParamBean;
    private BatteryCapacityBean mBatteryCapacityBean;
    private DeviceNameBean mDeviceNameBean;
    private DeviceInfoBean mDeviceInfoBean;
    private OutputModeBean mOutputModeBean;
    private PermissionParamBean mPermissionParamBean;
    private TagOperationBean mTagOperationBean;
    private KeyStateBean mKeyStateBean;
    private RemoteNetParaBean mRemoteNetParaBean;

    /**
     * 设置数据，通过反射设置给当前类的成员变量赋值
     * 所以需要做sdk代码混淆的时候注意保留成员变量
     *
     * @param object 需要设置的数据对象
     */
    public void setData(Object object) {
        if (object == null) return;
        Class<?> aClass = object.getClass();
        for (Field field : getClass().getDeclaredFields()) {
            if (field.getType().equals(aClass)) {
                try {
                    mDataType = aClass;
                    field.set(this, object);
                } catch (IllegalAccessException pE) {
                    pE.printStackTrace();
                }
                break;
            }
        }

    }

    /**
     * 通过mDataType变量获取当前赋值的数据
     *
     * @return 返回当前数据类型对象
     */
    public Object getData() {
        for (Field field : getClass().getDeclaredFields()) {
            if (field.getType().equals(mDataType)) {
                try {
                    return field.get(this);
                } catch (IllegalAccessException pE) {
                    pE.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 获取数据类型
     *
     * @return 返回数据类型的class对象
     */
    public Class<?> getDataType() {
        return mDataType;
    }
}
