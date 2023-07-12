package com.example.ogima.helper;

import android.content.Context;
import android.util.Log;

import java.io.File;

public class LimparCacheUtils {

    public void clearAppCache(Context context) {
        try {
            File cacheDir = context.getCacheDir();
            if (cacheDir != null && cacheDir.isDirectory()) {
                deleteDir(cacheDir);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    boolean success = deleteDir(new File(dir, child));
                    if (!success) {
                        return false;
                    }
                }
            }
            Log.d("CACHE", "SUB CACHE LIMPO");
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            Log.d("CACHE", "CACHE LIMPO");
            return dir.delete();
        } else {
            return false;
        }
    }
}
