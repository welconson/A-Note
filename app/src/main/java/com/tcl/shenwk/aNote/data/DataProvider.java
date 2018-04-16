package com.tcl.shenwk.aNote.data;

import android.content.Context;

import com.tcl.shenwk.aNote.entity.NoteEntity;
import com.tcl.shenwk.aNote.entity.NoteTagEntity;
import com.tcl.shenwk.aNote.view.adapter.AllNoteDisplayAdapter.PreviewNoteItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Provide data for whole application, when database or network has any update,
 * we will update cache data here, and other component can access new data.
 * Created by shenwk on 2018/3/27.
 */

public class DataProvider {
    private static final String TAG = "DataProvider";
    private static DataProvider mInstance;
    private List<NoteEntity> allNoteEntities;
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

    public List<NoteEntity> getAllNoteEntities(){
        if(allNoteEntities == null)
            updateNoteEntity();
        return allNoteEntities;
    }

    public List<NoteTagEntity> getAllTopTagEntity() {
        if(allNoteTagEntity == null)
            updateAllTopTagEntity();
        return allNoteTagEntity;
    }

    public void updateNoteEntity(){
        allNoteEntities = ANoteDBManager.getInstance(context).queryAllNoteRecord();
    }

    public void updateAllTopTagEntity(){
        allNoteTagEntity = ANoteDBManager.getInstance(context).queryAllTopTag();
    }

    public List<PreviewNoteItem> transformNoteEntityToPreviewList(List<NoteEntity> noteEntities){
        List<PreviewNoteItem> previewNoteEntries = new ArrayList<>();
        for (NoteEntity noteEntity : noteEntities) {
            PreviewNoteItem previewNoteItem = new PreviewNoteItem();
            previewNoteItem.noteEntity = noteEntity;
            previewNoteItem.preResourceDataEntries =
                    ANoteDBManager.getInstance(context).queryAllResourceDataByNoteId(
                            previewNoteItem.noteEntity.getNoteId());
            previewNoteEntries.add(previewNoteItem);
        }
        return previewNoteEntries;
    }

    public List<NoteEntity> getArchivedNoteList(){
        List<NoteEntity> noteEntities = new ArrayList<>();
        allNoteEntities = getAllNoteEntities();
        for(NoteEntity noteEntity : allNoteEntities){
            if(noteEntity.hasArchived() && !noteEntity.isLabeledDiscarded())
                noteEntities.add(noteEntity);
        }
        return noteEntities;
    }

    public List<NoteEntity> getUnarchivedNoteList(){
        List<NoteEntity> noteEntities = new ArrayList<>();
        allNoteEntities = getAllNoteEntities();
        for(NoteEntity noteEntity : allNoteEntities){
            if(!noteEntity.hasArchived() && !noteEntity.isLabeledDiscarded())
                noteEntities.add(noteEntity);
        }
        return noteEntities;
    }

    public List<NoteEntity> getLabeledDiscardNoteList(){
        List<NoteEntity> noteEntities = new ArrayList<>();
        allNoteEntities = getAllNoteEntities();
        for(NoteEntity noteEntity : allNoteEntities){
            if(noteEntity.isLabeledDiscarded())
                noteEntities.add(noteEntity);
        }
        return noteEntities;
    }
}
