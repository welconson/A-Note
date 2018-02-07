package com.tcl.shenwk.aNote.model;

import android.content.Context;
import android.util.Log;

import com.tcl.shenwk.aNote.entry.NoteEntry;
import com.tcl.shenwk.aNote.util.Constants;
import com.tcl.shenwk.aNote.util.FileUtil;

import java.util.Date;
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
        if(noteId == Constants.NO_NOTE_ID) {
            noteId = ANoteDBManager.getInstance(context).insertNoteRecord(noteEntry);
            if(noteId == -1)
                return false;
            noteEntry.setNoteId(noteId);
            Log.i(TAG, "saveNote: create new content file, id = " + noteId);
        }
        else {
            ANoteDBManager.getInstance(context).updateNoteRecord(noteEntry);
            Log.i(TAG, "saveNote: with exist content file");
        }
        return FileUtil.writeFile(context, Constants.CONTENT_FILE_NAME, noteId);
    }

    public static List<NoteEntry> getAllNotesList(Context context){
        return ANoteDBManager.getInstance(context).queryAllNotesRecord();
    }
}
