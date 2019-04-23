package cn.ezandroid.ezpermission;

import android.content.Context;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

/**
 * 权限管理
 *
 * @author like
 * @date 2017-09-28
 */
public class EZPermission {

    private EZPermission() {
    }

    public static Builder permissions(String... permission) {
        return new Builder(new Permission(permission));
    }

    public static Builder permissions(String[]... groups) {
        List<Permission> permissions = new ArrayList<>();
        for (String[] group : groups) {
            permissions.add(new Permission(group));
        }
        return new Builder(permissions.toArray(new Permission[permissions.size()]));
    }

    public static Builder permissions(Permission... groups) {
        return new Builder(groups);
    }

    public static class Builder {

        private Permission[] mPermissionGroups;

        private Builder(Permission... groups) {
            this.mPermissionGroups = groups;
        }

        /**
         * 权限是否都可用
         *
         * @param context
         * @return
         */
        public boolean available(Context context) {
            for (Permission permission : mPermissionGroups) {
                if (!permission.available(context)) {
                    return false;
                }
            }
            return true;
        }

        /**
         * 申请权限
         *
         * @param context
         */
        public void apply(final Context context) {
            apply(context, null);
        }

        /**
         * 申请权限
         *
         * @param context
         * @param callback
         */
        public void apply(final Context context, final PermissionCallback callback) {
            PermissionCallback globalCallback = new PermissionCallback() {
                int mGrantedCount = 0;
                int mRemainCount = mPermissionGroups.length;
                boolean mHasNoLongerPrompted; // 是否有勾选了不再提示并且拒绝的权限

                @Override
                public void onPermissionGranted(Permission grantedPermission) {
                    if (callback != null) {
                        callback.onPermissionGranted(grantedPermission);
                    }

                    mGrantedCount++;

                    mRemainCount--;
                    if (mRemainCount <= 0) {
                        onAllComplete(mHasNoLongerPrompted);
                    }
                }

                @Override
                public void onPermissionDenied(Permission deniedPermission, boolean isNoLongerPrompted) {
                    if (callback != null) {
                        callback.onPermissionDenied(deniedPermission, isNoLongerPrompted);
                    }

                    mHasNoLongerPrompted = mHasNoLongerPrompted || isNoLongerPrompted;

                    mRemainCount--;
                    if (mRemainCount <= 0) {
                        onAllComplete(mHasNoLongerPrompted);
                    }
                }

                @Override
                public void onAllPermissionsGranted() {
                    if (callback != null) {
                        callback.onAllPermissionsGranted();
                    }
                }

                private void onAllComplete(boolean startSetting) {
                    if (mGrantedCount == mPermissionGroups.length) {
                        onAllPermissionsGranted();
                    } else if (startSetting) {
                        if (callback != null) {
                            callback.onStartSetting(context);
                        }
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        ProxyActivity.sPermissionCallback = null; // 防止内存泄漏
                    }
                }
            };

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                globalCallback.onAllPermissionsGranted();
            } else {
                for (Permission permission : mPermissionGroups) {
                    permission.apply(context, globalCallback);
                }
            }
        }
    }
}
