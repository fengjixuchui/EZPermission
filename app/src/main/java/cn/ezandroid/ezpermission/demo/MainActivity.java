package cn.ezandroid.ezpermission.demo;

import android.Manifest;
import android.content.Context;
import android.os.Bundle;
import android.support.v13.app.ActivityCompat;
import android.util.Log;
import android.view.View;

import cn.ezandroid.ezpermission.EZPermission;
import cn.ezandroid.ezpermission.Permission;
import cn.ezandroid.ezpermission.PermissionCallback;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        $(R.id.hasPermissions).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int v1 = ActivityCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.CAMERA);
                boolean v2 = EZPermission.permissions(Permission.CAMERA)
                        .available(MainActivity.this);
                Log.e("MainActivity", "hasPermissions:" + v1 + " " + v2);
            }
        });

        $(R.id.checkPermissions).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Permission storage = new Permission(Permission.STORAGE);
                Permission readPhoneState = new Permission(Manifest.permission.READ_PHONE_STATE);
                EZPermission.permissions(storage, readPhoneState)
                        .apply(MainActivity.this, new PermissionCallback() {
                            @Override
                            public void onPermissionGranted(Permission grantedPermission) {
                                Log.e("MainActivity", "onPermissionGranted:" + grantedPermission
                                        + " " + grantedPermission.available(MainActivity.this));
                            }

                            @Override
                            public void onPermissionDenied(Permission deniedPermission, boolean isNoLongerPrompted) {
                                Log.e("MainActivity", "onPermissionDenied a:" + deniedPermission
                                        + " " + deniedPermission.available(MainActivity.this) + " " + isNoLongerPrompted);
                            }

                            @Override
                            public void onAllPermissionsGranted() {
                                Log.e("MainActivity", "onAllPermissionsGranted");
                            }

                            @Override
                            public void onStartSetting(Context context) {
                                Log.e("MainActivity", "onStartSetting");
                            }
                        });
            }
        });
    }
}
