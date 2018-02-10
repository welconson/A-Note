package com.tcl.shenwk.aNote.util;

import android.content.Context;
import android.util.Log;

import com.tcl.shenwk.aNote.model.DBFieldsName;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * File utility.
 * Created by shenwk on 2018/2/5.
 */

public class FileUtil {
    private static String TAG = "FileUtil";
    private static String fileRootDir = null;

    /**
     *
     * @param context   context for file operation.
     * @param content   content of note to be saved.
     * @param dirName   directory to store note content and attachments.
     * @return  return whether operate successfully.
     */
    public static boolean writeFile(Context context, String content, String dirName){
        if(fileRootDir == null)
            fileRootDir = context.getFilesDir().getAbsolutePath();
        if(createDir(fileRootDir+ File.separator + dirName) != null) {
            File file = createFile(fileRootDir+ File.separator
                    + dirName + File.separator + Constants.CONTENT_FILE_NAME);
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
        }
        return true;
    }

    /**
     *
     * @param dirName directory name to create.
     * @return  return whether operate successfully.
     */
    public static File createDir(String dirName){
        File dir = new File(dirName);
        if(!dir.exists()) {
            if (!dir.mkdir()) {
                Log.i(TAG, "createDir: create note directory " + dirName + " failed");
                return null;
            }
        }
        return dir;
    }

    /**
     *
     * @param name  file name to create.
     * @return  return whether operate successfully.
     */
    public static File createFile(String name){
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

    public static String readNoteContent(Context context, String dirName){
        if(fileRootDir == null)
            fileRootDir = context.getFilesDir().getAbsolutePath();
        StringBuilder contentBuilder = new StringBuilder();
        File file =  new File(fileRootDir+ File.separator
                + dirName + File.separator + Constants.CONTENT_FILE_NAME);
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

    public static void deleteFile(String name){
        File file = new File(name);
        if(file.exists() && !file.isDirectory()){
            if (file.delete()) {
                Log.i(TAG, "deleteFile: delete file " + file.getAbsolutePath() + " successfully");
            }
            else{
                Log.i(TAG, "deleteFile: delete file " + file.getAbsolutePath() + " failed");
            }
        }
    }
}
