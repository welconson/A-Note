package com.tcl.shenwk.aNote.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.Toast;

import com.tcl.shenwk.aNote.R;
import com.tcl.shenwk.aNote.manager.LoginManager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * File utility.
 * Created by shenwk on 2018/2/5.
 */

public class FileUtil {
    private static String TAG = "FileUtil";

    /**
     *
     * @param context   context for file operation.
     * @param content   content of note to be saved.
     * @param filePath   file path where the content is stored.
     * @return  return whether operate successfully.
     */
    public static boolean writeFile(Context context, String content, String filePath){
        if(filePath == null)
            return false;
        File file = createFile(filePath);
        Log.i(TAG, "writeFile: filePath " + filePath);
        if(file == null)
            return false;
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            if(content != null) {
                fileOutputStream.write(content.getBytes());
            }
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     *
     * @param dirPath directory name to create.
     * @return  return whether operate successfully.
     */
    public static boolean createDir(String dirPath){
        if(dirPath == null)
            return false;
        File dir = new File(dirPath);
        if(!dir.exists()) {
            if (!dir.mkdir()) {
                Log.i(TAG, "createDir: create directory " + dirPath + " failed");
                return false;
            }
            Log.i(TAG, "createDir: " + dirPath + " success");
            return true;
        }
        else {
            Log.i(TAG, "createDir: failed directory exist");
            return false;
        }
    }

    /**
     *
     * @param name  file name to create.
     * @return  the File is created, or null if failed.
     */
    public static File createFile(String name){
        if(name == null)
            return null;
        File file = new File(name);
        if(!file.exists()){
            try {
                if(!file.createNewFile()){
                    return null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    public static boolean isFileOrDirectoryExist(String filepath){
        if(filepath != null) {
            File file = new File(filepath);
            return file.exists();
        }else return false;
    }

    public static String readFile(String filePath){
        StringBuilder contentBuilder = new StringBuilder();
        File file =  new File(filePath);
        if(file.exists()){
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                byte[] buffer = new byte[1024];
                int length;
                while((length = fileInputStream.read(buffer, 0, 1024)) != -1){
                    contentBuilder.append(new String(buffer, 0, length));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return contentBuilder.toString();
    }

    /**
     *
     * @param name filename excluded directory.
     */
    public static boolean deleteFile(String name){
        File file = new File(name);
        boolean ret = true;
        if(!file.isDirectory()){
            if (file.delete()) {
                Log.i(TAG, "deleteFile: delete file " + file.getAbsolutePath() + " successfully");
            }
            else{
                Log.i(TAG, "deleteFile: delete file " + file.getAbsolutePath() + " failed");
                ret = false;
            }
        }
        return ret;
    }

    /**
     *  Recursively delete files under the pointed directory.
     * @param path directory need to delete.
     * @return  true if all operation handled successfully, or false.
     */
    public static boolean deleteDirectoryAndFiles(String path){
        if(path == null)
            return false;
        boolean ret = true;
        File file = new File(path);
        if(file.isDirectory()){
            String[] childList = file.list();
            for(String child : childList) {
                if (!deleteDirectoryAndFiles(path + File.separator + child)) {
                    ret = false;
                }
            }
            //directory delete here
            if(!file.delete()){
                ret = false;
                Log.i(TAG, "deleteDirectoryAndFiles: directory failed " + path);
            }else{
                Log.i(TAG, "deleteDirectoryAndFiles: directory successfully " + path);
            }
        }
        //normal file delete here
        else if(!file.delete()){
            ret = false;
            Log.i(TAG, "deleteDirectoryAndFiles: normal file failed " + path);
        }
        else Log.i(TAG, "deleteDirectoryAndFiles: normal file successfully " + path);
        return ret;
    }

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    public static boolean saveFileOfUri(Context context, Uri sourceUri, String filePath){
        Log.i(TAG, "saveFileOfUri: save file path = " + filePath);

        // TODO: 2018/3/11 Judge whether the file had exist
        //open the file referred by the uri
        BufferedInputStream bis;
        AssetFileDescriptor fd = null;
        ContentResolver resolver = context.getContentResolver();
        try {
            fd = resolver.openAssetFileDescriptor(sourceUri, "r");
        } catch(FileNotFoundException e) {
            throw new IllegalArgumentException();
        }
        if (fd == null) {
            throw new IllegalArgumentException();
        }
        FileDescriptor descriptor = fd.getFileDescriptor();
        bis = new BufferedInputStream(new FileInputStream(descriptor));

        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(filePath, false));
            byte[] buf = new byte[1024];
            while(bis.read(buf) != -1){
                bos.write(buf);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bis.close();
                if (bos != null) bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     *
     * @param context activity context.
     * @param contentUri uri with the content.
     * @return name of the uri file.
     */
    public static String getFileNameFromURI(Context context, Uri contentUri, int resourceType) {
        if(contentUri == null){
            return null;
        }
        if(StringUtil.equal(contentUri.getScheme(), "file"))
            return contentUri.getPath().substring(contentUri.getPath().lastIndexOf(File.separator) + 1);
        Cursor cursor;
        String ret = null;
        String[] proj;
        switch (resourceType){
            case Constants.RESOURCE_TYPE_IMAGE:
                proj = new String[]{MediaStore.Images.Media.DISPLAY_NAME};break;
            case Constants.RESOURCE_TYPE_AUDIO:
                proj = new String[]{MediaStore.Audio.Media.DISPLAY_NAME};break;
            case Constants.RESOURCE_TYPE_VIDEO:
                proj = new String[]{MediaStore.Video.Media.DISPLAY_NAME};break;
            case Constants.RESOURCE_TYPE_FILE:
                proj = new String[]{OpenableColumns.DISPLAY_NAME};break;
                default:proj = new String[]{""};
        }
        contentUri.getScheme();
        try {
            context.getContentResolver().openAssetFileDescriptor(contentUri, "r");
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            if(cursor != null) {
                int column_index = cursor.getColumnIndexOrThrow(proj[0]);
                cursor.moveToFirst();
                ret = cursor.getString(column_index);
                cursor.close();
                Log.i(TAG, "getFileNameFromURI: try " + ret);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
            Toast.makeText(context, R.string.toast_without_permission, Toast.LENGTH_SHORT).show();
            ret = null;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return ret;
    }


    // check whether the file uri represented need external storage permission
    public static boolean isUriPointToExternalStorage(Context context,Uri uri){
        boolean ret = false;
        if(uri == null)
            return false;
        try{
            context.getContentResolver().openAssetFileDescriptor(uri, "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (SecurityException e){
            e.printStackTrace();
            ret = true;
        }
        return ret;
    }

    // generate Uri of a file pointed by the path.
    public static Uri generateUriFromFilePath(String path){
        if(path == null)
            return null;
        File file = new File(path);
        if(file.exists())
            return Uri.fromFile(file);
        else return null;
    }

    // get temporary directory path.
    public static String getTempDir(Context context){
        return context.getFilesDir() + File.separator + Constants.TEMP_FILE_DIR;
    }


    // clean temp files.
    public static void cleanTempDirectory(Context context){
        File tempDir = new File(getTempDir(context));
        if(tempDir.exists() && tempDir.isDirectory()){
            File[] fileList = tempDir.listFiles();
            for(File file : fileList){
                if (file.delete())
                    Log.i(TAG, "cleanTempDirectory: delete file " + file.getName());
                else Log.i(TAG, "cleanTempDirectory: delete failed on file" + file.getName());
            }
        }
    }

    public static String getFileSize(String path){
        if(path == null)
            return "0B";
        File file = new File(path);
        if(!file.exists())
            return "0B";
        long size = file.length();
        int times = 0;
        while(size >= 1024){
            times++;
            size = size >> 10;
        }
        String postFix = "B";
        switch (times){
            case 1:
                postFix = "K";
                break;
            case 2:
                postFix = "M";
                break;
            case 3:
                postFix = "G";
                break;
        }
        return size + postFix;
    }

    public static String getFileSize(Context context, Uri uri){
        if(uri == null)
            return "0B";
        long size = getAssetFileDescriptorFromUri(context, uri).getLength();
        int times = 0;
        while(size >= 1024){
            times++;
            size = size >> 10;
        }
        String postFix = "B";
        switch (times){
            case 1:
                postFix = "K";
                break;
            case 2:
                postFix = "M";
                break;
            case 3:
                postFix = "G";
                break;
        }
        return size + postFix;
    }

    // get AssetFileDescriptor of uri.
    public static AssetFileDescriptor getAssetFileDescriptorFromUri(Context context, Uri uri){
        if(uri == null)
            return null;
        try {
            AssetFileDescriptor assetFileDescriptor = context.getContentResolver().openAssetFileDescriptor(uri, "r");
            if(assetFileDescriptor == null)
                return  null;
            return assetFileDescriptor;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getUserDirPath(Context context){
        return context.getFilesDir() + File.separator + LoginManager.userFolder;
    }

    public static String getNoteDirPath(Context context, String noteDirName){
        return getUserDirPath(context) + File.separator + noteDirName;
    }

    public static String getNoteContentPath(Context context, String noteDirName){
        return getNoteDirPath(context, noteDirName) + File.separator + Constants.CONTENT_FILE_NAME;
    }

    public static String getResourcePath(Context context, String resourceSavedName){
        return getUserDirPath(context) + File.separator + resourceSavedName;
    }
}
