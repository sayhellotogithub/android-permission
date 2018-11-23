package com.iblogstreet.permissionlib.permission;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.tbruyelle.rxpermissions.RxPermissions;

import java.util.ArrayList;
import java.util.List;

import rx.functions.Action1;


/**
 * @author think
 * @date 2018/5/25 下午3:49
 */

public class PermissionUtil {

    /**
     * 请求权限
     *
     * @param activity
     * @param listener
     * @param permissions
     */
    public static void requestPermission(final Activity activity, final PermissionListener listener, final String... permissions) {
        RxPermissions rxPermissions = new RxPermissions(activity);
        rxPermissions.request(permissions)
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        if (listener == null) {
                            return;
                        }
                        if (aBoolean) {
                            //授权成功
                            listener.onGranted();
                        } else {
                            listener.onDenied(activity, listener, permissions);
                        }
                    }
                });
    }

    /**
     * 文件权限
     *
     * @param activity
     * @param listener
     */
    public static void requestStoragePermission(Activity activity, PermissionListener listener) {
        requestPermission(activity, listener,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    /**
     * 根据参数显示提示
     *
     * @param context
     * @param params
     */
    public static void showMessageByParams(Context context, List<String> params, PermissionListener listener) {
        if (params != null) {
            String[] permissions = new String[params.size()];
            for (int i = 0; i < params.size(); i++) {
                permissions[i] = params.get(i);
            }
            showMessageByParams(context, permissions, listener);
        }
    }

    /**
     * 根据参数显示提示
     *
     * @param context
     * @param params
     */
    public static void showMessageByParams(final Context context, String[] params, final PermissionListener listener) {
        String message = getMessageByParams(params);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("提示")
                .setCancelable(false)
                .setMessage(message)
                .setPositiveButton("去设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        toPermission(context);
                        listener.onToSetting();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onCancel();
                    }
                }).create();
        dialog.show();
    }


    /**
     * 跳转到应用设置页面
     *
     * @param context
     */
    private static void toPermission(Context context) {
        String scheme = "package";
        //调用系统InstalledAppDetails界面所需的Extra名称(用于Android 2.1及之前版本)
        final String appPkgName21 = "com.android.settings.ApplicationPkgName";
        //调用系统InstalledAppDetails界面所需的Extra名称(用于Android 2.2)
        final String appPkgName22 = "pkg";
        //InstalledAppDetails所在包名
        final String appDetailsPackageName = "com.android.settings";
        //InstalledAppDetails类名
        final String appDetailsClassName = "com.android.settings.InstalledAppDetails";

        Intent intent = new Intent();
        final int apiLevel = Build.VERSION.SDK_INT;
        if (apiLevel >= 9) {
            // 2.3（ApiLevel 9）以上，使用SDK提供的接口
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts(scheme, context.getPackageName(), null);
            intent.setData(uri);
        } else { // 2.3以下，使用非公开的接口（查看InstalledAppDetails源码）
            // 2.2和2.1中，InstalledAppDetails使用的APP_PKG_NAME不同。
            final String appPkgName = (apiLevel == 8 ? appPkgName22
                    : appPkgName21);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setClassName(appDetailsPackageName,
                    appDetailsClassName);
            intent.putExtra(appPkgName, context.getPackageName());
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 获取提示内容
     *
     * @param params
     * @return
     */
    private static String getMessageByParams(String[] params) {
        if (params == null) {
            return "";
        }
        StringBuilder permissions = new StringBuilder();
        boolean isFirst = true;
        for (String param :
                params) {
            if (!isFirst) {
                permissions.append("、");
            } else {
                isFirst = false;
            }
            permissions.append(getPermissionName(param));

        }
        return "在设置-应用-权限中开启" + permissions + "权限，以正常使用功能";
    }

    /**
     * 根据权限，获取中文名
     *
     * @param permission
     * @return
     */
    private static String getPermissionName(String permission) {
        if (permission == null) {
            return "";
        }
        if (TextUtils.equals(permission, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                TextUtils.equals(permission, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            return "存储";
        } else if (TextUtils.equals(permission, Manifest.permission.CAMERA)) {
            return "相机";
        } else if (TextUtils.equals(permission, Manifest.permission.ACCESS_FINE_LOCATION) ||
                TextUtils.equals(permission, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            return "位置信息";
        } else if (TextUtils.equals(permission, Manifest.permission.READ_PHONE_STATE) ||
                TextUtils.equals(permission, Manifest.permission.READ_CALL_LOG) ||
                TextUtils.equals(permission, Manifest.permission.CALL_PHONE) ||
                TextUtils.equals(permission, Manifest.permission.WRITE_CALL_LOG)) {
            return "电话";
        }
        return "";
    }

    /**
     * 将String... permissions转化为集合
     *
     * @param permissions
     * @return
     */
    public static List<String> getDeniedPermission(Activity activity, String... permissions) {
        List<String> params = new ArrayList<>();
        for (String permission :
                permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                //被拒绝的权限
                params.add(permission);
            }
        }
        return params;
    }

    /**
     * 判断是否有调用相机的权限
     *
     * @param activity
     * @return
     */
    public static boolean isCameraPermission(Activity activity) {

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }

    /**
     * 是否有拨号的权限
     *
     * @param activity
     * @return
     */
    public static boolean isCallPermission(Activity activity) {
//        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CALL_PHONE)) {
//            //被拒绝的权限
//            return false;
//        }
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }

    /**
     * 拨号的权限
     *
     * @param activity
     * @param listener
     */
    public static void requestDialogPermission(Activity activity, PermissionListener listener) {
        requestPermission(activity, listener,
                Manifest.permission.CALL_PHONE);
    }

    /**
     * 拨打电话
     *
     * @param phoneNum
     */
    public static void callPhone(Context context, String phoneNum) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        Uri data = Uri.parse("tel:" + phoneNum);
        intent.setData(data);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        context.startActivity(intent);
    }

    /**
     * 跳转到权限设置界面
     *
     * @param context
     */
    public static void gotoSettingsPermission(Context context) {
        // 6.0以上系统才可以判断权限
        // 进入设置系统应用权限界面
        Intent intent = new Intent(Settings.ACTION_SETTINGS);
        context.startActivity(intent);
    }
}
