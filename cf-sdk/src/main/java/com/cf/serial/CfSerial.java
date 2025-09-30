package com.cf.serial;

import com.cf.zsdk.uitl.IOUtil;
import com.cf.zsdk.uitl.LogUtil;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CfSerial {

    // Used to load the 'serial' library on application startup.
    static {
        System.loadLibrary("cf_serial");
    }

    /*
     * Do not remove or rename the field mFd: it is used by native method close();
     */
    private FileDescriptor mFd;
    private FileInputStream mFileInputStream;
    private FileOutputStream mFileOutputStream;

    public CfSerial(String pDevicePath, int baudrate, int flags) throws SecurityException, IOException {
        File device = new File(pDevicePath);

        //这里的代码在一些无ROOT权限的设备上会提示安全风险，要是串口没有权限，让你厂商开放权限是正确的做法，所以将代码注释掉
        /* Check access permission */
        //if (!device.canRead() || !device.canWrite()) {
        //    try {
        //        /* Missing read/write permission, trying to chmod the file */
        //        Process su;
        //        su = Runtime.getRuntime().exec("/system/bin/su");
        //        String cmd = "chmod 666 " + device.getAbsolutePath() + "\n" + "exit\n";
        //        su.getOutputStream().write(cmd.getBytes());
        //        if ((su.waitFor() != 0) || !device.canRead() || !device.canWrite()) {
        //            throw new SecurityException();
        //        }
        //    } catch (Exception e) {
        //        e.printStackTrace();
        //        throw new SecurityException();
        //    }
        //}

        mFd = open(device.getAbsolutePath(), baudrate, flags);
        if (mFd == null) {
            LogUtil.e("native open returns null");
            throw new IOException();
        }
        mFileInputStream = new FileInputStream(mFd);
        mFileOutputStream = new FileOutputStream(mFd);
    }

    // Getters and setters
    public InputStream getInputStream() {
        return mFileInputStream;
    }

    public OutputStream getOutputStream() {
        return mFileOutputStream;
    }

    public void closeSerial() {
        IOUtil.close(mFileInputStream);
        IOUtil.close(mFileOutputStream);
        close();
    }

    // JNI
    private native static FileDescriptor open(String pPath, int pBaudRate, int flags);

    private native void close();
}