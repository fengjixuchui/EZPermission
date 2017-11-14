package cn.ezandroid.ezpermission;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.List;

/**
 * 代理Activity
 * <p>
 * 在onRequestPermissionsResult中进行处理权限申请回调是个很不爽的事情，会破坏代码阅读及编写时的连续性。
 * 这里使用一个代理Activity，封装后外边调用时支持使用回调的方式接收权限申请结果。
 *
 * @author like
 * @date 2017-09-28
 */
@RequiresApi(api = Build.VERSION_CODES.M)
public final class ProxyActivity extends Activity {

    private static final String KEY_PERMISSION = "KEY_PERMISSIONS";

    private static PermissionCallback sPermissionCallback;

    private Permission mPermission;

    public static void launch(Context context, Permission permission, PermissionCallback permissionCallback) {
        Intent intent = new Intent(context, ProxyActivity.class);
        intent.putExtra(ProxyActivity.KEY_PERMISSION, permission);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        sPermissionCallback = permissionCallback;
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Permission permission = (Permission) intent.getSerializableExtra(KEY_PERMISSION);

        if (permission == null) {
            finish();
            return;
        }

        mPermission = permission;

        requestPermissions(permission.getPermissions(), 1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        List<String> deniedList = new ArrayList<>();
        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                deniedList.add(permissions[i]);
            }
        }
        if (deniedList.isEmpty()) {
            if (sPermissionCallback != null) {
                sPermissionCallback.onPermissionGranted(mPermission);
            }
        } else {
            if (sPermissionCallback != null) {
                sPermissionCallback.onPermissionDenied(mPermission);
            }
        }
        finish();
    }
}
