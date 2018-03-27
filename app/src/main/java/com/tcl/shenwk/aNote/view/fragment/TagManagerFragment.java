package com.tcl.shenwk.aNote.view.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tcl.shenwk.aNote.R;
import com.tcl.shenwk.aNote.entry.NoteEntry;
import com.tcl.shenwk.aNote.entry.NoteTagEntry;
import com.tcl.shenwk.aNote.entry.TagRecordEntry;
import com.tcl.shenwk.aNote.model.ANoteDBManager;
import com.tcl.shenwk.aNote.model.DataProvider;
import com.tcl.shenwk.aNote.util.Constants;
import com.tcl.shenwk.aNote.view.activity.EditNoteActivity;
import com.tcl.shenwk.aNote.view.activity.HomePageActivity;
import com.tcl.shenwk.aNote.view.adapter.TagManagerAdapter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

/**
 * Tags item's user interface of navigation drawer, implementing with a fragment.
 * Created by shenwk on 2018/3/23.
 */

public class TagManagerFragment extends Fragment implements HomePageActivity.OnKeyDownListener{
    private static final String TAG = "TagManagerFragment";
    private RecyclerView recyclerView;
    private List<NoteTagEntry> noteTagEntries;
    private List<NoteEntry> allNoteEntries;

    private Stack<TagManagerAdapter> tagManagerAdapterStack;
    private TagManagerAdapter currentAdapter;
    private LinearLayout linearLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tag_manager_layout, container, false);
        linearLayout = view.findViewById(R.id.tag_hierarchy_hint);
        addHierarchyTitle(getResources().getString(R.string.root_tag_hierarchy));

        noteTagEntries = DataProvider.getInstance(getContext()).getAllNoteTagEntry();
        recyclerView = view.findViewById(R.id.recycler_view);

        TagManagerAdapter tagManagerAdapter = new TagManagerAdapter(noteTagEntries, new ArrayList<NoteEntry>(), getActivity().getLayoutInflater());
        tagManagerAdapter.setOnItemClickListener(onItemClickListener);
        recyclerView.setAdapter(tagManagerAdapter);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        tagManagerAdapterStack = new Stack<>();
        currentAdapter = tagManagerAdapter;

        allNoteEntries = DataProvider.getInstance(getContext()).getAllNoteEntry();

        Log.i(TAG, "onCreateView: ");
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 0){
            if(resultCode == Activity.RESULT_OK && data.getSerializableExtra(Constants.ITEM_NOTE_ENTRY) != null){
                NoteEntry noteEntry = (NoteEntry) data.getSerializableExtra(Constants.ITEM_NOTE_ENTRY);
                currentAdapter.refreshSingleItemByPosition(data.getIntExtra(Constants.ITEM_POSITION, Constants.DEFAULT_ITEM_POSITION),
                        noteEntry);
            }
        }
    }

    private TagManagerAdapter.OnItemClickListener onItemClickListener = new TagManagerAdapter.OnItemClickListener() {
        @Override
        public void onTagClick(TagManagerAdapter.TagItem tagItem) {
            Log.i(TAG, "onTagClick: ");
            switchToChildHierarchy(tagItem);
        }

        @Override
        public void onNoteClick(int position, TagManagerAdapter.NoteItem noteItem) {
            Log.i(TAG, "onNoteClick: ");
            NoteEntry noteEntry = noteItem.noteEntry;
            Intent intent = new Intent(getContext(), EditNoteActivity.class);
            intent.putExtra(Constants.ACTION_TYPE_OF_EDIT_NOTE, EditNoteActivity.EDIT_TYPE_MODIFY);
            intent.putExtra(Constants.ITEM_POSITION, position);
            intent.putExtra(Constants.ITEM_NOTE_ENTRY, noteEntry);
            startActivityForResult(intent, 0);
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
        switch (keyCode){
            case KeyEvent.KEYCODE_BACK:
                Log.i(TAG, "onKeyDown: back");
                if(!tagManagerAdapterStack.empty())
                    backToParentHierarchy();
                else return false;

        }
        return true;
    }

    private void switchToChildHierarchy(TagManagerAdapter.TagItem tagItem){
        TagManagerAdapter tagManagerAdapter = new TagManagerAdapter(tagItem.getSubTagEntries(),
                getNoteEntryByNoteTagRecord(tagItem.getTagRecordEntries()), getActivity().getLayoutInflater());
        tagManagerAdapterStack.push(currentAdapter);
        currentAdapter = tagManagerAdapter;
        tagManagerAdapter.setOnItemClickListener(onItemClickListener);
        recyclerView.setAdapter(tagManagerAdapter);

        addHierarchyTitle(tagItem.getNoteTagEntry().getTagName());
    }

    private void backToParentHierarchy(){
        TagManagerAdapter tagManagerAdapter = tagManagerAdapterStack.pop();
        currentAdapter = tagManagerAdapter;
        recyclerView.setAdapter(tagManagerAdapter);

        removeHierarchyTitle();
    }

    private List<NoteEntry> getNoteEntryByNoteTagRecord(List<TagRecordEntry> tagRecordEntries){
        List<NoteEntry> noteEntries = new ArrayList<>();
        Iterator<TagRecordEntry> iterator = tagRecordEntries.iterator();
        if(iterator.hasNext()){
            TagRecordEntry tagRecordEntry = iterator.next();
            for(NoteEntry noteEntry : allNoteEntries){
                if(tagRecordEntry.getNoteId() == noteEntry.getNoteId()){
                    noteEntries.add(noteEntry);
                    if(iterator.hasNext()){
                        tagRecordEntry = iterator.next();
                    }
                    else break;
                }
            }
        }
        return noteEntries;
    }

    private void addHierarchyTitle(String hierarchyTitle){
        View hierarchyView = getActivity().getLayoutInflater().inflate(R.layout.tag_hierarchy_item, linearLayout, false);
        ((TextView) hierarchyView.findViewById(R.id.hierarchy_title)).setText(hierarchyTitle);
        linearLayout.addView(hierarchyView);
    }

    private void removeHierarchyTitle(){
        int childCount = linearLayout.getChildCount();
        if(childCount > 0){
            linearLayout.removeViewAt(childCount - 1);
        } else Log.i(TAG, "removeHierarchyTitle: no more hierarchy inside hierarchy directory to remove");
    }
}
