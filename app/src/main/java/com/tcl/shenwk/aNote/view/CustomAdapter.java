package com.tcl.shenwk.aNote.view;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.tcl.shenwk.aNote.R;
import com.tcl.shenwk.aNote.entry.NoteEntry;
import com.tcl.shenwk.aNote.model.ANoteDBManager;
import com.tcl.shenwk.aNote.model.EditNoteHandler;
import com.tcl.shenwk.aNote.util.Constants;
import com.tcl.shenwk.aNote.view.activity.EditNoteActivity;

import java.util.List;

/**
 * Adapter used for holding note preview items.
 * Created by shenwk on 2018/2/6.
 */

public class CustomAdapter extends RecyclerView.Adapter<CustomViewHolder> {
    private static String TAG = "CustomAdapter";
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
    public void onBindViewHolder(final CustomViewHolder holder, int position) {
        View itemView = holder.itemView;
        NoteEntry noteEntry = mNoteList.get(position);
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
                int position = holder.getAdapterPosition();
                NoteEntry noteEntry = mNoteList.get(position);
                Intent intent = new Intent(v.getContext(), EditNoteActivity.class);
                intent.putExtra(Constants.ACTION_EDIT_NOTE, EditNoteActivity.EDIT_TYPE_MODIFY);
                intent.putExtra(Constants.EDIT_NOTE_ID, noteEntry.getNoteId());
                intent.putExtra(Constants.ITEM_ENTRY, noteEntry);
                intent.putExtra(Constants.ITEM_POSITION, position);
                v.getContext().startActivity(intent);
            }
        });
        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
                Menu menu = popupMenu.getMenu();
                menu.add("delete");
                menu.getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int position = holder.getAdapterPosition();
                        NoteEntry noteEntry = mNoteList.get(position);
                        Log.i(TAG, "onMenuItemClick: delete onClick");
                        EditNoteHandler.removeNote(v.getContext(), noteEntry);
                        mNoteList.remove(position);
                        notifyItemRemoved(position);
                        return true;
                    }
                });
                menu.add("detail");
                popupMenu.setGravity(Gravity.END);
                popupMenu.show();
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mNoteList.size();
    }

    public void refreshSingleItemByPosition(int position, NoteEntry noteEntry){
        if(position < 0 || noteEntry == null)
            return;
        mNoteList.set(position, noteEntry);
        notifyItemChanged(position);
    }

    public void addItem(NoteEntry noteEntry){
        if(noteEntry == null)
            return;
        mNoteList.add(Constants.ITEM_BEGIN_POSITION, noteEntry);
        notifyItemInserted(Constants.ITEM_BEGIN_POSITION);
    }
}
