package com.tcl.shenwk.aNote.view;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tcl.shenwk.aNote.R;
import com.tcl.shenwk.aNote.entry.NoteEntry;
import com.tcl.shenwk.aNote.util.Constants;
import com.tcl.shenwk.aNote.view.activity.EditNoteActivity;

import java.util.List;

/**
 * Adapter used for holding note preview items.
 * Created by shenwk on 2018/2/6.
 */

public class CustomAdapter extends RecyclerView.Adapter<CustomViewHolder> {
    private LayoutInflater mInflater = null;
    private List<NoteEntry> mNoteList;
    private Context context;

    public CustomAdapter(LayoutInflater inflater, List<NoteEntry> noteEntries) {
        super();
        mInflater = inflater;
        mNoteList = noteEntries;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View item = mInflater.inflate(R.layout.note_preview_item, parent, false);
        return new CustomViewHolder(item);
    }

    @Override
    public void onBindViewHolder(CustomViewHolder holder, int position) {
        View itemView = holder.itemView;
        final NoteEntry noteEntry = mNoteList.get(position);
        TextView textView = itemView.findViewById(R.id.item_title);
        if(noteEntry.getNoteTitle() == null || noteEntry.getNoteTitle().equals("")){
            textView.setText(R.string.item_no_title);
        }
        else textView.setText(noteEntry.getNoteTitle());
        textView = itemView.findViewById(R.id.item_text);
        textView.setText(noteEntry.getNoteContent() == null ? "" : noteEntry.getNoteContent());
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), EditNoteActivity.class);
                intent.putExtra(Constants.ACTION_EDIT_NOTE, EditNoteActivity.EDIT_TYPE_MODIFY);
                intent.putExtra(Constants.EDIT_NOTE_ID_NAME, noteEntry.getNoteId());
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mNoteList.size();
    }
}
