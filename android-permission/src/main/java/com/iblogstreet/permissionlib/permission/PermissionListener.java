package com.iblogstreet.permissionlib.permission;

import android.app.Activity;

/**
 * @author think
 * @date 2018/5/25 下午3:51
 */

public interface PermissionListener {
    void onGranted();

    void onDenied(Activity activity, PermissionListener listener, String... permissions);

    void onCancel();

    void onToSetting();
}
