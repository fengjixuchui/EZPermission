package cn.ezandroid.ezsaf;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.File;

public class EZSAF {

    private EZSAF() {
    }

    public static Builder document(String path) {
        return new Builder(new File(path));
    }

    public static Builder document(File file) {
        return new Builder(file);
    }

    public static class Builder {

        private File mFile;

        private Builder(File file) {
            this.mFile = file;
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        public boolean canWrite(Context context) {
            return SAFUtil.canWrite(context, mFile);
        }

        public void apply(final Context context, SAFCallback callback) {
            SAFCallback globalCallback = new SAFCallback() {
                @Override
                public void onSAFGranted(File file) {
                    callback.onSAFGranted(file);
                }

                @Override
                public void onSAFDenied(File file) {
                    callback.onSAFDenied(file);
                }
            };
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                globalCallback.onSAFGranted(mFile);
            } else {
                SAFProxyActivity.launch(context, mFile, globalCallback);
            }
        }
    }
}
