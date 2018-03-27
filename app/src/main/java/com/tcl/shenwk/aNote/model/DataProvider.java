package com.tcl.shenwk.aNote.model;

import android.content.Context;
import android.widget.LinearLayout;

import com.tcl.shenwk.aNote.entry.NoteEntry;
import com.tcl.shenwk.aNote.entry.NoteTagEntry;

import java.util.List;

/**
 * Provide data for whole application, when database or network has any update,
 * we will update cache data here, and other component can access new data.
 * Created by shenwk on 2018/3/27.
 */

public class DataProvider {
    private static final String TAG = "DataProvider";
    private static DataProvider mInstance;
    private List<NoteEntry> allNoteEntry;
    private List<NoteTagEntry> allNoteTagEntry;
    private Context context;

    private DataProvider(Context context){
        this.context = context;
    }

    public static DataProvider getInstance(Context context){
        if(mInstance == null) {
            mInstance = new DataProvider(context.getApplicationContext());
        }
        return mInstance;
    }

    public List<NoteEntry> getAllNoteEntry(){
        if(allNoteEntry == null)
            updateNoteEntry();
        return allNoteEntry;
    }

    public List<NoteTagEntry> getAllNoteTagEntry() {
        if(allNoteTagEntry == null)
            updateNoteTagEntry();
        return allNoteTagEntry;
    }

    public void updateNoteEntry(){
        allNoteEntry = ANoteDBManager.getInstance(context).queryAllNoteRecord();
    }

    public void updateNoteTagEntry(){
        allNoteTagEntry = ANoteDBManager.getInstance(context).queryAllTag();
    }
}
