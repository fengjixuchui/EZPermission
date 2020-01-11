package cn.ezandroid.ezpermission.demo;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import cn.ezandroid.ezpermission.EZPermission;
import cn.ezandroid.ezpermission.Permission;
import cn.ezandroid.ezsaf.EZSAF;
import cn.ezandroid.ezsaf.SAFCallback;
import cn.ezandroid.ezsaf.SAFUtil;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        $(R.id.hasPermissions).setOnClickListener(view -> {
            int v1 = ActivityCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.CAMERA);
            boolean v2 = EZPermission.permissions(Permission.CAMERA)
                    .available(MainActivity.this);
            Log.e("MainActivity", "hasPermissions:" + v1 + " " + v2);
        });

        $(R.id.checkPermissions).setOnClickListener(view -> {
//            Permission storage = new Permission(Permission.STORAGE);
//            Permission readPhoneState = new Permission(Manifest.permission.READ_PHONE_STATE);
//            EZPermission.permissions(storage, readPhoneState)
//                    .apply(MainActivity.this, new PermissionCallback() {
//                        @Override
//                        public void onPermissionGranted(Permission grantedPermission) {
//                            Log.e("MainActivity", "onPermissionGranted:" + grantedPermission
//                                    + " " + grantedPermission.available(MainActivity.this));
//                        }
//
//                        @Override
//                        public void onPermissionDenied(Permission deniedPermission, boolean isNoLongerPrompted) {
//                            Log.e("MainActivity", "onPermissionDenied a:" + deniedPermission
//                                    + " " + deniedPermission.available(MainActivity.this) + " " + isNoLongerPrompted);
//                        }
//
//                        @Override
//                        public void onAllPermissionsGranted() {
//                            Log.e("MainActivity", "onAllPermissionsGranted");
//                        }
//
//                        @Override
//                        public void onStartSetting(Context context) {
//                            Log.e("MainActivity", "onStartSetting");
//                        }
//                    });
            testSAF();
        });
    }

    private void testSAF() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            File file = new File("/storage/0123-4567/saftest3.sgf");
            boolean canWrite = EZSAF.document(file).canWrite(this);
            Log.e("MainActivity", "canWrite:" + canWrite);
            EZSAF.document(file).apply(this, new SAFCallback() {
                @Override
                public void onSAFGranted(File file) {
                    Log.e("MainActivity", "onSAFGranted");
                    OutputStream outputStream = SAFUtil.getOutputStream(MainActivity.this, file);
                    String content = "哈哈哈";
                    byte[] data = content.getBytes();
                    try {
                        outputStream.write(data, 0, data.length);
                        outputStream.flush();
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onSAFDenied(File file) {
                    Log.e("MainActivity", "onSAFDenied");
                }
            });
        }
    }
}
