package com.tcl.shenwk.aNote.view.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tcl.shenwk.aNote.R;
import com.tcl.shenwk.aNote.entry.NoteEntry;
import com.tcl.shenwk.aNote.model.ANoteDBManager;
import com.tcl.shenwk.aNote.model.DataProvider;
import com.tcl.shenwk.aNote.model.NoteHandler;
import com.tcl.shenwk.aNote.util.Constants;
import com.tcl.shenwk.aNote.view.activity.EditNoteActivity;
import com.tcl.shenwk.aNote.view.activity.HomePageActivity;
import com.tcl.shenwk.aNote.view.adapter.AllNoteDisplayAdapter;

import java.util.List;

/**
 * All note display fragment.
 * Created by shenwk on 2018/3/23.
 */

public class AllNoteFragment extends Fragment {
    private static final String TAG = "AllNoteFragment";
    private static final int REQUEST_CODE_NEW_NOTE_EDIT = 0;
    private static final int REQUEST_CODE_SAVED_NOTE_EDIT = 1;
    private RecyclerView recyclerView;
    private List<NoteEntry> noteEntries;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.all_note_layout, container, false);
        recyclerView = view.findViewById(R.id.all_note_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        noteEntries = DataProvider.getInstance(getContext()).getAllNoteEntry();
        List<HomePageActivity.PreviewNoteEntry> previewNoteList =
                NoteHandler.transformNoteEntryToPreviewList(getContext(), noteEntries);
        Log.i(TAG, "onCreate: size " + previewNoteList.size());
        AllNoteDisplayAdapter allNoteDisplayAdapter = new AllNoteDisplayAdapter(inflater,
                previewNoteList);
        allNoteDisplayAdapter.setOnItemClickListener(onItemClickListener);
        recyclerView.setAdapter(allNoteDisplayAdapter);

        FloatingActionButton fab = view.findViewById(R.id.add_note_button);
        fab.setOnClickListener(fabListener);

        return view;
    }

    private FloatingActionButton.OnClickListener fabListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getContext(), EditNoteActivity.class);
            intent.putExtra(Constants.ACTION_TYPE_OF_EDIT_NOTE, EditNoteActivity.EDIT_TYPE_CREATE);
            startActivityForResult(intent, REQUEST_CODE_NEW_NOTE_EDIT);
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CODE_NEW_NOTE_EDIT){
            // Maybe have added a new note, we will check
            if(resultCode == Activity.RESULT_OK && data.getSerializableExtra(Constants.ITEM_NOTE_ENTRY) != null){
                AllNoteDisplayAdapter adapter = (AllNoteDisplayAdapter) recyclerView.getAdapter();
                HomePageActivity.PreviewNoteEntry previewNoteEntry = new HomePageActivity.PreviewNoteEntry();
                previewNoteEntry.noteEntry = (NoteEntry) data.getSerializableExtra(Constants.ITEM_NOTE_ENTRY);
                // Add the new note to homepage all note list
                noteEntries.add(0, previewNoteEntry.noteEntry);
                previewNoteEntry.preResourceDataEntries = ANoteDBManager.getInstance(
                        getContext()).queryAllResourceDataByNoteId(previewNoteEntry.noteEntry.getNoteId());
                adapter.addItem(previewNoteEntry);
            }
        } else if(requestCode == REQUEST_CODE_SAVED_NOTE_EDIT){
            // After a modification, we refresh data set inside adapter.
            if(resultCode == Activity.RESULT_OK && data.getSerializableExtra(Constants.ITEM_NOTE_ENTRY) != null){
                AllNoteDisplayAdapter adapter = (AllNoteDisplayAdapter) recyclerView.getAdapter();
                HomePageActivity.PreviewNoteEntry previewNoteEntry = new HomePageActivity.PreviewNoteEntry();
                previewNoteEntry.noteEntry = (NoteEntry) data.getSerializableExtra(Constants.ITEM_NOTE_ENTRY);
                previewNoteEntry.preResourceDataEntries = ANoteDBManager.getInstance(
                        getContext()).queryAllResourceDataByNoteId(previewNoteEntry.noteEntry.getNoteId());
                adapter.refreshSingleItemByPosition(data.getIntExtra(Constants.ITEM_POSITION, Constants.DEFAULT_ITEM_POSITION),
                        previewNoteEntry);
            }
        }
    }

    AllNoteDisplayAdapter.OnItemClickListener onItemClickListener = new AllNoteDisplayAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position, NoteEntry noteEntry) {
            Intent intent = new Intent(getContext(), EditNoteActivity.class);
            intent.putExtra(Constants.ACTION_TYPE_OF_EDIT_NOTE, EditNoteActivity.EDIT_TYPE_MODIFY);
            intent.putExtra(Constants.ITEM_NOTE_ENTRY, noteEntry);
            intent.putExtra(Constants.ITEM_POSITION, position);
            startActivityForResult(intent, REQUEST_CODE_SAVED_NOTE_EDIT);
        }
    };
}
