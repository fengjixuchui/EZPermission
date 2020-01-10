package cn.ezandroid.ezsaf;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;

import java.io.File;

/**
 * 代理Activity
 *
 * @author like
 * @date 2020-01-10
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public final class SAFProxyActivity extends Activity {

    private static final String KEY_FILE = "KEY_FILE";

    public static SAFCallback mSAFCallback;

    private File mFile;

    public static void launch(Context context, File file, SAFCallback safCallback) {
        Intent intent = new Intent(context, SAFProxyActivity.class);
        intent.putExtra(KEY_FILE, file.getAbsolutePath());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mSAFCallback = safCallback;
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String path = intent.getStringExtra(KEY_FILE);

        if (path == null) {
            finish();
            return;
        }

        mFile = new File(path);

        startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Uri treeUri = data.getData();
            if (treeUri != null) {
                grantUriPermission(getPackageName(), treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                SAFUtil.saveTreeUri(this, SAFUtil.getSecondStorageFolder(mFile, this), data.getData());
                mSAFCallback.onSAFGranted(mFile);
            } else {
                mSAFCallback.onSAFDenied(mFile);
            }
        } else {
            mSAFCallback.onSAFDenied(mFile);
        }
        finish();
    }
}
