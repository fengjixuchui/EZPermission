package cn.ezandroid.ezsaf;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;

import androidx.annotation.RequiresApi;
import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class SAFUtil {

    private static ArrayList<String> sExtSdCardPaths = new ArrayList<>();

    public static ArrayList<String> getSecondStoragePaths(Context context) {
        if (sExtSdCardPaths.size() > 0) {
            return sExtSdCardPaths;
        }
        for (File file : context.getExternalFilesDirs("external")) {
            if (file != null && !file.equals(context.getExternalFilesDir("external"))) {
                int index = file.getAbsolutePath().lastIndexOf("/Android/data");
                if (index >= 0) {
                    try {
                        String path = file.getAbsolutePath().substring(0, index);
                        path = new File(path).getCanonicalPath();
                        sExtSdCardPaths.add(path);
                    } catch (Exception ignore) {
                    }
                }
            }
        }
        return sExtSdCardPaths;
    }

    public static String getSecondStorageFolder(File file, Context context) {
        try {
            ArrayList<String> extSdPaths = getSecondStoragePaths(context);
            for (int i = 0; i < extSdPaths.size(); i++) {
                if (file.getCanonicalPath().startsWith(extSdPaths.get(i))) {
                    return extSdPaths.get(i);
                }
            }
        } catch (Exception ignore) {
        }
        return null;
    }

    public static boolean isOnSecondStorage(File file, Context context) {
        return getSecondStorageFolder(file, context) != null;
    }

    public static DocumentFile getDocumentFile(File file, boolean isDirectory, Context context) {
        String baseFolder = getSecondStorageFolder(file, context);
        if (baseFolder == null) {
            return null;
        }

        boolean originalDirectory = false;
        String relativePath = null;
        try {
            String fullPath = file.getCanonicalPath();
            if (!baseFolder.equals(fullPath)) {
                relativePath = fullPath.substring(baseFolder.length() + 1);
            } else {
                originalDirectory = true;
            }
        } catch (IOException e) {
            return null;
        } catch (Exception e) {
            originalDirectory = true;
        }

        String as = PreferenceManager.getDefaultSharedPreferences(context).getString(baseFolder, null);
        Uri treeUri = null;
        if (as != null) {
            treeUri = Uri.parse(as);
        }
        if (treeUri == null) {
            return null;
        }

        DocumentFile document = DocumentFile.fromTreeUri(context, treeUri);
        if (originalDirectory) return document;

        String[] parts = relativePath.split("/");
        for (int i = 0; i < parts.length; i++) {
            if (document != null) {
                DocumentFile nextDocument = document.findFile(parts[i]);
                if (nextDocument == null) {
                    if ((i < parts.length - 1) || isDirectory) {
                        nextDocument = document.createDirectory(parts[i]);
                    } else {
                        nextDocument = document.createFile("text/plain", parts[i]);
                    }
                }
                document = nextDocument;
            }
        }

        return document;
    }

    private static boolean canWrite(File file) {
        boolean res = file.exists() && file.canWrite();
        if (!res && !file.exists()) {
            try {
                if (!file.isDirectory()) {
                    res = file.createNewFile() && file.delete();
                } else {
                    res = file.mkdirs() && file.delete();
                }
            } catch (IOException ignore) {
            }
        }
        return res;
    }

    public static boolean canWrite(Context context, File file) {
        boolean res = canWrite(file);
        if (!res && isOnSecondStorage(file, context)) {
            DocumentFile documentFile = getDocumentFile(file, file.isDirectory(), context);
            res = documentFile != null && documentFile.canWrite();
        }
        return res;
    }

    public static OutputStream getOutputStream(Context context, File destFile) {
        OutputStream out = null;
        try {
            if (!canWrite(destFile) && isOnSecondStorage(destFile, context)) {
                DocumentFile file = getDocumentFile(destFile, destFile.isDirectory(), context);
                if (file != null && file.canWrite()) {
                    out = context.getContentResolver().openOutputStream(file.getUri());
                }
            } else {
                out = new FileOutputStream(destFile);
            }
        } catch (IOException ignore) {
        }
        return out;
    }

    public static boolean saveTreeUri(Context context, String rootPath, Uri uri) {
        DocumentFile file = DocumentFile.fromTreeUri(context, uri);
        if (file != null && file.canWrite()) {
            SharedPreferences perf = PreferenceManager.getDefaultSharedPreferences(context);
            perf.edit().putString(rootPath, uri.toString()).apply();
            return true;
        }
        return false;
    }
}
