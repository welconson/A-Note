package com.tcl.shenwk.aNote.model;

import android.content.Context;
import android.net.Uri;
import android.text.Editable;
import android.util.Log;

import com.tcl.shenwk.aNote.entry.NoteEntry;
import com.tcl.shenwk.aNote.entry.ResourceDataEntry;
import com.tcl.shenwk.aNote.util.Constants;
import com.tcl.shenwk.aNote.util.FileUtil;
import com.tcl.shenwk.aNote.util.RandomUtil;
import com.tcl.shenwk.aNote.view.activity.HomePageActivity;
import com.tcl.shenwk.aNote.view.customSpan.ViewSpan;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Note operation handler, include editing note and managing note.
 * Created by shenwk on 2018/2/2.
 */

public class NoteHandler {
    private static String TAG = "NoteHandler";
    /**
     * The method handlers all note data, if there is no error all data writing successfully,
     * or nothing will be modified.
     * @param noteEntry     note content entry holder, together with note record
     *                  information content and attachments inside note.
     * @param context   context used to operate on files.
     * @param editable  content of note.
     * @return  true if no error, or false and do nothing.
     */
    public static boolean saveNote(NoteEntry noteEntry, Context context, Editable editable,
                                   List<ViewSpan> viewSpans){
        long noteId = noteEntry.getNoteId();
        boolean ret = true;
        boolean isNewRecord = false;
        // if it is a new note, create a directory for it
        if(noteId == Constants.NO_NOTE_ID) {
            // new note record
            // if it is a new note, create a directory fot it.
            isNewRecord = true;
            String notePath = createNoteDirectory(context);
            if(notePath == null){
                ret = false;
            }
            else noteEntry.setNotePath(notePath);
        }
        //if directory created error, ignore next step.
        if(ret) {
            if (FileUtil.writeFile(context, editable.toString(),
                    noteEntry.getNotePath() + File.separator + Constants.CONTENT_FILE_NAME)) {
                //if it is a new note, insert, or update it.
                if (isNewRecord) {
                    noteId = ANoteDBManager.getInstance(context).insertNoteRecord(noteEntry);
                    // if insert note record error, delete note directory
                    if (noteId == Constants.NO_NOTE_ID) {
                        Log.i(TAG, "saveNote: insert note record to database error");
                        FileUtil.deleteDirectoryAndFiles(noteEntry.getNotePath());
                        ret = false;
                    }
                } else
                    ANoteDBManager.getInstance(context).updateNoteRecord(noteEntry, ANoteDBManager.UpdateFlagTable.UPDATE_ALL);

                // if note record does not exist, ignore next step
                if (ret) {
                    noteEntry.setNoteId(noteId);
                    String resourceDir = createNoteResourceDirectory(noteEntry.getNotePath());
                    if(resourceDir == null)
                        ret = false;
                    // if resource directory does not exist, ignore next step
                    if (ret){
                        // TODO: 2018/3/13 handler ViewSpan removing situation
                        List<String> resourcePathList = new ArrayList<>();
                        for (ViewSpan viewSpan : viewSpans) {
                            ResourceDataEntry resourceDataEntry = viewSpan.getResourceDataEntry();
                            int spanStart = editable.getSpanStart(viewSpan);
                            if(spanStart == -1) {
                                if(viewSpan.getFilePath() != null)
                                    deleteResourceData(context, resourceDataEntry);
                            }else {
                                resourceDataEntry.setSpanStart(spanStart);
                                // set resource entry with the valid note id
                                resourceDataEntry.setNoteId(noteId);
                                String resourcePath = saveResourceData(context, resourceDir,
                                        resourceDataEntry, viewSpan.getResourceDataUri());
                                if (resourcePath != null) {
                                    resourcePathList.add(resourcePath);
                                }
                                // once there is a file saving occurs error,
                                // delete all new files created in this part.
                                else {
                                    for (String resourcePathToDelete : resourcePathList) {
                                        FileUtil.deleteFile(resourcePathToDelete);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            // if write content file error, delete the note directory
            else {
                Log.i(TAG, "saveNote: write note content error");
                if(noteId != Constants.NO_NOTE_ID)
                    FileUtil.deleteDirectoryAndFiles(noteEntry.getNotePath());
                ret = false;
            }
        }
        Log.i(TAG, "saveNote: save content file, id = " + noteId);
        return ret;
    }

    public static List<HomePageActivity.PreviewNoteEntry> getAllPreviewNoteList(Context context){
        List<HomePageActivity.PreviewNoteEntry> previewNoteEntries = new ArrayList<>();
        List<NoteEntry> noteEntries = ANoteDBManager.getInstance(context).queryAllNotesRecord();
        for (NoteEntry noteEntry : noteEntries) {
            HomePageActivity.PreviewNoteEntry previewNoteEntry = new HomePageActivity.PreviewNoteEntry();
            previewNoteEntry.noteEntry = noteEntry;
            previewNoteEntry.preResourceDataEntries =
                    ANoteDBManager.getInstance(context).queryAllResourceDataByNoteId(
                            previewNoteEntry.noteEntry.getNoteId());
            previewNoteEntries.add(previewNoteEntry);
        }
        return previewNoteEntries;
    }

    public static List<ResourceDataEntry> getResourceDataById(Context context, long noteId){
        return ANoteDBManager.getInstance(context).queryAllResourceDataByNoteId(noteId);
    }

    public static NoteEntry getSingleNote(Context context, long noteId){
        return ANoteDBManager.getInstance(context).querySingleNoteRecordById(noteId);
    }

    public static void removeNote(Context context, NoteEntry noteEntry){
        ANoteDBManager.getInstance(context).updateNoteRecord(noteEntry,
                ANoteDBManager.UpdateFlagTable.UPDATE_IS_LABELED_DISCARDED);
        deleteNote(context, noteEntry);
    }

    public static void deleteNote(Context context, NoteEntry noteEntry){
        ANoteDBManager.getInstance(context).deleteNoteRecord(noteEntry.getNoteId());
        FileUtil.deleteDirectoryAndFiles(noteEntry.getNotePath());
    }


    private static String createNoteDirectory(Context context){
        String directory;
        do{
            directory = context.getFilesDir().getAbsolutePath()+ File.separator +
                    RandomUtil.randomString(Constants.NOTE_DIRECTORY_LENGTH);
        }while (FileUtil.isFileOrDirectoryExist(directory));
        if(!FileUtil.createDir(directory)) {
            Log.i(TAG, "saveNote: new note create directory error");
            return null;
        }
        return directory;
    }

    /**
     * if note resource directory exists, do nothing, or create a new directory.
     * @param notePath  the note path which the resource corresponding to.
     * @return  if exists or successfully created, return the path, or false.
     */
    private static String createNoteResourceDirectory(String notePath){
        String resourceDir = notePath + File.separator
                + Constants.RESOURCE_DIR;
        if(!FileUtil.isFileOrDirectoryExist(resourceDir )){
            if (!FileUtil.createDir(resourceDir)) {
                Log.i(TAG, "saveNote: create resource data directory failed");
                return null;
            }
        }
        return resourceDir;
    }

    /**
     * Save resource file, and render a random name for the resource file.
     * @param context       save uri file need context.
     * @param resourceDir   resource directory of the note.
     * @param resourceDataEntry     resource data entry to be saved.
     * @param resourceUri   if a new resource file, the uri will be a valid value.
     * @return  the resource file path if successfully, or null.
     */
    private static String saveResourceData(Context context, String resourceDir,
                                           ResourceDataEntry resourceDataEntry, Uri resourceUri){
        String resourcePath = resourceDataEntry.getPath();
        // since ViewSpan's subclass have been restricted,
        // between filePath and there is and only one not null.
        if(resourceDataEntry.getPath() == null) {
            // resource data is new
            // first get a path for it
            resourcePath = generateResourceDataPath(resourceDir);
            resourceDataEntry.setPath(resourcePath);

            if (FileUtil.saveFileOfUri(context, resourceUri,
                    resourcePath)) {
                Log.i(TAG, "saveNote: save resource data successfully");
                if (ANoteDBManager.getInstance(context).insertResourceData(resourceDataEntry) == -1) {
                    FileUtil.deleteFile(resourcePath);
                    resourcePath = null;
                    Log.i(TAG, "saveNote: insert resource data record failed");
                }
            } else {
                resourcePath = null;
                Log.i(TAG, "saveNote: save resource data failed");
            }
        }
        // resource data record exist, so we just update inside text position information
        else {
            ANoteDBManager.getInstance(context).updateResourceData(resourceDataEntry);
        }
        return resourcePath;
    }

    /**
     * Generate a resource data path corresponding to the resource directory, and
     * ensure the return value will be a path have not been used before.
     * @param resourceDir   the directory for resource data to store.
     * @return  valid path string.
     */
    private static String generateResourceDataPath(String resourceDir){
        String resourcePath;
        do {
            resourcePath = resourceDir + File.separator +
                    RandomUtil.randomString(Constants.RESOURCE_FILE_NAME_LENGTH);
        }while(FileUtil.isFileOrDirectoryExist(resourcePath));
        return resourcePath;
    }

    private static void deleteResourceData(Context context, ResourceDataEntry resourceDataEntry){
        if(resourceDataEntry != null && resourceDataEntry.getPath() != null){
            ANoteDBManager.getInstance(context).deleteResourceData(resourceDataEntry.getResourceId());
            if (FileUtil.deleteFile(resourceDataEntry.getPath())) {
                Log.i(TAG, "deleteResourceData delete data file: successfully");
            }
            else Log.i(TAG, "deleteResourceData delete data file: failed");
        }
    }
}
