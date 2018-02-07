package com.tcl.shenwk.aNote.util;

import android.content.Context;
import android.util.Log;

import com.tcl.shenwk.aNote.model.DBFieldsName;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * File utility.
 * Created by shenwk on 2018/2/5.
 */

public class FileUtil {
    private static String TAG = "FileUtil";
    private static String fileRootDir = null;
    public static boolean writeFile(Context context, String content, long noteId){
        if(fileRootDir == null)
            fileRootDir = context.getFilesDir().getAbsolutePath();
        if(createDir(fileRootDir+ File.separator
                + String.valueOf(noteId)) != null) {
            File file = createFile(fileRootDir+ File.separator
                    + noteId + File.separator + Constants.CONTENT_FILE_NAME);
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

    //Create pointed directory, if exist or create successfully return file object.
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

    //Create pointed file, if exist or create successfully return the file object.
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
}
