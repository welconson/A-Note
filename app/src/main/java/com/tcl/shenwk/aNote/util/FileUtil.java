package com.tcl.shenwk.aNote.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

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
     * @param dirName directory name to create.
     * @return  return whether operate successfully.
     */
    public static boolean createDir(String dirName){
        if(dirName == null)
            return false;
        File dir = new File(dirName);
        if(!dir.exists()) {
            if (!dir.mkdir()) {
                Log.i(TAG, "createDir: create directory " + dirName + " failed");
                return false;
            }
            Log.i(TAG, "createDir: " + dirName + " success");
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

    public static boolean isFileExist(String filepath){
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
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
                byte[] buffer = new byte[1024];
                String line;
                while((line = bufferedReader.readLine()) != null){
                    contentBuilder.append(line);
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
        Log.i(TAG, "saveFileOfUri: sava file path = " + filePath);

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
            Toast.makeText(context, Constants.TOAST_TEXT_WITHOUT_PERMISSION, Toast.LENGTH_SHORT).show();
            ret = null;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static Uri getUriFromFile(String filePath){
        if(filePath == null)
            return null;
        return Uri.fromFile(new File(filePath));
    }

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

    public static String getContentFileName(String notePath){
        return notePath + File.separator + Constants.CONTENT_FILE_NAME;
    }
}
