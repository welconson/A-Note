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
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.tcl.shenwk.aNote.R;
import com.tcl.shenwk.aNote.entity.NoteEntity;
import com.tcl.shenwk.aNote.entity.NoteTagEntity;
import com.tcl.shenwk.aNote.entity.TagRecordEntity;
import com.tcl.shenwk.aNote.model.ANoteDBManager;
import com.tcl.shenwk.aNote.model.DataProvider;
import com.tcl.shenwk.aNote.util.Constants;
import com.tcl.shenwk.aNote.util.DateUtil;
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
    private static final int REQUEST_CODE_NEW_NOTE_EDIT = 0;
    private static final int REQUEST_CODE_OLD_NOTE_EDIT = 1;
    private RecyclerView recyclerView;
    private List<NoteTagEntity> noteTagEntries;
    private List<NoteEntity> allNoteEntries;

    private Stack<TagManagerAdapter> tagManagerAdapterStack;
    private TagManagerAdapter currentAdapter;
    private ViewGroup hierarchyDisplayView;
    private HorizontalScrollView scrollView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tag_manager_layout, container, false);
        scrollView = view.findViewById(R.id.scroll_view);
        hierarchyDisplayView = view.findViewById(R.id.tag_hierarchy_hint);
        addHierarchyTitle(getResources().getString(R.string.root_tag_hierarchy));

        noteTagEntries = DataProvider.getInstance(getContext()).getAllTopTagentity();
        recyclerView = view.findViewById(R.id.recycler_view);

        TagManagerAdapter tagManagerAdapter = new TagManagerAdapter(noteTagEntries, new ArrayList<NoteEntity>(), getActivity().getLayoutInflater());
        tagManagerAdapter.setOnItemClickListener(onItemClickListener);
        recyclerView.setAdapter(tagManagerAdapter);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        tagManagerAdapterStack = new Stack<>();
        currentAdapter = tagManagerAdapter;

        allNoteEntries = DataProvider.getInstance(getContext()).getAllNoteEntity();

        FloatingActionButton fab = view.findViewById(R.id.add);
        fab.setOnClickListener(fabOnClickListener);

        Log.i(TAG, "onCreateView: ");
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        allNoteEntries = DataProvider.getInstance(getContext()).getAllNoteEntity();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                // Check whether note edition influence the adapter binding data, and update it 
                case REQUEST_CODE_OLD_NOTE_EDIT: {
                    NoteEntity noteEntity = (NoteEntity) data.getSerializableExtra(Constants.ITEM_NOTE_entity);
                    if(noteEntity == null)
                        break;
                    NoteTagEntity noteTagEntity = currentAdapter.getHierarchyTagentity();
                    if (noteTagEntity != null && ANoteDBManager.getInstance(getContext()).
                            queryTagRecordByDoubleId(noteTagEntity.getTagId(), noteEntity.getNoteId()) == null) {
                        currentAdapter.removeItem(data.getIntExtra(Constants.ITEM_POSITION, Constants.DEFAULT_ITEM_POSITION));
                    } else {
                        currentAdapter.refreshNoteItemByPosition(data.getIntExtra(Constants.ITEM_POSITION, Constants.DEFAULT_ITEM_POSITION),
                                noteEntity);
                    }
                    break;
                }
                case REQUEST_CODE_NEW_NOTE_EDIT: {
                    NoteEntity noteEntity = (NoteEntity) data.getSerializableExtra(Constants.ITEM_NOTE_entity);
                    if(noteEntity == null)
                        break;
                    NoteTagEntity noteTagEntity = currentAdapter.getHierarchyTagentity();
                    if (noteTagEntity != null && ANoteDBManager.getInstance(getContext()).
                            queryTagRecordByDoubleId(noteTagEntity.getTagId(), noteEntity.getNoteId()) != null) {
                        currentAdapter.insertItem(noteEntity);
                    }
                    DataProvider.getInstance(getContext()).updateNoteentity();
                    break;
                }
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
        public void onTagLongClick(View v, final int position, final TagManagerAdapter.TagItem tagItem) {
            PopupMenu popupMenu = new PopupMenu(getContext(), v);
            Menu menu = popupMenu.getMenu();
            menu.add("delete");
            menu.getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (tagItem.getSubTagEntries() != null && tagItem.getTagRecordEntries() != null &&
                            tagItem.getSubTagEntries().size() == 0 && tagItem.getTagRecordEntries().size() == 0) {
                        NoteTagEntity noteTagEntity = tagItem.getNoteTagEntity();
                        boolean needUpdateTopentity = noteTagEntity.getRootTagId() == Constants.NO_TAG_ID;
                        ANoteDBManager.getInstance(getContext()).deleteTag(noteTagEntity.getTagId());
                        if(needUpdateTopentity)
                            DataProvider.getInstance(getContext()).updateAllTopTagentity();
                        currentAdapter.removeItem(position);
                    } else {
                        Toast.makeText(getContext(), Constants.TOAST_TAG_DELETE_FAILED, Toast.LENGTH_LONG).show();
                    }
                    Log.i(TAG, "onMenuItemClick: delete onClick");
                    return true;
                }
            });
            menu.add("detail");
            popupMenu.setGravity(Gravity.END);
            popupMenu.show();
        }

        @Override
        public void onNoteClick(int position, TagManagerAdapter.NoteItem noteItem) {
            Log.i(TAG, "onNoteClick: ");
            NoteEntity noteEntity = noteItem.noteEntity;
            Intent intent = new Intent(getContext(), EditNoteActivity.class);
            intent.putExtra(Constants.ACTION_TYPE_OF_EDIT_NOTE, EditNoteActivity.EDIT_TYPE_MODIFY);
            intent.putExtra(Constants.ITEM_POSITION, position);
            intent.putExtra(Constants.ITEM_NOTE_entity, noteEntity);
            startActivityForResult(intent, REQUEST_CODE_OLD_NOTE_EDIT);
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
                getNoteentityByNoteTagRecord(tagItem.getTagRecordEntries()), getActivity().getLayoutInflater());
        tagManagerAdapter.setHierarchyTagentity(tagItem.getNoteTagEntity());
        currentAdapter.setNeedUpdateData();
        tagManagerAdapterStack.push(currentAdapter);
        currentAdapter = tagManagerAdapter;
        tagManagerAdapter.setOnItemClickListener(onItemClickListener);
        recyclerView.setAdapter(tagManagerAdapter);

        addHierarchyTitle(tagItem.getNoteTagEntity().getTagName());
    }

    private void backToParentHierarchy(){
        TagManagerAdapter tagManagerAdapter = tagManagerAdapterStack.pop();
        currentAdapter = tagManagerAdapter;
        recyclerView.setAdapter(tagManagerAdapter);

        removeHierarchyTitle();
    }

    private List<NoteEntity> getNoteentityByNoteTagRecord(List<TagRecordEntity> tagRecordEntries){
        List<NoteEntity> noteEntries = new ArrayList<>();
        Iterator<TagRecordEntity> iterator = tagRecordEntries.iterator();
        if(iterator.hasNext()){
            TagRecordEntity tagRecordEntity = iterator.next();
            for(NoteEntity noteEntity : allNoteEntries){
                if(tagRecordEntity.getNoteId() == noteEntity.getNoteId()){
                    noteEntries.add(noteEntity);
                    if(iterator.hasNext()){
                        tagRecordEntity = iterator.next();
                    }
                    else break;
                }
            }
        }
        return noteEntries;
    }

    private void addHierarchyTitle(String hierarchyTitle){
        View hierarchyView = getActivity().getLayoutInflater().inflate(R.layout.tag_hierarchy_item, hierarchyDisplayView, false);
        ((TextView) hierarchyView.findViewById(R.id.hierarchy_title)).setText(hierarchyTitle);
        hierarchyDisplayView.addView(hierarchyView);
        if(scrollView != null)
            scrollView.arrowScroll(View.FOCUS_RIGHT);
    }

    private void removeHierarchyTitle(){
        int childCount = hierarchyDisplayView.getChildCount();
        if(childCount > 0){
            hierarchyDisplayView.removeViewAt(childCount - 1);
        } else Log.i(TAG, "removeHierarchyTitle: no more hierarchy inside hierarchy directory to remove");
    }

    private View.OnClickListener fabOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            PopupMenu popupMenu = new PopupMenu(getContext(), v);
            Menu menu = popupMenu.getMenu();
            MenuItem menuItem = menu.add("add tag");
            menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    NoteTagEntity noteTagEntity = new NoteTagEntity(String.valueOf(DateUtil.getInstance().getTime()).substring(10));
                    boolean needUpdateTopTag = false;
                    if(currentAdapter.getHierarchyTagentity() != null){
                        noteTagEntity.setRootTagId(currentAdapter.getHierarchyTagentity().getTagId());
                        needUpdateTopTag = true;
                    }
                    long tagId = ANoteDBManager.getInstance(getContext()).insertTag(noteTagEntity);
                    if(tagId != Constants.NO_TAG_ID){
                        noteTagEntity.setTagId(tagId);
                        currentAdapter.insertItem(noteTagEntity);
                        if(needUpdateTopTag)
                            DataProvider.getInstance(getContext()).updateAllTopTagentity();
                    }
                    else Toast.makeText(getContext(),Constants.TOAST_TAG_ADD_FAILED, Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
            if(currentAdapter.getHierarchyTagentity() != null) {
                menuItem = menu.add("add note here");
                menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        Intent intent = new Intent(getContext(), EditNoteActivity.class);
                        intent.putExtra(Constants.ACTION_TYPE_OF_EDIT_NOTE, EditNoteActivity.EDIT_TYPE_CREATE);
                        intent.putExtra(Constants.WITH_TAG_ID, currentAdapter.getHierarchyTagentity().getTagId());
                        startActivityForResult(intent, REQUEST_CODE_NEW_NOTE_EDIT);
                        return true;
                    }
                });
            }
            popupMenu.setGravity(Gravity.END);
            popupMenu.show();
        }
    };
}
