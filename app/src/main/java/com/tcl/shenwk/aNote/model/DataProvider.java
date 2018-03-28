package com.tcl.shenwk.aNote.model;

import android.content.Context;

import com.tcl.shenwk.aNote.entity.NoteEntity;
import com.tcl.shenwk.aNote.entity.NoteTagEntity;

import java.util.List;

/**
 * Provide data for whole application, when database or network has any update,
 * we will update cache data here, and other component can access new data.
 * Created by shenwk on 2018/3/27.
 */

public class DataProvider {
    private static final String TAG = "DataProvider";
    private static DataProvider mInstance;
    private List<NoteEntity> allNoteEntity;
    private List<NoteTagEntity> allNoteTagEntity;
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

    public List<NoteEntity> getAllNoteEntity(){
        if(allNoteEntity == null)
            updateNoteentity();
        return allNoteEntity;
    }

    public List<NoteTagEntity> getAllTopTagentity() {
        if(allNoteTagEntity == null)
            updateAllTopTagentity();
        return allNoteTagEntity;
    }

    public void updateNoteentity(){
        allNoteEntity = ANoteDBManager.getInstance(context).queryAllNoteRecord();
    }

    public void updateAllTopTagentity(){
        allNoteTagEntity = ANoteDBManager.getInstance(context).queryAllTopTag();
    }
}
