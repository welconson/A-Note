package com.tcl.shenwk.aNote.view.fragment;

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
import com.tcl.shenwk.aNote.model.NoteHandler;
import com.tcl.shenwk.aNote.util.Constants;
import com.tcl.shenwk.aNote.util.StringUtil;
import com.tcl.shenwk.aNote.view.activity.EditNoteActivity;
import com.tcl.shenwk.aNote.view.activity.HomePageActivity;
import com.tcl.shenwk.aNote.view.adapter.CustomAdapter;

import java.util.List;

/**
 * Created by shenwk on 2018/3/23.
 */

public class AllNoteFragment extends Fragment {
    private static final String TAG = "AllNoteFragment";
    private RecyclerView recyclerView;
    private List<NoteEntry> noteEntries;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.all_note_layout, container, false);
        recyclerView = view.findViewById(R.id.all_note_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        noteEntries = ((HomePageActivity) getActivity()).getNoteEntries();
        if(noteEntries == null){
            noteEntries = ANoteDBManager.getInstance(getContext()).queryAllNoteRecord();
        }
        List<HomePageActivity.PreviewNoteEntry> previewNoteList =
                NoteHandler.transformNoteEntryToPreviewList(getContext(), noteEntries);
        Log.i(TAG, "onCreate: size " + previewNoteList.size());
        CustomAdapter customAdapter = new CustomAdapter(inflater,
                previewNoteList);
        recyclerView.setAdapter(customAdapter);

        FloatingActionButton fab = view.findViewById(R.id.add_note_button);
        fab.setOnClickListener(fabListener);

        return view;
    }

    private FloatingActionButton.OnClickListener fabListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getContext(), EditNoteActivity.class);
            intent.putExtra(Constants.ACTION_EDIT_NOTE, EditNoteActivity.EDIT_TYPE_CREATE);
            startActivity(intent);
        }
    };

    private HomePageActivity.OnEditActivityFinishedListener OnEditActivityFinishedListener = new HomePageActivity.OnEditActivityFinishedListener() {
        @Override
        public void onEditActivityFinished(Intent intent) {
            // Examine whether any note has been modified.
            // If modified, update RecyclerView data set and display.
            if(intent.getIntExtra(Constants.EDIT_TO_HOME_PAGE_STATUS, Constants.HOME_PAGE_NORMAL_RESUME)
                    == Constants.HOME_PAGE_UPDATE_RESUME) {
                CustomAdapter adapter = (CustomAdapter) recyclerView.getAdapter();
                String action = intent.getStringExtra(Constants.ACTION_EDIT_NOTE);
                HomePageActivity.PreviewNoteEntry previewNoteEntry = new HomePageActivity.PreviewNoteEntry();
                previewNoteEntry.noteEntry = (NoteEntry) intent.getSerializableExtra(Constants.ITEM_NOTE_ENTRY);
                // Add the new note to homepage all note list
                noteEntries.add(0, previewNoteEntry.noteEntry);
                previewNoteEntry.preResourceDataEntries = ANoteDBManager.getInstance(
                        getContext()).queryAllResourceDataByNoteId(previewNoteEntry.noteEntry.getNoteId());
                if (StringUtil.equal(action, EditNoteActivity.EDIT_TYPE_MODIFY)) {
                    adapter.refreshSingleItemByPosition(intent.getIntExtra(Constants.ITEM_POSITION, Constants.DEFAULT_ITEM_POSITION),
                            previewNoteEntry);
                } else {
                    adapter.addItem(previewNoteEntry);
                }
                intent.putExtra(Constants.EDIT_TO_HOME_PAGE_STATUS, Constants.HOME_PAGE_NORMAL_RESUME);
            }
        }
    };

    public HomePageActivity.OnEditActivityFinishedListener getOnEditActivityFinishedListener() {
        return OnEditActivityFinishedListener;
    }
}
