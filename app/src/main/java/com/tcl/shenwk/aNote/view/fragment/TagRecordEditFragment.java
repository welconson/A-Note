package com.tcl.shenwk.aNote.view.fragment;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.tcl.shenwk.aNote.R;
import com.tcl.shenwk.aNote.entity.NoteTagEntity;
import com.tcl.shenwk.aNote.entity.TagRecordEntity;
import com.tcl.shenwk.aNote.model.ANoteDBManager;
import com.tcl.shenwk.aNote.model.DataProvider;
import com.tcl.shenwk.aNote.util.Constants;
import com.tcl.shenwk.aNote.view.activity.EditNoteActivity;
import com.tcl.shenwk.aNote.view.adapter.TagRecordEditAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Inside edit note activity tag adding fragment.
 * Created by shenwk on 2018/3/19.
 */

public class TagRecordEditFragment extends DialogFragment implements TagRecordEditAdapter.OnItemClickListener{
    private static final String TAG = "TagRecordEditFragment";
    private TagRecordEditAdapter currentAdapter;
    private TextView textView;
    private RecyclerView recyclerView;
    private ImageButton back;
    private ImageButton cancel;

    private Stack<TagRecordEditAdapter> tagRecordEditAdapterStack;
    private Stack<TagTreeNode> tagTreeNodeStack;
    private TagTreeNode currentNode;
    private TagTreeNode rootNode;

