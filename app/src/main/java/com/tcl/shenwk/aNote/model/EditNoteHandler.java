package com.tcl.shenwk.aNote.model;

import android.content.Context;
import android.text.Editable;
import android.util.Log;

import com.tcl.shenwk.aNote.entry.NoteEntry;
import com.tcl.shenwk.aNote.entry.ResourceDataEntry;
import com.tcl.shenwk.aNote.util.Constants;
import com.tcl.shenwk.aNote.util.FileUtil;
import com.tcl.shenwk.aNote.util.RandomUtil;
import com.tcl.shenwk.aNote.view.customSpan.ViewSpan;

import java.io.File;
import java.util.List;

/**
 * Note operation handler, include editing note and managing note.
 * Created by shenwk on 2018/2/2.
 */

public class EditNoteHandler {
    private static String TAG = "EditNoteHandler";
    /**
     *
     * @param noteEntry     note content entry holder, together with note record
     *                  information content and attachments inside note.
     * @param context   context used to operate on files.
     * @param editable  content of note.
     * @return  return to tell whether this operation successfully.
     */
    public static boolean saveNote(NoteEntry noteEntry, Context context, Editable editable, List<ViewSpan> viewSpans){
        long noteId = noteEntry.getNoteId();
        boolean ret = true;
        boolean isNewRecord = false;
        if(noteId == Constants.NO_NOTE_ID) {
            // new note record
            // if it is a new note, create a directory fot it.
            isNewRecord = true;
            noteEntry.setNotePath(context.getFilesDir().getAbsolutePath()+ File.separator +
                    RandomUtil.randomString(Constants.NOTE_DIRECTORY_LENGTH));
            if(!FileUtil.createDir(noteEntry.getNotePath())) {
                Log.i(TAG, "saveNote: new note create directory error");
                ret = false;
            }
        }
        //if directory created error, ignore next step.
        if(ret) {
            if (FileUtil.writeFile(context, noteEntry.getNoteContent(),
                    noteEntry.getNotePath() + File.separator + Constants.CONTENT_FILE_NAME)) {
                if (isNewRecord) {
                    noteId = ANoteDBManager.getInstance(context).insertNoteRecord(noteEntry);
                    if (noteId == -1) {
                        Log.i(TAG, "saveNote: insert note record to database error");
                        // TODO: 2018/3/10 undo failed data storing
                        ret = false;
                    }
                } else
                    ANoteDBManager.getInstance(context).updateNoteRecord(noteEntry, ANoteDBManager.UpdateFlagTable.UPDATE_ALL);
                if (ret) {
                    noteEntry.setNoteId(noteId);
                    String resourceDir = noteEntry.getNotePath() + File.separator
                            + Constants.RESOURCE_DIR;
                    if (!FileUtil.createDir(resourceDir)) {
                        Log.i(TAG, "saveNote: create resource data directory failed");
                        ret = false;
                    } else {
                        for (ViewSpan viewSpan : viewSpans) {
                            // since ViewSpan's subclass have been restricted,
                            // between filePath and there is and only one not null.
                            ResourceDataEntry resourceDataEntry = new ResourceDataEntry();
                            resourceDataEntry.setSpanStart(editable.getSpanStart(viewSpan));
                            resourceDataEntry.setNoteId(noteId);
                            String resourcePath;
                            do {
                                resourcePath = resourceDir + File.separator +
                                        RandomUtil.randomString(Constants.RESOURCE_FILE_NAME_LENGTH);
                            }while(FileUtil.isFileExist(resourcePath));
                            resourceDataEntry.setPath(resourcePath);
                            resourceDataEntry.setFileName(viewSpan.getFileName());
                            resourceDataEntry.setDataType(viewSpan.getResourceDataType());
                            if(viewSpan.getFilePath() == null) {
                                if (FileUtil.saveFileOfUri(context, viewSpan.getResourceDataUri(),
                                        resourceDataEntry.getPath())) {
                                    Log.i(TAG, "saveNote: save resource data successfully");
                                    ret = ANoteDBManager.getInstance(context).insertResourceData(noteId,
                                            resourceDataEntry) != -1;
                                } else {
                                    ret = false;
                                    Log.i(TAG, "saveNote: save resource data failed");
                                    break;
                                }
                            }
                            else {
                                ret = ANoteDBManager.getInstance(context).insertResourceData(noteId,
                                        resourceDataEntry) != -1;
                            }
                        }
                    }
                }
            }
            else {
                Log.i(TAG, "saveNote: write note content error");
                // TODO: 2018/3/10 undo failed data storing
                ret = false;
            }
        }
        Log.i(TAG, "saveNote: save content file, id = " + noteId);
        return ret;
    }

    public static List<NoteEntry> getAllNotesList(Context context){
        List<NoteEntry> noteEntries = ANoteDBManager.getInstance(context).queryAllNotesRecord();
        for (NoteEntry noteEntry : noteEntries) {
            noteEntry.setNoteContent(FileUtil.readFile(context, noteEntry.getNotePath()
                    + File.separator + Constants.CONTENT_FILE_NAME));
        }
        return noteEntries;
    }

    public static List<ResourceDataEntry> getResourceDataById(Context context, long noteId){
        return ANoteDBManager.getInstance(context).queryAllResourceDataByNoteId(noteId);
    }

    public static NoteEntry getSingleNote(Context context, long noteId){
         NoteEntry noteEntry = ANoteDBManager.getInstance(context).querySingleNoteRecordById(noteId);
         noteEntry.setNoteContent(FileUtil.readFile(context, noteEntry.getNotePath()
                 + File.separator + Constants.CONTENT_FILE_NAME));
         return noteEntry;
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
}
