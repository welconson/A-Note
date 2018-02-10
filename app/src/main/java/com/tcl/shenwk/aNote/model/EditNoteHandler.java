package com.tcl.shenwk.aNote.model;

import android.content.Context;
import android.util.Log;

import com.tcl.shenwk.aNote.entry.NoteEntry;
import com.tcl.shenwk.aNote.util.Constants;
import com.tcl.shenwk.aNote.util.FileUtil;
import com.tcl.shenwk.aNote.util.RandomUtil;
import com.tcl.shenwk.aNote.view.activity.EditNoteActivity;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * EditNoteActivity operation handler.
 * Created by shenwk on 2018/2/2.
 */

public class EditNoteHandler {
    private static String TAG = "EditNoteHandler";
    /**
     *
     * @param noteEntry note content entry holder, together with note record
     *                  information content and attachments inside note.
     * @param context context used to operate on files.
     * @return  return to tell whether this operation successfully.
     */
    public static boolean saveNote(NoteEntry noteEntry, Context context){
        long noteId = noteEntry.getNoteId();
        boolean ret = true;
        if(noteId == Constants.NO_NOTE_ID) {
            // new note record
            noteEntry.setNoteContentPath(RandomUtil.randomString(Constants.CONTENT_FILE_NAME_LENGTH));
            if(FileUtil.writeFile(context, noteEntry.getNoteContent(), noteEntry.getNoteContentPath())) {
                noteId = ANoteDBManager.getInstance(context).insertNoteRecord(noteEntry);
                if (noteId == -1)
                    ret = false;
                else
                    noteEntry.setNoteId(noteId);
            }
            else ret = false;
        }
        else {
            // saved note
            FileUtil.writeFile(context, noteEntry.getNoteContent(), noteEntry.getNoteContentPath());
            ANoteDBManager.getInstance(context).updateNoteRecord(noteEntry, ANoteDBManager.UpdateFlagTable.UPDATE_ALL);
        }
        Log.i(TAG, "saveNote: save content file, id = " + noteId);
        return ret;
    }

    public static List<NoteEntry> getAllNotesList(Context context){
        List<NoteEntry> noteEntries = ANoteDBManager.getInstance(context).queryAllNotesRecord();
        for (NoteEntry noteEntry : noteEntries) {
            noteEntry.setNoteContent(FileUtil.readNoteContent(context, noteEntry.getNoteContentPath()));
        }
        return noteEntries;
    }

    public static NoteEntry getSingleNote(Context context, long noteId){
         NoteEntry noteEntry = ANoteDBManager.getInstance(context).querySingleNoteRecordById(noteId);
         noteEntry.setNoteContent(FileUtil.readNoteContent(context, noteEntry.getNoteContentPath()));
         return noteEntry;
    }

    public static void removeNote(Context context, NoteEntry noteEntry){
        ANoteDBManager.getInstance(context).updateNoteRecord(noteEntry,
                ANoteDBManager.UpdateFlagTable.UPDATE_IS_LABELED_DISCARDED);
        deleteNote(context, noteEntry);
    }

    public static void deleteNote(Context context, NoteEntry noteEntry){
        ANoteDBManager.getInstance(context).deleteNoteRecord(noteEntry.getNoteId());
        FileUtil.deleteFile(noteEntry.getNoteContentPath());
    }
}
