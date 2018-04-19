package com.tcl.shenwk.aNote.view.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import com.tcl.shenwk.aNote.R;
import com.tcl.shenwk.aNote.entity.NoteEntity;
import com.tcl.shenwk.aNote.data.ANoteDBManager;
import com.tcl.shenwk.aNote.data.DataProvider;
import com.tcl.shenwk.aNote.model.NoteHandler;
import com.tcl.shenwk.aNote.util.Constants;
import com.tcl.shenwk.aNote.view.activity.EditNoteActivity;
import com.tcl.shenwk.aNote.view.adapter.AllNoteDisplayAdapter;
import com.tcl.shenwk.aNote.view.adapter.AllNoteDisplayAdapter.PreviewNoteItem;

import java.util.List;

/**
 * Archived notes display fragment whose entry locates in navigation drawer.
 * Created by shenwk on 2018/3/29.
 */

public class ArchivedFragment extends BaseFragment{
    private static final String TAG = "ArchivedFragment";
    private static final int REQUEST_CODE_NEW_NOTE_EDIT = 0;
    private static final int REQUEST_CODE_SAVED_NOTE_EDIT = 1;
    private RecyclerView recyclerView;
    private AllNoteDisplayAdapter allNoteDisplayAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.note_list_layout, container, false);
        recyclerView = view.findViewById(R.id.note_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        List<PreviewNoteItem> previewNoteList =
                DataProvider.getInstance(getContext()).transformNoteEntityToPreviewList(
                        DataProvider.getInstance(getContext()).getArchivedNoteList()
                );
        Log.i(TAG, "onCreate: size " + previewNoteList.size());
        allNoteDisplayAdapter = new AllNoteDisplayAdapter(inflater,
                previewNoteList);
        allNoteDisplayAdapter.setOnItemClickListener(onItemClickListener);
        recyclerView.setAdapter(allNoteDisplayAdapter);

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CODE_NEW_NOTE_EDIT){
            // Maybe have added a new note, we will check
            if(resultCode == Activity.RESULT_OK && data.getSerializableExtra(Constants.ITEM_NOTE_ENTITY) != null){
                AllNoteDisplayAdapter adapter = (AllNoteDisplayAdapter) recyclerView.getAdapter();
                PreviewNoteItem previewNoteItem = new PreviewNoteItem();
                previewNoteItem.noteEntity = (NoteEntity) data.getSerializableExtra(Constants.ITEM_NOTE_ENTITY);

                previewNoteItem.preResourceDataEntries = ANoteDBManager.getInstance(
                        getContext()).queryAllResourceDataByNoteId(previewNoteItem.noteEntity.getNoteId());
                adapter.addItem(previewNoteItem);
            }
        } else if(requestCode == REQUEST_CODE_SAVED_NOTE_EDIT){
            // After a modification, we refresh data set inside adapter.
            if(resultCode == Activity.RESULT_OK && data.getSerializableExtra(Constants.ITEM_NOTE_ENTITY) != null){
                AllNoteDisplayAdapter adapter = (AllNoteDisplayAdapter) recyclerView.getAdapter();
                PreviewNoteItem previewNoteItem = new PreviewNoteItem();
                previewNoteItem.noteEntity = (NoteEntity) data.getSerializableExtra(Constants.ITEM_NOTE_ENTITY);
                previewNoteItem.preResourceDataEntries = ANoteDBManager.getInstance(
                        getContext()).queryAllResourceDataByNoteId(previewNoteItem.noteEntity.getNoteId());
                adapter.refreshSingleItemByPosition(data.getIntExtra(Constants.ITEM_POSITION, Constants.DEFAULT_ITEM_POSITION),
                        previewNoteItem);
            }
        }
    }

    AllNoteDisplayAdapter.OnItemClickListener onItemClickListener = new AllNoteDisplayAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position, View v) {
            PreviewNoteItem previewNoteItem = getAllNoteDisplayAdapter().getItemByPosition(position);
            Intent intent = new Intent(getContext(), EditNoteActivity.class);
            intent.putExtra(Constants.ACTION_TYPE_OF_EDIT_NOTE, EditNoteActivity.EDIT_TYPE_MODIFY);
            intent.putExtra(Constants.ITEM_NOTE_ENTITY, previewNoteItem.noteEntity);
            intent.putExtra(Constants.ITEM_POSITION, position);
            startActivityForResult(intent, REQUEST_CODE_SAVED_NOTE_EDIT);
        }

        @Override
        public void onItemLongClick(final int position, final View v) {
            PopupMenu popupMenu = new PopupMenu(getContext(), v);
            Menu menu = popupMenu.getMenu();
            menu.add("discard").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    AllNoteDisplayAdapter allNoteDisplayAdapter = getAllNoteDisplayAdapter();
                    NoteEntity noteEntity = allNoteDisplayAdapter.getItemByPosition(position).noteEntity;
                    Log.i(TAG, "onMenuItemClick: discard onClick");
                    noteEntity.setIsLabeledDiscarded(true);
                    NoteHandler.setNoteIsLabelDiscard(getContext(), noteEntity);
                    DataProvider.getInstance(getContext()).updateNoteEntity();
                    getAllNoteDisplayAdapter().removeItemByPosition(position);
                    allNoteDisplayAdapter.notifyItemRemoved(position);
                    return true;
                }
            });
            menu.add("unarchived").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    AllNoteDisplayAdapter allNoteDisplayAdapter = getAllNoteDisplayAdapter();
                    NoteEntity noteEntity = allNoteDisplayAdapter.getItemByPosition(position).noteEntity;
                    Log.i(TAG, "onMenuItemClick: unarchived onClick");
                    noteEntity.setHasArchived(false);
                    NoteHandler.setNoteHasArchived(getContext(), noteEntity);

                    DataProvider.getInstance(getContext()).updateNoteEntity();
                    getAllNoteDisplayAdapter().removeItemByPosition(position);
                    allNoteDisplayAdapter.notifyItemRemoved(position);
                    return true;
                }
            });
            menu.add("detail");
            popupMenu.setGravity(Gravity.END);
            popupMenu.show();
        }
    };

    public AllNoteDisplayAdapter getAllNoteDisplayAdapter(){
        return allNoteDisplayAdapter;
    }

    public RecyclerView getRecyclerView(){
        return recyclerView;
    }

    @Override
    public void reload() {

    }
}
