package com.cf.zsdk.uitl;

import java.io.Closeable;
import java.io.IOException;

public class IOUtil {

    /**
     * 关闭数据流，
     *
     * @param pCloseable 可关闭接口类，操作对象为该接口的实现类
     */
    public static void close(Closeable pCloseable) {
        if (pCloseable != null) {
            try {
                pCloseable.close();
            } catch (IOException pE) {
                pE.printStackTrace();
            }
        }
    }
}
