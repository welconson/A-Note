package com.tcl.shenwk.aNote.view.fragment;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.tcl.shenwk.aNote.R;
import com.tcl.shenwk.aNote.entry.NoteTagEntry;
import com.tcl.shenwk.aNote.entry.TagRecordEntry;
import com.tcl.shenwk.aNote.model.ANoteDBManager;
import com.tcl.shenwk.aNote.model.DataProvider;
import com.tcl.shenwk.aNote.util.Constants;
import com.tcl.shenwk.aNote.view.activity.EditNoteActivity;
import com.tcl.shenwk.aNote.view.adapter.TagAdapter;

import java.util.List;

/**
 * Inside edit note activity tag adding fragment.
 * Created by shenwk on 2018/3/19.
 */

public class TagEditFragment extends DialogFragment {
    TagAdapter tagAdapter;
    TextView textView;
    private ExitCallback exitCallback;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.tag_editor_layout);
        dialog.setCanceledOnTouchOutside(false);
        setAdapter(dialog);
        setReaction(dialog);
        return dialog;
    }


    public void setExitCallback(ExitCallback exitCallback){
        this.exitCallback = exitCallback;
    }

    private void setAdapter(Dialog dialog){
        RecyclerView recyclerView = dialog.findViewById(R.id.tag_list);
        List<NoteTagEntry> noteTagEntries = ANoteDBManager.getInstance(getContext()).queryAllTag();

        tagAdapter = new TagAdapter(((LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE)),
                TagAdapter.setItemList(noteTagEntries,
                        ((EditNoteActivity) getActivity()).getNoteTagRecord()),
                recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(tagAdapter);
    }

    private void setReaction(Dialog dialog){
        textView = dialog.findViewById(R.id.edit_tag_name);
        ImageButton imageButton = dialog.findViewById(R.id.add_tag);
        imageButton.setOnClickListener(addTagListener);
        imageButton = dialog.findViewById(R.id.tag_cancel);
        imageButton.setOnClickListener(cancelListener);
        imageButton = dialog.findViewById(R.id.tag_done);
        imageButton.setOnClickListener(doneListener);
    }

    private View.OnClickListener addTagListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(textView.getText().length() < 0){
                Toast.makeText(getContext(), Constants.TOAST_TAG_NOT_EMPTY, Toast.LENGTH_SHORT).show();
            }
            else {
                NoteTagEntry noteTagEntry = new NoteTagEntry(textView.getText().toString());
                int position;
                if((position = tagAdapter.isTagInList(noteTagEntry.getTagName())) >= 0){
                    tagAdapter.checkOnPosition(position);
                    textView.setText("");
                }else {
                    long tagId = ANoteDBManager.getInstance(getContext()).insertTag(noteTagEntry);
                    if (tagId == Constants.NO_TAG_ID)
                        Toast.makeText(getContext(), "add tag error", Toast.LENGTH_SHORT).show();
                    else {
                        noteTagEntry.setTagId(tagId);
                        tagAdapter.addNewTag(noteTagEntry);
                        textView.setText("");
                        DataProvider.getInstance(getContext()).updateNoteTagEntry();
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
            List<TagRecordEntry> tagRecordEntries = tagAdapter.getCheckedList();
            if(exitCallback != null)
                exitCallback.onTagSelectDone(tagRecordEntries);
            dismiss();
        }
    };

    public interface ExitCallback{
        void onTagSelectDone(List<TagRecordEntry> tagRecordEntries);
    }
}
