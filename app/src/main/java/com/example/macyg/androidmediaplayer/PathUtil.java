package com.example.macyg.androidmediaplayer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Aki on 1/7/2017.
 */

public class PathUtil extends MainActivity {
    /*
     * Gets the file path of the given Uri.
     */
    public static String removableStoragePath;
    public static String sdPath, sdCardLabel;
    public static boolean sdCardPathDetection;
    public static int dirCounterSD;
    public static ArrayList<Integer> iterationCounterLocationsSD = new ArrayList<Integer>();

    @SuppressLint("NewApi")
    public static String getPath(Context context, Uri uri) {
        final boolean needToCheckUri = Build.VERSION.SDK_INT >= 19;
        String selection = null;
        System.out.println("URI SCHEME: " + uri.getScheme() + "getDocumentId: " + DocumentsContract.getDocumentId(uri));
        String[] selectionArgs = null;
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        // deal with different Uris.
        if (DocumentsContract.isDocumentUri(context.getApplicationContext(), uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                File fileList[] = new File("/storage/").listFiles();
                for (File file : fileList) {
                    if (!file.toString().contains("self") && !file.toString().contains("emulated")) {
                        sdPath = file.toString();
                        System.out.println("SD PATH SUCCESSFULLY IDENTIFIED!: " + sdPath);
                    }
                    sdCheck(uri);
                }
                System.out.println(removableStoragePath);
                return removableStoragePath + "/" + split[1];

            } else {
                if (isMediaDocument(uri)) {
                    String pathUtil = uri.getPath();
                    System.out.println("Media document: " + pathUtil);
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    File fileList[] = new File("/storage/").listFiles();
                    for (File file : fileList) {
                        if (file.toString().contains("self") || file.toString().contains("emulated")) {
                            sdPath = file.toString();
                            System.out.println("SD PATH SUCCESSFULLY IDENTIFIED!: " + sdPath);
                        }
                        sdCheck(uri);
                    }
                    System.out.println(removableStoragePath);
                    return removableStoragePath + "/" + split[1];
                }
            }
        }
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public static void sdCheck(Uri uri) {
        System.out.println("SD CHECK IS WORKING!");
        for (int i = 0; i < uri.toString().length(); i++) {
            char c = uri.toString().charAt(i);
            //Process char
            if (c == '/') {
                dirCounterSD += 1;
                iterationCounterLocationsSD.add(i);
            }
        }
        Object iterationArraySD[] = iterationCounterLocationsSD.toArray();
        int lastIndexPathSD = iterationArraySD.length;
        int lastIndexSlashSD = (int) iterationArraySD[lastIndexPathSD - 1];
        System.out.println("# Slashes: " + lastIndexPathSD + " Slash index: "
                + iterationArraySD[lastIndexPathSD - 1].toString());

        sdCardLabel = uri.toString().substring(lastIndexSlashSD + 1, lastIndexSlashSD + 10);
        if (sdCardLabel.toCharArray()[4] == "-".toCharArray()[0]) {
            sdCardPathDetection = true;
            removableStoragePath = sdPath;
        } else {
            removableStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        }
        System.out.println("SD Card Label: " + sdCardLabel);

    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}