    private ExitCallback exitCallback;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.tag_editor_layout);
        dialog.setCanceledOnTouchOutside(false);
        rootNode = new TagTreeNode();
        rootNode.subTagNodes = constructTagTree(ANoteDBManager.getInstance(getContext()).queryAllTopTag(),
                null, ((EditNoteActivity) getActivity()).getNoteTagRecord());
        currentNode = rootNode;
        setRecyclerView(dialog);

        textView = dialog.findViewById(R.id.edit_tag_name);
        ImageButton imageButton = dialog.findViewById(R.id.add_tag);
        imageButton.setOnClickListener(addTagListener);
        imageButton = dialog.findViewById(R.id.tag_done);
        imageButton.setOnClickListener(doneListener);

        cancel = dialog.findViewById(R.id.tag_cancel);
        cancel.setOnClickListener(cancelListener);

        back = dialog.findViewById(R.id.back);
        back.setOnClickListener(backListener);
        back.setVisibility(View.INVISIBLE);

        tagRecordEditAdapterStack = new Stack<>();
        tagTreeNodeStack = new Stack<>();
        return dialog;
    }


    public void setExitCallback(ExitCallback exitCallback){
        this.exitCallback = exitCallback;
    }

    private void setRecyclerView(Dialog dialog){
        recyclerView = dialog.findViewById(R.id.tag_list);
        List<NoteTagEntity> noteTagEntries = getSubtagFromNode(currentNode);

        currentAdapter = new TagRecordEditAdapter(((LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE)),
                TagRecordEditAdapter.setItemList(noteTagEntries,
                        ((EditNoteActivity) getActivity()).getNoteTagRecord())
        );
        currentAdapter.setOnItemClickListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(currentAdapter);
    }

    private void switchToSubTagHierarchy(NoteTagEntity noteTagEntity, TagRecordEntity tagRecordEntity){
        TagTreeNode tagTreeNode = getTagTreeNode(noteTagEntity);
        if(tagTreeNode != null) {
            List<NoteTagEntity> noteTagEntities = getSubtagFromNode(tagTreeNode);

            // save now adapter hierarchy record
            TagTreeNode rootNode = getRootNode(tagTreeNode);
            if(rootNode.hierarchyRecord!= null && tagRecordEntity != null && rootNode.hierarchyRecord.getTagId() != tagRecordEntity.getTagId()){
                rootNode.hierarchyRecord = tagRecordEntity;
            }

            TagRecordEditAdapter tagRecordEditAdapter = new TagRecordEditAdapter(getActivity().getLayoutInflater(),
                    TagRecordEditAdapter.setItemList(noteTagEntities, rootNode.hierarchyRecord));
            tagRecordEditAdapter.setHasRootTag(true);
            tagRecordEditAdapter.setOnItemClickListener(this);

            tagTreeNodeStack.push(currentNode);
            tagRecordEditAdapterStack.push(currentAdapter);
            currentNode = tagTreeNode;
            currentAdapter = tagRecordEditAdapter;
            recyclerView.setAdapter(tagRecordEditAdapter);
            back.setVisibility(View.VISIBLE);
            cancel.setVisibility(View.INVISIBLE);
        } else Toast.makeText(getContext(), Constants.TOAST_TAG_RECORD_EDIT_ENTER_SUB_TAG, Toast.LENGTH_SHORT).show();
    }

    private void backToParentTagHierarchy(){
        if(tagRecordEditAdapterStack.size() <= 0) {
            Log.i(TAG, "backToParentTagHierarchy: adapter stack is empty");
            return;
        }
        TagRecordEntity tagRecordEntity = currentAdapter.getCheckedTagRecordUnderRootTag();

        currentAdapter = tagRecordEditAdapterStack.pop();

        TagTreeNode rootNode = getRootNode(currentNode);
        // if the exiting adapter has a tag got checked, replace the hierarchy record in rootNode.
        if(tagRecordEntity != null){
            if(rootNode.hierarchyRecord == null || tagRecordEntity.getTagId() != rootNode.hierarchyRecord.getTagId())
                rootNode.hierarchyRecord = tagRecordEntity;
        }

        // check whether hierarchy is the original
        int position;
        if(rootNode.hierarchyRecord != null && currentNode.noteTagEntity!= null && rootNode.hierarchyRecord.getTagId() != currentNode.noteTagEntity.getTagId() &&
                (position = currentAdapter.getPositionByTagId(currentNode.noteTagEntity.getTagId())) != -1){
            currentAdapter.unCheckOnPosition(position);
        }
        currentNode = tagTreeNodeStack.pop();

        recyclerView.setAdapter(currentAdapter);
        if(tagRecordEditAdapterStack.size() == 0){
            cancel.setVisibility(View.VISIBLE);
            back.setVisibility(View.INVISIBLE);
        }
    }

    // Get tag tree node through the noteTagEntity from current root node.
    private TagTreeNode getTagTreeNode(NoteTagEntity noteTagEntity){
        TagTreeNode tagTreeNode = null;
        if(currentNode.subTagNodes != null){
            for(TagTreeNode childNode : currentNode.subTagNodes){
                if(noteTagEntity.getTagId() == childNode.noteTagEntity.getTagId()) {
                    tagTreeNode = childNode;
                }
            }
        }
        return tagTreeNode;
    }

    // Get noteTagEntities of the tree node.
    private List<NoteTagEntity> getSubtagFromNode(TagTreeNode rootNode){
        List<NoteTagEntity> noteTagEntities = new ArrayList<>();
        if(rootNode != null && rootNode.subTagNodes != null) {
            for (TagTreeNode tagTreeNode : rootNode.subTagNodes)
                noteTagEntities.add(tagTreeNode.noteTagEntity);
        }
        return noteTagEntities;
    }

    private View.OnClickListener addTagListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(textView.getText().length() < 0){
                Toast.makeText(getContext(), Constants.TOAST_TAG_NOT_EMPTY, Toast.LENGTH_SHORT).show();
            }
            else {
                NoteTagEntity noteTagEntity = new NoteTagEntity(textView.getText().toString());
                if(currentNode.noteTagEntity != null)
                    noteTagEntity.setRootTagId(currentNode.noteTagEntity.getTagId());
                int position;
                // Check whether is a existed tag, if so we will directly check it.
                if((position = currentAdapter.isTagInList(noteTagEntity.getTagName())) >= 0){
                    currentAdapter.checkOnPosition(position);
                    textView.setText("");
                }
                // If is a new tag, we will create the new tag, and store it to database, also check it.
                else {
                    long tagId = ANoteDBManager.getInstance(getContext()).insertTag(noteTagEntity);
                    if (tagId == Constants.NO_TAG_ID)
                        Toast.makeText(getContext(), "add tag error", Toast.LENGTH_SHORT).show();
                    else {
                        currentNode.subTagNodes = constructTagTree(
                                ANoteDBManager.getInstance(getContext())
                                        .queryAllSubTagByRootTagId(currentNode.noteTagEntity.getTagId()),
                                currentNode, new ArrayList<TagRecordEntity>());
                        noteTagEntity.setTagId(tagId);
                        currentAdapter.addNewTag(noteTagEntity);
                        textView.setText("");
                        DataProvider.getInstance(getContext()).updateAllTopTagentity();
                    }
                }
            }
        }
    };

    private View.OnClickListener cancelListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dismiss();
        }
    };

    private View.OnClickListener doneListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            while(tagRecordEditAdapterStack.size() > 0){
                backToParentTagHierarchy();
            }
            List<TagRecordEntity> rootTagRecords = currentAdapter.getCheckedList();
            List<TagRecordEntity> tagRecordEntries = new ArrayList<>();
            for(TagTreeNode tagTreeNode : currentNode.subTagNodes){
                for(TagRecordEntity tagRecordEntity : rootTagRecords){
                    if(tagTreeNode.noteTagEntity.getTagId() == tagRecordEntity.getTagId() && tagRecordEntity.status == TagRecordEntity.NEW_CREATE){
                        tagTreeNode.hierarchyRecord = tagRecordEntity;
                    }
                }
                if(tagTreeNode.hierarchyRecord != null) {
                    tagRecordEntries.add(tagTreeNode.hierarchyRecord);
                    if (tagTreeNode.originHierarchyRecord != null && tagTreeNode.hierarchyRecord.getTagId() != tagTreeNode.originHierarchyRecord.getTagId()) {
                        tagTreeNode.originHierarchyRecord.status = TagRecordEntity.TO_DELETE;
                        tagRecordEntries.add(tagTreeNode.originHierarchyRecord);
                    }
                }
            }
            if(exitCallback != null)
                exitCallback.onTagSelectDone(tagRecordEntries);
            dismiss();
        }
    };

    private View.OnClickListener backListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            backToParentTagHierarchy();
        }
    };

    @Override
    public void onItemClick(NoteTagEntity noteTagEntity, TagRecordEntity tagRecordEntity) {
        switchToSubTagHierarchy(noteTagEntity, tagRecordEntity);
    }

    public interface ExitCallback{
        void onTagSelectDone(List<TagRecordEntity> tagRecordEntries);
    }

    private List<TagTreeNode> constructTagTree(List<NoteTagEntity> noteTagEntities, TagTreeNode parentNode, List<TagRecordEntity> tagRecordEntities){
        // No sub tags.
        if(noteTagEntities.size() == 0)
            return null;
        List<TagTreeNode> tagTreeNodes = new ArrayList<>();
        for(NoteTagEntity noteTagEntity : noteTagEntities){
            TagTreeNode tagTreeNode = new TagTreeNode();
            tagTreeNode.noteTagEntity = noteTagEntity;
            if(parentNode != null)
                tagTreeNode.parentNode = parentNode;
            TagTreeNode rootNode = getRootNode(tagTreeNode);
            tagTreeNode.subTagNodes = constructTagTree(ANoteDBManager.getInstance(getContext())
                    .queryAllSubTagByRootTagId(noteTagEntity.getTagId()), tagTreeNode, tagRecordEntities);

            if(rootNode.hierarchyRecord == null) {
                for (TagRecordEntity tagRecordEntity : tagRecordEntities) {
                    if (tagRecordEntity.getTagId() == noteTagEntity.getTagId()) {
                        rootNode.hierarchyRecord = tagRecordEntity;
                        rootNode.originHierarchyRecord = tagRecordEntity;
                        break;
                    }
                }
            }
            tagTreeNodes.add(tagTreeNode);
        }

        return tagTreeNodes;
    }

    private class TagTreeNode{
        List<TagTreeNode> subTagNodes;
        NoteTagEntity noteTagEntity;
        TagTreeNode parentNode = null;
        TagRecordEntity hierarchyRecord = null;
        TagRecordEntity originHierarchyRecord = null;
    }

    private TagTreeNode getRootNode(TagTreeNode tagTreeNode){
        if(tagTreeNode == null)
            return null;
        while (tagTreeNode.parentNode != null)
            tagTreeNode = tagTreeNode.parentNode;
        return tagTreeNode;
    }
}
