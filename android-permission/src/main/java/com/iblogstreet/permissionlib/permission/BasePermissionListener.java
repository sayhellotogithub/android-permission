package com.iblogstreet.permissionlib.permission;

import android.app.Activity;

import java.util.List;

/**
 * @author think
 * @date 2018/5/25 下午3:51
 */

public abstract class BasePermissionListener implements PermissionListener {

    @Override
    public void onDenied(Activity activity, PermissionListener listener, String... permissions) {
        List<String> params = PermissionUtil.getDeniedPermission(activity, permissions);
        PermissionUtil.showMessageByParams(activity, params, listener);
    }

    @Override
    public void onCancel() {

    }

    @Override
    public void onToSetting() {

    }
}
