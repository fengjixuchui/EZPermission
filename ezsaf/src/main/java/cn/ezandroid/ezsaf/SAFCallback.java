package cn.ezandroid.ezsaf;

import java.io.File;

/**
 * 权限申请回调
 *
 * @author like
 * @date 2017-09-28
 */
public interface SAFCallback {

    /**
     * 权限申请成功
     */
    default void onSAFGranted(File file) {
    }

    /**
     * 权限申请失败
     */
    default void onSAFDenied(File file) {
    }
}
