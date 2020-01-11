package cn.ezandroid.ezsaf;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;

import java.io.File;

import cn.ezandroid.ezpermission.R;

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

        Html.ImageGetter imgGetter = source -> {
            int id = Integer.parseInt(source);
            Drawable drawable = getResources().getDrawable(id);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            return drawable;
        };
        String message = getString(R.string.saf_message, SAFUtil.getSecondStorageFolder(mFile, this)) + "\n<img src='" + R.drawable.saf_step + "'/>";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.saf_tip);
        builder.setMessage(Html.fromHtml(message, imgGetter, null));
        builder.setPositiveButton(R.string.saf_choose, (dialog, which) -> startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), 1));
        builder.setNegativeButton(R.string.saf_cancel, (dialog, which) -> mSAFCallback.onSAFDenied(mFile));
        Dialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
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
